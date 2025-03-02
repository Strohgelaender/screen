package de.fll.screen.service.comparators;

import de.fll.screen.model.Team;
import de.fll.screen.web.dto.TeamDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

abstract class AbstractWROComparator implements CategoryComparator {

	// Use the comparator to assign ranks to the teams
	// The same rank is only assigned if the comparator returns 0
	public List<TeamDTO> assignRanks(Set<Team> teams) {
		List<Team> sorted = new ArrayList<>(teams);
		sorted.sort(this);
		List<TeamDTO> teamDTOs = new ArrayList<>(teams.size());

		int rank = 0;
		for (int i = 0; i < sorted.size(); i++) {
			Team team = sorted.get(i);
			var highlightIndices = getHighlightIndices(team);
			if (i == 0 || compare(sorted.get(i - 1), team) != 0) {
				// New rank
				rank = i + 1;
			}
			if (rank != 1 && team.getScores().stream().noneMatch(score -> score.getPoints() > 0)) {
				// Skip teams with 0 points
				// Except when everyone has 0 points (all rank 1)
				continue;
			}
			teamDTOs.add(TeamDTO.of(team, rank, highlightIndices));
		}

		return teamDTOs;
	}
}
