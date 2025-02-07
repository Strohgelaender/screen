package de.fll.screen.service.parser;

import de.fll.screen.model.*;
import de.fll.screen.repository.CompetitionRepository;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class FLLRobotGameParser implements Parser {

	private static final String HOT_LIVE = "https://evaluation.hands-on-technology.org";
	private static final String HOT_TEST = "https://test.evaluation.hands-on-technology.org";
	private static final String LOCAL_DE = "/de";
	private static final String LOCAL_EN = "/en";
	private static final String LOGIN_PATH = "/login";
	private static final String RG_SCORE_PATH = "/robot-game-score?tournament=";
	private static final String COMPETITION_SELECTION_PATH = "/tournament?action=choose";
	private static final String RG_PAIRING_PATH = "/tournament?tournament=";

	@Value("${fll.username}")
	private String username;

	@Value("${fll.password}")
	private String password;

	@Value("${fll.environment}")
	private String environment;

	private final CompetitionRepository competitionRepository;

	private static String local = LOCAL_DE;

	// Map usernames to cookie managers
	private final Map<String, CookieManager> cookieManagers = new HashMap<>();

	public FLLRobotGameParser(CompetitionRepository competitionRepository) {
		this.competitionRepository = competitionRepository;
	}

	public Competition parse(Competition competition, int id) {
		return parse(competition, id, username, password);
	}

	@Override
	public List<String> getAvailableCompetitionIds(String user, String password) {

		CookieManager cookieManager = cookieManagers.computeIfAbsent(user, k -> new CookieManager());

		var page = requestLogin(cookieManager, makeURL(LOGIN_PATH), makeURL(COMPETITION_SELECTION_PATH), user, password);
		var doc = Jsoup.parse(page);
		return doc.selectFirst(".link-set")
				.select("li")
				.stream()
				.map(e -> e.selectFirst("a")
						.attr("href"))
				.map(s -> {
					var args = s.split("\\?")[1];
					return Arrays.stream(args.split("&"))
							.map(a -> a.split("="))
							.filter(a -> "tournament".equals(a[0]))
							.map(a -> a[1])
							.findFirst().orElse("");
				}).filter(s -> !((String) s).isEmpty())
				.toList();
	}

	@Nonnull
	@Override
	public Competition parse(@Nullable Competition competition, int id, String user, String password) {
		CookieManager cookieManager = cookieManagers.computeIfAbsent(user, k -> new CookieManager());

		if (competition == null) {
			competition = new Competition();
			competition.setInternalId(id);
		}

		String rawScorePage = requestLogin(cookieManager, makeURL(LOGIN_PATH), makeURL(RG_SCORE_PATH) + id, user, password);
		if (rawScorePage == null) {
			return competition; // SOMETHING WENT WRONG WHILE GETTING DATA
		}
		updateCompetition(Jsoup.parse(rawScorePage), competition);

		String rawPairingPage = requestPageAfterLogin(cookieManager, makeURL(RG_PAIRING_PATH));
		if (rawPairingPage == null) {
			return competition; // SOMETHING WENT WRONG WHILE GETTING DATA
		}
		updatePairings(Jsoup.parse(rawPairingPage), competition);

		if (competitionRepository != null) {
			competition = competitionRepository.save(competition);
		}
		return competition;
	}

	private String requestLogin(CookieManager cookieManager, String url, String targetUrl, String user, String password) {
		return executeRequest(
				cookieManager,
				url,
				b -> b.header("Content-Type", "application/x-www-form-urlencoded"),
				b -> b.POST(
						HttpRequest.BodyPublishers.ofString(
								getLoginBody(user, password, targetUrl))));
	}

	private String getLoginBody(String user, String pwd, String targetUrl) {
		var targetPath = Base64.getUrlEncoder().encodeToString(targetUrl.getBytes(StandardCharsets.UTF_8));
		return "FORM_SUBMIT=tl_login_216"
				+ "&REQUEST_TOKEN="
				+ "&_target_path=" + targetPath
				+ "&_always_use_target_path=0"
				+ "&username=" + user
				+ "&password=" + pwd;
	}

	private String requestPageAfterLogin(CookieManager cookieManager, String tournamentUrl) {
		return executeRequest(cookieManager, tournamentUrl, HttpRequest.Builder::GET);
	}

	@SafeVarargs
	private String executeRequest(CookieManager cookieManager, String uri, UnaryOperator<HttpRequest.Builder>... modifiers) {
		try (HttpClient client = createHttpClient(cookieManager)) {
			var requestBuilder = HttpRequest.newBuilder(new URI(uri));
			for (UnaryOperator<HttpRequest.Builder> modifier : modifiers) {
				requestBuilder = modifier.apply(requestBuilder);
			}
			HttpRequest request = requestBuilder.build();
			HttpResponse<String> send = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
			return send.body();
		} catch (IOException | InterruptedException | URISyntaxException e) {
			LoggerFactory.getLogger(FLLRobotGameParser.class).error(e.getMessage(), e);
			return null;
		}
	}

	private HttpClient createHttpClient(CookieManager cookieManager) {
		return HttpClient.newBuilder()
				.followRedirects(HttpClient.Redirect.ALWAYS)
				.cookieHandler(cookieManager)
				.build();
	}

	private String makeURL(String path) {
		return environment + local + path;
	}

	private void updateCompetition(Document doc, Competition comp) {
		if (comp.getName() == null || comp.getName().isEmpty()) {
			writeCompetitionTitle(doc, comp);
		}
		Elements tableRows = Objects.requireNonNull(doc.selectFirst("tBody"), "Table Body not found!")
				.select("tr");

		// TODO: find current comp object
		Category category = generateCategory(comp);

		tableRows.forEach(
				e -> {
					var team = parseTeam(e);
					// TODO: check currently existing teams.
					category.getTeams().add(team);
				});

		comp.getCategories().add(category);
	}

	private Team parseTeam(Element e) {
		try { // FIXME ccsQ = "span"
			var name = Objects.requireNonNull(
					e.selectFirst("span"), "Name Element could not be found!")
					.ownText();
			var teamId = Objects.requireNonNull(e.selectFirst(".team-id"), "Team ID could not be found!")
					.text();

			var scores = e.select("td").stream()
					.map(Element::text)
					.mapToInt(Integer::parseInt)
					.boxed()
					.collect(Collectors.toList());
			scores.remove(3);

			System.out.println("Name: " + name + ", Team: " + teamId + ", Scores: " + scores);

			Team team = new Team(teamId, scores.size());
			team.setName(name);
			// TODO: do we even need HoT ID?
			int hotID = Integer.parseInt(teamId.substring(1, teamId.length() - 2));
			var teamScores = team.getScores();
			scores.forEach(s -> teamScores.add(generateFromPoints(s, team)));
			return team;
		} catch (NullPointerException | NumberFormatException ex) {
			// TODO: Return empty team and let user know + give freedom to "fix" manually?
			throw new IllegalArgumentException(
					"Unparsable Element!" + System.lineSeparator() + e, ex);
		}
	}

	private Score generateFromPoints(int points, Team team) {
		Score score = new Score(points, 0);
		score.setTeam(team);
		return score;
	}

	private void writeCompetitionTitle(Document doc, Competition competition) {
		String competitionTitle = Objects.requireNonNull(
				doc.selectFirst(".page-title"),
				"Could not find Title Element!" + System.lineSeparator() + doc)
				.text()
				.split(":")[1]
				.strip();
		competition.setName(competitionTitle);
	}

	private Category generateCategory(Competition competition) {
		Category category = new Category();
		category.setName(competition.getName());
		// TODO: make request to teams overview in order to retrieve tournament id
		category.setCompetition(competition);
		category.setCategoryScoring(CategoryScoring.FLL_ROBOT_GAME);
		return category;
	}

	private int getRobotGameState() {
		return 0; // TODO: Auto-Detect Round to be used in smart generation
	}

	private void updatePairings(Document doc, Competition competition) {
		Element collapseSet = doc.selectFirst(".collaps-set");
		var preliminaryPairings = Objects.requireNonNull(collapseSet, "Could not find preliminary round containers!")
				.select(".toggle-preliminaryround");
		for (int i = 0; i < 3; i++) {
			var pairings = preliminaryPairings.get(i).select(".match");
			pairings.forEach(
					e -> {
						var a = e.select("a");
						if (a.size() == 1) {
							// FREE SLOT DETECTED
							System.out.println(
									a.getFirst().text()
											+ " ||||| "
											+ "FREIER SLOT (HAT KEINE PKTE)");
							System.out.println(extractLink(a.getFirst()));
						} else {
							var teamLink1 = a.get(0);
							var teamLink2 = a.get(1);

							System.out.println(teamLink1.text() + " ||||| " + teamLink2.text());
							System.out.println(
									extractLink(teamLink1) + " ||||| " + extractLink(teamLink2));
						}
					});
		}
		// Test code for single match analysis tools
		Elements allPairingsAllRounds = preliminaryPairings.select("a");
		var pairing = allPairingsAllRounds.get(
				ThreadLocalRandom.current().nextInt(allPairingsAllRounds.size()));
		// checkMatch(extractLink(pairing), competition);
		// System.out.println("DONE");
		// TODO extract data from eval sheets
	}

	private void checkMatch(String path, Competition competition, String username) {
		String match = requestPageAfterLogin(cookieManagers.get(username), environment + "/" + path);
		// System.out.println(match);
		var core = Jsoup.parse(match);
		Element ratingForm = core.expectForm("#ratingForm");
		Elements tasks = ratingForm.selectFirst(".collaps-set").select("li");
		List<int[]> magicValues = new ArrayList<>();
		tasks.forEach(t -> {
			// General Points
			var head = t.selectFirst(".collaps-head.icon-right");
			var title = head.selectFirst("h2").ownText();
			var points = head.selectFirst(".status.task-points").selectFirst(".value").ownText();
			if (points.isEmpty()) {
				points = "0";
			}
			System.out.println(title + " : " + points + " points");
			// DETAILS
			var subTasks = t.selectFirst(".collaps-content").select(".sub-task.border-bottom.clearfix");

			int[] partialPoints = new int[subTasks.size() + 1];
			partialPoints[0] = Integer.parseInt(points);
			for (int i = 0; i < subTasks.size(); i++) {
				Element st = subTasks.get(i);
				var stText = st.selectFirst(".label").selectFirst("strong").ownText();
				var stPointsNode = st.selectFirst("[checked]");
				String stPoints;
				if (stPointsNode == null) {
					stPoints = "0";
				} else {
					stPoints = stPointsNode.attr("value");
				}
				System.out.println(stText + " " + stPoints + " points");
				partialPoints[i + 1] = Integer.parseInt(stPoints);
			}
			magicValues.add(partialPoints);
		});
		var scoreDetails = new ScoreDetails(magicValues.toArray(new int[magicValues.size()][]));
		System.out.println(scoreDetails);
		// TODO: decide about whether to include gracious professionalism™️
		Element graciousProfessionalismForm = core.expectForm("#ratingFormNoAjax");
	}

	private String extractLink(Element element) {
		return element.attr("href");
	}

	public static void main(String[] args) {
		local = LOCAL_DE;
		var parser = new FLLRobotGameParser(null);
		parser.environment = HOT_TEST;
		// Competition competition = parser.parse(null, 231, args[0], args[1]);
		var res = parser.getAvailableCompetitionIds(args[0], args[1]);
		System.out.println("DONE");
	}
}
