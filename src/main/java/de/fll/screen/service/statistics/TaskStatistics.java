package de.fll.screen.service.statistics;

import java.util.Arrays;
import java.util.stream.Stream;

public class TaskStatistics extends ArrayStatistics<TaskStatistics> {

	private final double[] maxPerEntry;
	private final double[][] subTaskData;

	public TaskStatistics(double[] maxPerEntry, double[][] subTaskData) {
		super(maxPerEntry[0], Arrays.stream(subTaskData).mapToDouble(a -> a[0]).toArray());
		this.subTaskData = subTaskData;
		this.maxPerEntry = maxPerEntry;
	}

	public SimpleArrayStatistics getSubTaskStatistics(int subTaskIndex) {
		return new SimpleArrayStatistics(maxPerEntry[subTaskIndex], Arrays.stream(subTaskData)
				.mapToDouble(a -> a[subTaskIndex]).toArray());
	}

	@Override
	public TaskStatistics merge(TaskStatistics... other) {
		var mergedData = Stream.concat(Stream.of(this), Arrays.stream(other))
				.flatMap(s -> Arrays.stream(s.subTaskData))
				.toArray(double[][]::new);

		return new TaskStatistics(maxPerEntry, mergedData);
	}
}
