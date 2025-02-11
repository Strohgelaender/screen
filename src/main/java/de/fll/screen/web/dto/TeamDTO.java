package de.fll.screen.web.dto;

import de.fll.screen.model.Score;
import de.fll.screen.model.Team;

import java.util.List;

public record TeamDTO(long id, String name, List<ScoreDTO> scores, int rank) {
	public static TeamDTO of(Team team, int rank) {
		return new TeamDTO(team.getId(), team.getName(), team.getScores().stream().map(ScoreDTO::of).toList(), rank);
	}

	public record ScoreDTO(double points, int time) {
		public static ScoreDTO of(Score score) {
			return new ScoreDTO(score.getPoints(), score.getTime());
		}
	}
}
