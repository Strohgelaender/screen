package de.fll.screen.repository;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Arrays;
import java.util.stream.Collectors;

@Converter
public class ScoreDetailsConverter implements AttributeConverter<int[][], String> {

	@Override
	public String convertToDatabaseColumn(int[][] attribute) {
		if (attribute == null) {
			return null;
		}
		return Arrays.stream(attribute)
				.map(row -> Arrays.stream(row)
						.mapToObj(String::valueOf)
						.collect(Collectors.joining(",")))
				.collect(Collectors.joining(";"));
	}

	@Override
	public int[][] convertToEntityAttribute(String dbData) {
		if (dbData == null || dbData.isEmpty()) {
			return new int[0][0];
		}
		return Arrays.stream(dbData.split(";"))
				.map(row -> Arrays.stream(row.split(","))
						.mapToInt(Integer::parseInt)
						.toArray())
				.toArray(int[][]::new);
	}
}
