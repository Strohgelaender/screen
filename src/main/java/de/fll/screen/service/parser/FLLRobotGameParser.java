package de.fll.screen.service.parser;

import de.fll.screen.model.*;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
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
	private static final String RG_PAIRING_PATH = "/tournament?tournament=";

	@Value("${fll.username}")
	private String username;

	@Value("${fll.password}")
	private String password;

	@Value("${fll.environment}")
	private String environment;

	private static String local = LOCAL_DE;

	private final Map<String, CookieManager> cookieManagers = new HashMap<>();

	public Competition parse(Competition competition, int id) {
		return parse(competition, id, username, password);
	}

	@Nonnull
	@Override
	public Competition parse(
			@Nullable Competition competition, int id, String user, String password) {
		CookieManager cookieManager;

		if (competition == null) {
			competition = new Competition();
			cookieManager = new CookieManager();
		} else {
			cookieManager = cookieManagers.computeIfAbsent(competition.getName(), k -> new CookieManager());
		}

		String rawScorePage = requestLogin(cookieManager, makeURL(LOGIN_PATH), makeURL(RG_SCORE_PATH) + id, user, password);
		if (rawScorePage == null) {
			return competition; // SOMETHING WENT WRONG WHILE GETTING DATA
		}
		updateCompetition(Jsoup.parse(rawScorePage), competition);
		// WE HATE COOKIES - save session cookie
		cookieManagers.put(competition.getName(), cookieManager);

		String rawPairingPage = requestPageAfterLogin(cookieManager, makeURL(RG_PAIRING_PATH));
		if (rawPairingPage == null) {
			return competition; // SOMETHING WENT WRONG WHILE GETTING DATA
		}
		updatePairings(Jsoup.parse(rawPairingPage), competition);

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
		Elements allPairingsAllRounds = preliminaryPairings.select("a");
		var pairing = allPairingsAllRounds.get(
				ThreadLocalRandom.current().nextInt(allPairingsAllRounds.size()));
		// checkMatch(extractLink(pairing), competition);
		// System.out.println("DONE");
		// TODO extract data from eval sheets
	}

	private void checkMatch(String path, Competition competition) {
		String match = requestPageAfterLogin(
				cookieManagers.get(competition.getName()), environment + "/" + path);
		System.out.println(match);
	}

	private String extractLink(Element element) {
		return element.attr("href");
	}

	public static void main(String[] args) {
		local = LOCAL_DE;
		Competition competition = new FLLRobotGameParser().parse(null, 231, args[0], args[1]);
		System.out.println("DONE");
	}
}
