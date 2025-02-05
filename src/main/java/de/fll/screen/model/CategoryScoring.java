package de.fll.screen.model;

import de.fll.screen.model.comparators.FLLRobotGameComparator;
import de.fll.screen.model.comparators.WRO2025Comparator;
import de.fll.screen.model.comparators.WROStarterComparator;
import java.util.Comparator;

public enum CategoryScoring implements Comparator<Team> {
	FLL_ROBOT_GAME(new FLLRobotGameComparator()), WRO_STARTER(new WROStarterComparator()), WRO_ROBOMISSION_2025(new WRO2025Comparator());

	private final Comparator<Team> compareFunction;

	CategoryScoring(Comparator<Team> compareFunction) {
		this.compareFunction = compareFunction;
	}

	@Override
	public int compare(Team o1, Team o2) {
		return compareFunction.compare(o1, o2);
	}
}
