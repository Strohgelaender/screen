package de.fll.screen.model;

import de.fll.screen.repository.ScoreDetailsConverter;
import jakarta.persistence.*;

import java.util.Arrays;
import java.util.stream.Collectors;

@Entity
@Table(name = "score_details")
public class ScoreDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private long id;

	@Convert(converter = ScoreDetailsConverter.class)
	@Lob
	@Column(name = "detailed_scores")
	private int[][] detailedScores;

	public ScoreDetails() {
		// Empty constructor for JPA
	}

	public ScoreDetails(int[][] detailedScores) {
		this.detailedScores = detailedScores;
	}

	public int[] getDetailsForTask(int taskNumber) {
		return detailedScores[taskNumber];
	}

	public int getScoreForTask(int taskNumber) {
		return detailedScores[taskNumber][0];
	}

	public void setDetailedScores(int[][] detailedScores) {
		this.detailedScores = detailedScores;
	}

	@Override
	public String toString() {
		return "ScoreDetails{" +
				"detailedScores=[" + Arrays.stream(detailedScores).map(Arrays::toString).collect(Collectors.joining(", ")) +
				"]}";
	}
}
