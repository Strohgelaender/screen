package de.fll.screen.repository;

import de.fll.screen.model.Competition;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompetitionRepository extends CrudRepository<Competition, Long> {}
