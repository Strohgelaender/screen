package de.fll.screen.service.comparators;

import de.fll.screen.model.Score;
import de.fll.screen.model.Team;
import de.fll.screen.web.dto.TeamDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

abstract class AbstractFLLComparator implements CategoryComparator {

	protected abstract List<Score> getRelevantScores(Team scores);

	protected List<TeamDTO> assignRanks(Set<Team> teams, Function<Team, Score> scoreExtractor) {
		List<Team> sorted = new ArrayList<>(teams);
		sorted.sort(this);
		List<TeamDTO> teamDTOs = new ArrayList<>(teams.size());

		double previousScore = -1;
		int rank = 0;
		for (int i = 0; i < sorted.size(); i++) {
			Team team = sorted.get(i);
			Score score = scoreExtractor.apply(team);
			if (score.getPoints() != previousScore) {
				rank = i + 1;
			}

			var highlightIndices = getHighlightIndices(team);

			var scores = getRelevantScores(team).stream()
					.map(s -> new TeamDTO.ScoreDTO(s.getPoints(), s.getTime(), highlightIndices.contains(team.getScores().indexOf(score))))
					.toList();
					;
			teamDTOs.add(new TeamDTO(team.getId(), team.getName(), scores, rank));
			previousScore = score.getPoints();
		}
		return teamDTOs;
	}
}
