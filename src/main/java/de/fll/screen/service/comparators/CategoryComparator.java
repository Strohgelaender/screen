package de.fll.screen.service.comparators;

import de.fll.screen.model.Team;
import de.fll.screen.web.dto.TeamDTO;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

public interface CategoryComparator extends Comparator<Team> {

	Set<Integer> getHighlightIndices(Team team);

	/**
	 * Use the comparator to assign ranks to the teams and convert to TeamDTOs
	 */
	List<TeamDTO> assignRanks(Set<Team> teams);

}
