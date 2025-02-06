package de.fll.screen.service.statistics;

import java.util.Arrays;
import java.util.function.DoubleUnaryOperator;

public class WrappedArrayStatistics extends ArrayStatistics<WrappedArrayStatistics> {

	public WrappedArrayStatistics(ArrayStatistics<?> base, DoubleUnaryOperator dataMapper) {
		super(dataMapper.applyAsDouble(base.maxPerEntry), Arrays.stream(base.dataSet).map(dataMapper).toArray());
	}

	private WrappedArrayStatistics(double maxPerEntry, double[] dataSet) {
		super(maxPerEntry, dataSet);
	}

	@Override
	public WrappedArrayStatistics merge(WrappedArrayStatistics... other) {
		return new WrappedArrayStatistics(maxPerEntry,
				Statistics.combineOneAndMore(this, other)
						.flatMapToDouble(s -> Arrays.stream(s.dataSet)).toArray());
	}
}
