package de.fll.screen.model.comparators;

import de.fll.screen.model.Score;
import de.fll.screen.model.Team;
import de.fll.screen.web.dto.TeamDTO;

import java.util.*;

public class FLLQuarterFinalComparator implements CategoryComparator {

	// 0, 1, 2: regular rounds; index 3: quarter final
	private static final int QUARTER_FINAL_ROUND_INDEX = 3;

	@Override
	public Set<Integer> getHighlightIndices(Team team) {
		// Only one score for the quarter final, don't highlight anything
		return Collections.emptySet();
	}

	@Override
	public int compare(Team t1, Team t2) {
		var s1 = t1.getScoreForRound(QUARTER_FINAL_ROUND_INDEX);
		var s2 = t2.getScoreForRound(QUARTER_FINAL_ROUND_INDEX);
		if (s1 == null && s2 == null) {
			return 0;
		} else if (s1 == null) {
			return 1;
		} else if (s2 == null) {
			return -1;
		}
		return -s1.compareToWithTime(s2);
	}

	@Override
	public List<TeamDTO> assignRanks(Set<Team> teams) {
		List<Team> sorted = teams.stream()
				.sorted(this)
				.filter(team -> team.getScoreForRound(QUARTER_FINAL_ROUND_INDEX) != null)
				.limit(8)
				.toList();
		List<TeamDTO> teamDTOs = new ArrayList<>(sorted.size());

		double previousScore = -1;
		int rank = 0;
		for (int i = 0; i < sorted.size(); i++) {
			Team team = sorted.get(i);
			Score qfScore = team.getScoreForRound(QUARTER_FINAL_ROUND_INDEX);
			if (qfScore.getPoints() != previousScore) {
				rank = i + 1;
			}

			var scores = Collections.singletonList(new TeamDTO.ScoreDTO(qfScore.getPoints(), qfScore.getTime(), false));
			teamDTOs.add(new TeamDTO(team.getId(), team.getName(), scores, rank));
			previousScore = qfScore.getPoints();
		}

		return teamDTOs;
	}
}
