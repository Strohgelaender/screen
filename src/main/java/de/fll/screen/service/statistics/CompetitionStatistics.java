package de.fll.screen.service.statistics;

import de.fll.screen.model.Team;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class CompetitionStatistics implements Statistics<CompetitionStatistics> {

	// Does this even make sense? Why not just be backed by RunStatistics?
	private final Map<Team, RunStatistics> teamDetails;

	private RunStatistics allMerged;

	public CompetitionStatistics(Map<Team, RunStatistics> teamDetails) {
		this.teamDetails = teamDetails;
	}

	public RunStatistics getTotalRunStatistics() {
		return null; // TODO: lazy and back by that merged object?
	}

	@Override
	public double getMedian() {
		long size = teamDetails.values().stream().mapToInt(ArrayStatistics::size).sum();
		return teamDetails.values().stream()
				.flatMapToDouble(ts -> Arrays.stream(ts.dataSet))
				.sorted()
				.skip(size / 2)
				.findFirst()
				.orElseThrow(this::callOnEmptyDatasetException);
	}

	@Override
	public double getAverage() {
		return teamDetails.values().stream()
				.flatMapToDouble(ts -> Arrays.stream(ts.dataSet))
				.average()
				.orElseThrow(this::callOnEmptyDatasetException);
	}

	@Override
	public double getTotal() {
		return teamDetails.values().stream()
				.flatMapToDouble(ts -> Arrays.stream(ts.dataSet))
				.sum();
	}

	@Override
	public double getReachableTotal() {
		return teamDetails.values().stream().findAny().orElseThrow(this::callOnEmptyDatasetException).getMaxPerRound() * size();
	}

	@Override
	public int size() {
		return teamDetails.values().stream().mapToInt(ArrayStatistics::size).sum();
	}

	@Override
	public CompetitionStatistics merge(CompetitionStatistics... other) {
		Map<Team, RunStatistics> mergedData = Statistics.combineOneAndMore(this, other)
				.map(cs -> cs.teamDetails)
				.flatMap(m -> m.entrySet().stream())
				.collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue,
						Collectors.collectingAndThen(Collectors.reducing(RunStatistics::merge), Optional::orElseThrow))));
		return new CompetitionStatistics(mergedData);
	}
}
