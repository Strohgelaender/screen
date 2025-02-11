package de.fll.screen.web;

import de.fll.screen.model.Competition;
import de.fll.screen.repository.CompetitionRepository;
import de.fll.screen.service.parser.FLLRobotGameParser;
import de.fll.screen.web.dto.CompetitionDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ParserRessource {

	private final FLLRobotGameParser parser;

	private final CompetitionRepository competitionRepository;

	public ParserRessource(FLLRobotGameParser parser, CompetitionRepository competitionRepository) {
		this.parser = parser;
		this.competitionRepository = competitionRepository;
	}

	@PutMapping("/api/competitions")
	public Iterable<Competition> getCompetitionsForUser() {
		return competitionRepository.findAll();
	}

	@GetMapping("/api/parse")
	public CompetitionDTO parse() {
		long id = 231;
		/* if (competitionRepository.existsByInternalId(id)) { return competitionRepository.findByInternalId(id); } */
		Competition competition = parser.parse(null, 231);
		return CompetitionDTO.of(competition);
	}
}
