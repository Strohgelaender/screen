package de.fll.screen.service.comparators;

import de.fll.screen.model.Score;
import de.fll.screen.model.Team;
import de.fll.screen.web.dto.TeamDTO;

import java.util.*;

public class FLLQuarterFinalComparator extends AbstractFLLComparator {

	// 0, 1, 2: regular rounds; index 3: quarter final
	private static final int QUARTER_FINAL_ROUND_INDEX = 3;

	@Override
	public Set<Integer> getHighlightIndices(Team team) {
		// Only one score for the quarter final, don't highlight anything
		return Collections.emptySet();
	}

	@Override
	public int compare(Team t1, Team t2) {
		return compareOneScore(t1, t2, QUARTER_FINAL_ROUND_INDEX);
	}

	@Override
	public List<TeamDTO> assignRanks(Set<Team> teams) {
		return assignRanks(teams, (team -> team.getScoreForRound(QUARTER_FINAL_ROUND_INDEX))).subList(0, 8);
	}

	@Override
	protected List<Score> getRelevantScores(Team team) {
		var scores = team.getScores();
		if (scores.size() <= QUARTER_FINAL_ROUND_INDEX) {
			return Collections.emptyList();
		}
		return Collections.singletonList(scores.get(QUARTER_FINAL_ROUND_INDEX));
	}
}
