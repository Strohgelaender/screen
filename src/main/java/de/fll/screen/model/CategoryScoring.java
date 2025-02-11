package de.fll.screen.model;

import de.fll.screen.model.comparators.CategoryComparator;
import de.fll.screen.model.comparators.FLLRobotGameComparator;
import de.fll.screen.model.comparators.WRO2025Comparator;
import de.fll.screen.model.comparators.WROStarterComparator;
import de.fll.screen.web.dto.TeamDTO;

import java.util.List;
import java.util.Set;

public enum CategoryScoring implements CategoryComparator {
	FLL_ROBOT_GAME(new FLLRobotGameComparator()), WRO_STARTER(new WROStarterComparator()), WRO_ROBOMISSION_2025(new WRO2025Comparator());

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
