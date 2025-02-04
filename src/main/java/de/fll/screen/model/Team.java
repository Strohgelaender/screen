package de.fll.screen.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Entity
public final class Team {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private long id;

	@Column(name = "name", nullable = false)
	private String name;

	@OneToMany(mappedBy = "team", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderColumn(name = "round")
	private final List<Score> scores;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "category_id")
	@JsonIgnore
	private Category category;

	public Team() {
		this("");
	}

	public Team(String name) {
		this(name, 5);
	}

	public Team(int rounds) {
		this("", rounds);
	}

	public Team(String name, int rounds) {
		this.name = name;
		this.scores = new ArrayList<>(rounds);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Score> getScores() {
		return scores;
	}

	@JsonIgnore
	@Nullable
	public Score getScoreForRound(int index) {
		if (index < 0 || index >= scores.size()) {
			return null;
		}
		return scores.get(index);
	}

	@Override
	public String toString() {
		return "Team{" + "name='" + name + '\'' + ", scores=" + scores + '}';
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}
}
