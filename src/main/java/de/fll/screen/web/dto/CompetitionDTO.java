package de.fll.screen.web.dto;

import de.fll.screen.model.Category;
import de.fll.screen.model.Competition;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record CompetitionDTO(long internalId, String name, Set<CategoryDTO> categories) {
	public record CategoryDTO(String name, List<TeamDTO> teams) {
		public static CategoryDTO of(Category category) {
			// Sort the teams according to the category scoring
			var teams = category.getCategoryScoring().assignRanks(category.getTeams());
			return new CategoryDTO(category.getName(), teams);
		}
	}

	public static CompetitionDTO of(Competition competition) {
		return new CompetitionDTO(competition.getInternalId(), competition.getName(), competition.getCategories().stream().map(CategoryDTO::of).collect(Collectors.toSet()));
	}
}
