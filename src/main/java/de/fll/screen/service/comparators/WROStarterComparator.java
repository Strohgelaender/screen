package de.fll.screen.service.comparators;

import de.fll.screen.model.Score;
import de.fll.screen.model.Team;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class WROStarterComparator extends AbstractWROComparator {

	@Override
	public int compare(Team team1, Team team2) {
		var t1Scores = new ArrayList<>(team1.getScores());
		var t2Scores = new ArrayList<>(team2.getScores());

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

	@Override
	public Set<Integer> getHighlightIndices(Team team) {
		List<Score> scores = new ArrayList<>(team.getScores());
		scores.sort(Comparator.comparing(Score::getPoints).reversed());

		for (int i = 0; i < scores.size(); i++) {
			if (team.getScores().get(i).equals(scores.getFirst())) {
				return Set.of(i);
			}
		}
		return Set.of();
	}
}
