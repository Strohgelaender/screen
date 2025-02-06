package de.fll.screen.service.statistics;

import de.fll.screen.model.ScoreDetails;

import java.util.Arrays;
import java.util.function.IntFunction;
import java.util.stream.Stream;

public class RunStatistics extends ArrayStatistics<RunStatistics> {

	private final ScoreDetails[] scoreDetails;
	private final IntFunction<double[]> subTaskMaxScores;

	public RunStatistics(double maxReachable, IntFunction<double[]> subTaskMaxScores, ScoreDetails... scoreDetails) {
		super(maxReachable, Arrays.stream(scoreDetails).map(ScoreDetails::getDetailedScores)
				.mapToDouble(array -> Arrays.stream(array).mapToDouble(s -> s[0]).sum()).toArray());
		this.subTaskMaxScores = subTaskMaxScores;
		this.scoreDetails = scoreDetails;
	}

	public TaskStatistics createTaskStatisticsById(int taskId) {
		return new TaskStatistics(subTaskMaxScores.apply(taskId), Arrays.stream(scoreDetails)
				.map(ScoreDetails::getDetailedScores)
				.map(a -> Arrays.stream(a[taskId]).mapToDouble(i -> i).toArray())
				.toArray(double[][]::new));
	}

	@Override
	public RunStatistics merge(RunStatistics... other) {
		var mergedData = Stream.concat(Stream.of(this), Arrays.stream(other))
				.flatMap(ts -> Arrays.stream(ts.scoreDetails))
				.toArray(ScoreDetails[]::new);
		return new RunStatistics(maxPerEntry, subTaskMaxScores, mergedData);
	}

	public double getMaxPerRound() {
		return maxPerEntry;
	}

}
