package de.fll.screen.service.comparators;

import de.fll.screen.model.Score;
import de.fll.screen.model.Team;

import java.util.HashSet;
import java.util.Set;

public class WRO2025Comparator extends AbstractWROComparator {

	@Override
	public int compare(Team t1, Team t2) {
		var t1Morning = getBestRoundMorning(t1);
		var t2Morning = getBestRoundMorning(t2);

		var t1Afternoon = getBestRoundAfternoon(t1);
		var t2Afternoon = getBestRoundAfternoon(t2);

		Score t1Points = t1Morning.add(t1Afternoon);
		Score t2Points = t2Morning.add(t2Afternoon);

		int resultBoth = t1Points.compareToWithTime(t2Points);
		if (resultBoth != 0) {
			return resultBoth;
		}

		return t1Morning.compareToWithTime(t2Morning);
	}

	private Score getBestRoundMorning(Team team) {
		var s1 = team.getScores().get(0);
		var s2 = team.getScores().get(1);
		return getBetterRound(s1, s2);
	}

	private Score getBetterRound(Score s1, Score s2) {
		if (s1.getPoints() > s2.getPoints()) {
			return s1;
		} else if (s1.getPoints() < s2.getPoints()) {
			return s2;
		} else {
			return s1.getTime() < s2.getTime() ? s1 : s2;
		}
	}

	private Score getBestRoundAfternoon(Team team) {
		return getBetterRound(team.getScores().get(2), team.getScores().get(3));
	}

	@Override
	public Set<Integer> getHighlightIndices(Team team) {
		Score bestMorning = getBestRoundMorning(team);
		Score bestAfternoon = getBestRoundAfternoon(team);

		Set<Integer> highlightIndices = new HashSet<>();
		for (int i = 0; i < team.getScores().size(); i++) {
			var score = team.getScores().get(i);
			if (score.equals(bestMorning) || score.equals(bestAfternoon)) {
				highlightIndices.add(i);
			}
		}
		return highlightIndices;
	}
}
