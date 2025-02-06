package de.fll.screen.service.statistics;

import java.util.Arrays;
import java.util.stream.Stream;

public interface Statistics<T extends Statistics<T>> {

	/**
	 * @return median of the underlying data set
	 */
	double getMedian();

	/**
	 * @return average of the underlying data set
	 */
	double getAverage();

	/**
	 * @return Sum of the underlying data set
	 */
	double getTotal();

	/**
	 * @return theoretically achievable maximum of the underlying data set
	 */
	double getReachableTotal();

	/**
	 * @return number of entries in the data set
	 */
	int size();

	/**
	 * Combines the data of the two sets to a bigger set.
	 *
	 * @param other
	 *            the set to combine the data with
	 * @return a new data set with backed by copies of the old data set
	 */
	T merge(T... other);

	/**
	 * @return the percentage reached of the possible
	 */
	default double getTotalPercentage() {
		return getTotal() / getReachableTotal();
	}

	// Internal Helper. Probably move to package private static helper method
	default IllegalStateException callOnEmptyDatasetException() {
		return new IllegalStateException("Cannot calculate on empty dataset");
	}

	@SafeVarargs
	static <T> Stream<T> combineOneAndMore(T one, T... more) {
		return Stream.concat(Stream.of(one), Arrays.stream(more));
	}

}
