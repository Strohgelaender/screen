package de.fll.screen.service.statistics;

import java.util.Arrays;

public abstract class ArrayStatistics<T extends ArrayStatistics<T>> implements Statistics<T> {

	protected final double maxPerEntry;
	protected final double[] dataSet;

	public ArrayStatistics(double maxPerEntry, double[] dataSet) {
		this.maxPerEntry = maxPerEntry;
		this.dataSet = dataSet;
	}

	@Override
	public double getMedian() {
		return Arrays.stream(dataSet).sorted().skip(dataSet.length / 2).findFirst()
				.orElseThrow(this::callOnEmptyDatasetException);
	}

	@Override
	public double getAverage() {
		return Arrays.stream(dataSet).average().orElseThrow(this::callOnEmptyDatasetException);
	}

	@Override
	public double getTotal() {
		return Arrays.stream(dataSet).sum();
	}

	@Override
	public double getReachableTotal() {
		return dataSet.length * maxPerEntry;
	}

	@Override
	public int size() {
		return dataSet.length;
	}

}
