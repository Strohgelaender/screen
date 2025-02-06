package de.fll.screen.service.statistics;

import java.util.Arrays;
import java.util.stream.Stream;

public class SimpleArrayStatistics extends ArrayStatistics<SimpleArrayStatistics> {

	public SimpleArrayStatistics(double maxPerEntry, double[] dataSet) {
		super(maxPerEntry, dataSet);
	}

	@Override
	public SimpleArrayStatistics merge(SimpleArrayStatistics... other) {
		var mergedData = Stream.concat(Stream.of(this), Arrays.stream(other))
				.flatMapToDouble(o -> Arrays.stream(o.dataSet))
				.toArray();
		return new SimpleArrayStatistics(maxPerEntry, mergedData);
	}
}
