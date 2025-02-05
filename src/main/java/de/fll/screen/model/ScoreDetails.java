package de.fll.screen.model;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ScoreDetails {

	final int[][] detailedScores;

	public ScoreDetails(int[][] detailedScores) {
		this.detailedScores = detailedScores;
	}

	public int[] getDetailsForTask(int taskNumber) {
		return detailedScores[taskNumber];
	}

	public int getScoreForTask(int taskNumber) {
		return detailedScores[taskNumber][0];
	}

	@Override
	public String toString() {
		return "ScoreDetails{" +
				"detailedScores=[" + Arrays.stream(detailedScores).map(Arrays::toString).collect(Collectors.joining(", ")) +
				"]}";
	}
}
