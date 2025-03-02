package de.fll.screen.service.comparators;

import de.fll.screen.model.Score;
import de.fll.screen.model.Team;
import de.fll.screen.web.dto.TeamDTO;

import java.util.*;

public class FLLRobotGameComparator extends AbstractFLLComparator {

	@Override
	public int compare(Team team1, Team team2) {
		var t1Scores = new ArrayList<>(getRelevantScores(team1));
		var t2Scores = new ArrayList<>(getRelevantScores(team2));

		t1Scores.sort(Comparator.comparing(Score::getPoints).reversed());
		t2Scores.sort(Comparator.comparing(Score::getPoints).reversed());

		for (int i = 0; i < t1Scores.size(); i++) {
			if (t1Scores.get(i).getPoints() != t2Scores.get(i).getPoints()) {
				return Double.compare(t2Scores.get(i).getPoints(), t1Scores.get(i).getPoints());
			}
		}

		return 0;
	}

	protected List<Score> getRelevantScores(Team team) {
		return team.getScores().subList(0, 3);
	}

	@Override
	public Set<Integer> getHighlightIndices(Team team) {
		var scores = getRelevantScores(team);
		double bestScore = getBestScore(team);
		if (bestScore == 0) {
			return Collections.emptySet();
		}
		// Highlight first occurrence of best score
		for (int i = 0; i < scores.size(); i++) {
			if (scores.get(i).getPoints() == bestScore) {
				return Set.of(i);
			}
		}
		return Set.of();
	}

	@Override
	public List<TeamDTO> assignRanks(Set<Team> teams) {
		// For the FLL, ranks are only determined by the best score
		// We cannot use the comparator for this since it uses all scores, so this compares only the best score.
		return assignRanks(teams, team -> getRelevantScores(team).stream().max(Comparator.comparing(Score::getPoints)).orElse(null));
	}

	private double getBestScore(Team team) {
		return getRelevantScores(team).stream().mapToDouble(Score::getPoints).max().orElse(-1);
	}
}
