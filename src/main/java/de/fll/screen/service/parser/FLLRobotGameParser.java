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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
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
	private static final String COMPETITION_SELECTION_PATH_TEMPLATE = "/tournament%s?action=choose";
	private static final String RG_PAIRING_PATH = "/tournament?tournament=";
	private static final boolean PARSE_PAIRINGS = false;

	private static final Logger log = LoggerFactory.getLogger(FLLRobotGameParser.class);

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

	public Competition parse(Competition competition, int id) throws Exception {
		return parse(competition, id, username, password);
	}

	public Category parseTestRound(int id) throws Exception {
		return parseTestRound(id, username, password);
		// TODO: Log webrequests (start and end) for better debugging
	}

	public Category parseTestRound(int id, String username, String password) throws Exception {
		/// TODO quick hack at competiton, make this a lot better please!!!!
		boolean loginNeeded = !cookieManagers.containsKey(username);
		CookieManager cookieManager = cookieManagers.computeIfAbsent(username, k -> new CookieManager());

		// Beim Schiri Login hat der Redirect nicht funktioniert, daher nutzten wir den bestehendne Cookie um direkt auf die Seite zu navigieren.
		// TODO: Erkennen wann der Cookie abläuft und dann erneut anmelden.
		if (!loginNeeded) {
			requestPageAfterLogin(cookieManager, makeURL(RG_SCORE_PATH + id));
		} else {
			requestLogin(cookieManager, makeURL(LOGIN_PATH), makeURL(RG_SCORE_PATH + id), username, password);
		}

		Document paringPage = requestPageAfterLogin(cookieManager, makeURL(RG_PAIRING_PATH));
		if (paringPage == null) {
			return null; // SOMETHING WENT WRONG WHILE GETTING DATA
		}
		return testroundPairings(paringPage);
	}

	public List<String> getOwnCompetitionIds() throws Exception {
		return getOwnCompetitionIds(username, password);
	}

	@Override
	public List<String> getOwnCompetitionIds(String user, String password) throws Exception {
		CookieManager cookieManager = cookieManagers.computeIfAbsent(user, k -> new CookieManager());

		var doc = requestLogin(cookieManager, makeURL(LOGIN_PATH), makeURL(makeTournamentSelectionPath()), user, password);
		return doc.selectFirst(".link-set")
				.select("li")
				.stream()
				.map(e -> e.selectFirst("a").attr("href"))
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
	public Competition parse(@Nullable Competition competition, int id, String user, String password) throws Exception{
		boolean loginNeeded = !cookieManagers.containsKey(user);
		CookieManager cookieManager = cookieManagers.computeIfAbsent(user, k -> new CookieManager());

		if (competition == null) {
			competition = new Competition();
			competition.setInternalId(id);
		}

		// Beim Schiri Login hat der Redirect nicht funktioniert, daher nutzten wir den bestehendne Cookie um direkt auf die Seite zu navigieren.
		// TODO: Erkennen wann der Cookie abläuft und dann erneut anmelden.
		Document pageDocument;
		if (!loginNeeded) {
			pageDocument = requestPageAfterLogin(cookieManager, makeURL(RG_SCORE_PATH + id));
		} else {
			pageDocument = requestLogin(cookieManager, makeURL(LOGIN_PATH), makeURL(RG_SCORE_PATH + id), user, password);
		}
		if (pageDocument == null) {
			return competition; // SOMETHING WENT WRONG WHILE GETTING DATA
		}

		updateCompetition(pageDocument, competition);

		if (PARSE_PAIRINGS) {
			Document pairingPage = requestPageAfterLogin(cookieManager, makeURL(RG_PAIRING_PATH));
			if (pairingPage == null) {
				return competition; // SOMETHING WENT WRONG WHILE GETTING DATA
			}
			updatePairings(pairingPage, competition);
		}

		if (competitionRepository != null) {
			competition = competitionRepository.save(competition);
		}
		return competition;
	}

	private Document requestLogin(CookieManager cookieManager, String url, String targetUrl, String user, String password) throws Exception {
		var document = executeRequest(cookieManager, url,
				b -> b.header("Content-Type", "application/x-www-form-urlencoded"),
				b -> b.POST(HttpRequest.BodyPublishers.ofString(getLoginBody(user, password, targetUrl))));
		if (getPageTitle(document).equals("Wettbewerb auswählen")) {
			// Redirect failed, try again
			return executeRequest(cookieManager, targetUrl, HttpRequest.Builder::GET);
		}
		return document;
	}

	private String getLoginBody(String user, String pwd, String targetUrl) {
		var targetPath = Base64.getEncoder().encodeToString(targetUrl.getBytes(StandardCharsets.UTF_8));

		return "FORM_SUBMIT=tl_login_216"
				+ "&REQUEST_TOKEN="
				+ "&_target_path=" + targetPath
				+ "&_always_use_target_path=0"
				+ "&username=" + user
				+ "&password=" + pwd;
	}

	private Document requestPageAfterLogin(CookieManager cookieManager, String tournamentUrl) throws Exception {
		return executeRequest(cookieManager, tournamentUrl, HttpRequest.Builder::GET);
		// Re-validate session when unauthorized
	}

	@SafeVarargs
	private Document executeRequest(CookieManager cookieManager, String uri, UnaryOperator<HttpRequest.Builder>... modifiers) throws Exception {
		try (HttpClient client = createHttpClient(cookieManager)) {
			var requestBuilder = HttpRequest.newBuilder(new URI(uri));
			for (UnaryOperator<HttpRequest.Builder> modifier : modifiers) {
				requestBuilder = modifier.apply(requestBuilder);
			}
			HttpRequest request = requestBuilder.build();
			HttpResponse<String> send = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
			String response = send.body();
			Document document = Jsoup.parse(response);

			if (!document.select("#tl_login_216").isEmpty()) {
				log.error("Anmeldung fehlgeschlagen");
				return null;
			}

			// TODO unauthorized, login again

			return document;
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

	private String makeTournamentSelectionPath() {
		return String.format(COMPETITION_SELECTION_PATH_TEMPLATE, Objects.equals(environment, HOT_LIVE) ? ".html" : "");
	}

	private void updateCompetition(Document doc, Competition comp) {
		if (comp.getName() == null || comp.getName().isEmpty()) {
			writeCompetitionTitle(doc, comp);
		}
		Elements tableRows = Objects.requireNonNull(doc.selectFirst("tBody"), "Table Body not found!")
				.select("tr");

		// TODO: find current comp object
		Category category = generateCategory(comp);

		tableRows.forEach(e -> {
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
			// Index 3 = "Beste Vorrunde"
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

	private String getPageTitle(Document doc) {
		return Objects.requireNonNull(doc.selectFirst(".page-title"), "Could not find Title Element!" + System.lineSeparator() + doc)
				.text();
	}

	private void writeCompetitionTitle(Document doc, Competition competition) {
		String competitionTitle = getPageTitle(doc);
		String[] split = competitionTitle.split(":");
		if (split.length > 1) {
			String title = split[1].strip();
			competition.setName(title);
		}
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

	private Category testroundPairings(Document document) throws Exception {
		Category category = new Category();

		Element collapseSet = document.selectFirst(".collaps-set");
		var testroundLi = collapseSet.selectFirst("li");

		String title = testroundLi.selectFirst("h2").text();
		category.setName(title);

		var matches = testroundLi.select(".match");

		Set<Team> teams = Collections.synchronizedSet(new HashSet<>());
		List<CompletableFuture<Void>> teamFutures = new ArrayList<>();
		for (Element match : matches) {
			var a = match.select("a");
			if (a.size() == 1) {
				teamFutures.add(requestTestroundScore(a.getFirst(), teams));
			} else {
				var teamLink1 = a.get(0);
				var teamLink2 = a.get(1);

				teamFutures.add(requestTestroundScore(teamLink1, teams));
				teamFutures.add(requestTestroundScore(teamLink2, teams));
			}
		}

		CompletableFuture.allOf(teamFutures.toArray(new CompletableFuture[0])).join();
		category.setTeams(teams);
		return category;
	}

	private CompletableFuture<Void> requestTestroundScore(Element teamLink, Set<Team> teams) {
		String teamName = extractTeamnameFromTestround(teamLink);
		log.info("Requesting detailed evaluation form for testround of team: {}", teamName);
		return CompletableFuture.supplyAsync(() -> {
					try {
						return getTestroundScore(extractLink(teamLink), teamName, username);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				})
				.thenAccept(teams::add)
				.exceptionally(e -> {;
					log.error("Failed to request testround score for team: " + teamName, e);
					return null;
				});
	}

	private String extractTeamnameFromTestround(Element a) {
		String teamName = a.text();
		return teamName.substring(0, teamName.lastIndexOf('[')).strip();
	}

	private void updatePairings(Document doc, Competition competition) {
		Element collapseSet = doc.selectFirst(".collaps-set");
		var preliminaryPairings = Objects.requireNonNull(collapseSet, "Could not find preliminary round containers!")
				.select(".toggle-preliminaryround");
		for (int i = 0; i < 3; i++) {
			var pairings = preliminaryPairings.get(i).select(".match");
			pairings.forEach(e -> {
				var a = e.select("a");
				if (a.size() == 1) {
					// FREE SLOT DETECTED
					System.out.println(a.getFirst().text()
							+ " ||||| "
							+ "FREIER SLOT (HAT KEINE PKTE)");
					System.out.println(extractLink(a.getFirst()));
				} else {
					var teamLink1 = a.get(0);
					var teamLink2 = a.get(1);

					System.out.println(teamLink1.text() + " ||||| " + teamLink2.text());
					System.out.println(extractLink(teamLink1) + " ||||| " + extractLink(teamLink2));
				}
			});
		}
		// Test code for single match analysis tools
		Elements allPairingsAllRounds = preliminaryPairings.select("a");
		var pairing = allPairingsAllRounds.get(ThreadLocalRandom.current().nextInt(allPairingsAllRounds.size()));
		// checkMatch(extractLink(pairing), competition);
		// System.out.println("DONE");
		// TODO extract data from eval sheets
	}

	private Team getTestroundScore(String path, String teamName, String username) throws Exception {
		Team team = new Team();
		Document doc = requestPageAfterLogin(cookieManagers.get(username), environment + "/" + path);

		team.setName(teamName);

		var finalScore = doc.selectFirst("#finalScore");
		String score = finalScore.val();

		Score teamScore = new Score(Integer.parseInt(score), -1);
		team.getScores().add(teamScore);

		return team;
	}

	private void checkMatch(String path, Competition competition, String username) throws Exception {
		Document core = requestPageAfterLogin(cookieManagers.get(username), environment + "/" + path);

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

	public static void main(String[] args) throws Exception {
		local = LOCAL_DE;
		var parser = new FLLRobotGameParser(null);
		parser.environment = HOT_LIVE;

		parser.username = args[0];
		parser.password = args[1];

		// Competition competition = parser.parse(null, 231, args[0], args[1]);
		var res = parser.getOwnCompetitionIds(args[0], args[1]);
		System.out.println("Available Competitions: " + res);
		/* Set<Competition> collect = res.stream().mapToInt(r -> Integer.parseInt(r)) .mapToObj(i -> parser.parse(null, i, args[0], args[1])) .collect(Collectors.toSet()) System.out.println(collect); */
		var testRound = parser.parseTestRound(353, args[0], args[1]);
		System.out.println("DONE");
	}
}
