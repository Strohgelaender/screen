package de.fll.screen.model;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
public class Competition {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(name = "name", nullable = false)
	private String name;

	@OneToMany(mappedBy = "competition", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<Category> categories = new HashSet<>();

	public String getName() {
		return name;
	}

	public long getId() {
		return id;
	}

	public void setName(String name) {
		this.name = name;
	}
}
