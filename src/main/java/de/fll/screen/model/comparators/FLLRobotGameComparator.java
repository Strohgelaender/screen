package de.fll.screen.model.comparators;

import de.fll.screen.model.Score;
import de.fll.screen.model.Team;
import java.util.Comparator;
import java.util.List;

public class FLLRobotGameComparator implements Comparator<Team> {

	@Override
	public int compare(Team team1, Team team2) {
		var t1Scores = getRelevantScores(team1.getScores());
		var t2Scores = getRelevantScores(team2.getScores());

		t1Scores.sort(Comparator.comparing(Score::getPoints).reversed());
		t2Scores.sort(Comparator.comparing(Score::getPoints).reversed());

		for (int i = 0; i < t1Scores.size(); i++) {
			if (t1Scores.get(i).getPoints() != t2Scores.get(i).getPoints()) {
				return Double.compare(t2Scores.get(i).getPoints(), t1Scores.get(i).getPoints());
			}
		}

		return 0;
	}

	private List<Score> getRelevantScores(List<Score> scores) {
		return scores.subList(0, 3);
	}
}
