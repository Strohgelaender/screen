package de.fll.screen.service.comparators;

import de.fll.screen.model.Score;
import de.fll.screen.model.Team;
import de.fll.screen.web.dto.TeamDTO;

import java.util.List;
import java.util.Set;

public class FLLTestRoundComparator extends AbstractFLLComparator {
	@Override
	protected List<Score> getRelevantScores(Team team) {
		// TODO tesrounds not part of the normal scores model (yet), but extra team object with only one score so far
		// We should change this
		return team.getScores();
	}

	@Override
	public Set<Integer> getHighlightIndices(Team team) {
		// No highlighting for test rounds
		return Set.of();
	}

	@Override
	public List<TeamDTO> assignRanks(Set<Team> teams) {
		return assignRanks(teams, team -> team.getScores().isEmpty() ? null : team.getScores().getFirst());
	}

	@Override
	public int compare(Team o1, Team o2) {
		return compareOneScore(o1, o2, 0);
	}
}
