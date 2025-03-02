package de.fll.screen.web.dto;

import de.fll.screen.model.Category;
import de.fll.screen.model.CategoryScoring;

import java.util.List;

public record TestroundCategoryDTO(String name, List<TeamDTO> teams) {
	public static TestroundCategoryDTO of(Category category) {
		// Sort the teams according to their quarter final scores
		var teams = CategoryScoring.FLL_TESTROUND.assignRanks(category.getTeams());
		return new TestroundCategoryDTO(category.getName(), teams);
	}
}
