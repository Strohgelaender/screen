package de.fll.screen.service.comparators;

import de.fll.screen.model.Score;
import de.fll.screen.model.Team;
import de.fll.screen.web.dto.TeamDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

abstract class AbstractFLLComparator implements CategoryComparator {

	protected abstract List<Score> getRelevantScores(Team team);

	protected List<TeamDTO> assignRanks(Set<Team> teams, Function<Team, Score> scoreExtractor) {
		List<Team> sorted = new ArrayList<>(teams);
		sorted.sort(this);
		List<TeamDTO> teamDTOs = new ArrayList<>(teams.size());

		double previousScore = -1;
		int rank = 0;
		for (int i = 0; i < sorted.size(); i++) {
			Team team = sorted.get(i);
			Score bestScore = scoreExtractor.apply(team);
			if (bestScore.getPoints() != previousScore) {
				rank = i + 1;
			}

			var highlightIndices = getHighlightIndices(team);

			var scores = getRelevantScores(team).stream()
					.map(score -> TeamDTO.ScoreDTO.of(score, highlightIndices.contains(team.getScores().indexOf(score))))
					.toList();
			teamDTOs.add(new TeamDTO(team.getId(), team.getName(), scores, rank));
			previousScore = bestScore.getPoints();
		}
		return teamDTOs;
	}

	protected int compareOneScore(Team t1, Team t2, int roundIndex) {
		var s1 = t1.getScoreForRound(roundIndex);
		var s2 = t2.getScoreForRound(roundIndex);
		if (s1 == null && s2 == null) {
			return 0;
		} else if (s1 == null) {
			return 1;
		} else if (s2 == null) {
			return -1;
		}
		return -s1.compareToWithTime(s2);
	}
}
