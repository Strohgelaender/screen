package de.fll.screen.model.comparators;

import de.fll.screen.model.Score;
import de.fll.screen.model.Team;
import de.fll.screen.web.dto.TeamDTO;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class FLLRobotGameComparator implements CategoryComparator {

	@Override
	public int compare(Team team1, Team team2) {
		var t1Scores = new ArrayList<>(getRelevantScores(team1.getScores()));
		var t2Scores = new ArrayList<>(getRelevantScores(team2.getScores()));

		t1Scores.sort(Comparator.comparing(Score::getPoints).reversed());
		t2Scores.sort(Comparator.comparing(Score::getPoints).reversed());

		for (int i = 0; i < t1Scores.size(); i++) {
			if (t1Scores.get(i).getPoints() != t2Scores.get(i).getPoints()) {
				return Double.compare(t2Scores.get(i).getPoints(), t1Scores.get(i).getPoints());
			}
		}

		return 0;
	}

	private List<Score> getRelevantScores(List<Score> scores) {
		return scores.subList(0, 3);
	}

	@Override
	public Set<Integer> getHighlightIndices(Team team) {
		var scores = getRelevantScores(team.getScores());
		double bestScore = getBestScore(team);
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

		List<Team> sorted = new ArrayList<>(teams);
		sorted.sort(this);
		List<TeamDTO> teamDTOs = new ArrayList<>(teams.size());
		int rank = 0;
		double previousScore = -1;

		for (var team : sorted) {
			double bestScore = getBestScore(team);
			var highlightIndices = getHighlightIndices(team);
			if (bestScore == previousScore) {
				// Assign the same rank if the score is the same
				teamDTOs.add(TeamDTO.of(team, rank, highlightIndices));
			} else {
				// Otherwise, assign a new rank
				rank = rank + 1;
				teamDTOs.add(TeamDTO.of(team, rank, highlightIndices));
			}
			previousScore = bestScore;
		}

		return teamDTOs;
	}

	private double getBestScore(Team team) {
		return getRelevantScores(team.getScores()).stream().mapToDouble(Score::getPoints).max().orElse(-1);
	}
}
