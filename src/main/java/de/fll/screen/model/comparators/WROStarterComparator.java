package de.fll.screen.model.comparators;

import de.fll.screen.model.Score;
import de.fll.screen.model.Team;
import java.util.Comparator;

public class WROStarterComparator implements CategoryComparator {

	@Override
	public int compare(Team team1, Team team2) {
		var t1Scores = team1.getScores();
		var t2Scores = team2.getScores();

		t1Scores.sort(Comparator.comparing(Score::getPoints).reversed());
		t2Scores.sort(Comparator.comparing(Score::getPoints).reversed());

		for (int i = 0; i < t1Scores.size(); i++) {
			int compareWithTime = t1Scores.get(i).compareToWithTime(t2Scores.get(i));
			if (compareWithTime != 0) {
				return compareWithTime;
			}
		}
		return 0;
	}
}
