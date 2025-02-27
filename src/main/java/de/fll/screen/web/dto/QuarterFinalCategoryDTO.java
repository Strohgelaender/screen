package de.fll.screen.web.dto;

import de.fll.screen.model.Category;
import de.fll.screen.model.CategoryScoring;

import java.util.List;

public record QuarterFinalCategoryDTO(String name, List<TeamDTO> teams) {

	public static QuarterFinalCategoryDTO of(Category category) {
		// Sort the teams according to their quarter final scores
		var teams = CategoryScoring.FLL_QUARTER_FINAL.assignRanks(category.getTeams());
		return new QuarterFinalCategoryDTO(category.getName(), teams);
	}
}
