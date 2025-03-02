package de.fll.screen.model;

import de.fll.screen.service.comparators.*;
import de.fll.screen.web.dto.TeamDTO;

import java.util.List;
import java.util.Set;

public enum CategoryScoring implements CategoryComparator {
	FLL_ROBOT_GAME(new FLLRobotGameComparator()),
	FLL_QUARTER_FINAL(new FLLQuarterFinalComparator()),
	FLL_TESTROUND(new FLLTestRoundComparator()),
	WRO_STARTER(new WROStarterComparator()),
	WRO_ROBOMISSION_2025(new WRO2025Comparator());

	private final CategoryComparator categoryComparator;

	CategoryScoring(CategoryComparator compareFunction) {
		this.categoryComparator = compareFunction;
	}

	@Override
	public int compare(Team o1, Team o2) {
		return categoryComparator.compare(o1, o2);
	}

	@Override
	public List<TeamDTO> assignRanks(Set<Team> teams) {
		return this.categoryComparator.assignRanks(teams);
	}

	@Override
	public Set<Integer> getHighlightIndices(Team team) {
		return this.categoryComparator.getHighlightIndices(team);
	}
}
