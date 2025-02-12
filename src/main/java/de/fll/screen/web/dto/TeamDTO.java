package de.fll.screen.web.dto;

import de.fll.screen.model.Score;
import de.fll.screen.model.Team;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public record TeamDTO(long id, String name, List<ScoreDTO> scores, int rank) {

	public record ScoreDTO(double points, int time, boolean highlight) {
		public static ScoreDTO of(Score score, boolean highlight) {
			return new ScoreDTO(score.getPoints(), score.getTime(), highlight);
		}
	}

	public static TeamDTO of(Team team, int rank, Set<Integer> highlightIndices) {
		List<ScoreDTO> scores = new ArrayList<>(team.getScores().size());
		for (int i = 0; i < team.getScores().size(); i++) {
			scores.add(ScoreDTO.of(team.getScores().get(i), highlightIndices.contains(i)));
		}
		return new TeamDTO(team.getId(), team.getName(), scores, rank);
	}
}
