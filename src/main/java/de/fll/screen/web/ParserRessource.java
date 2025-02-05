package de.fll.screen.web;

import de.fll.screen.model.Competition;
import de.fll.screen.repository.CompetitionRepository;
import de.fll.screen.service.parser.FLLRobotGameParser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ParserRessource {

	private final FLLRobotGameParser parser;

	private final CompetitionRepository competitionRepository;

	public ParserRessource(FLLRobotGameParser parser, CompetitionRepository competitionRepository) {
		this.parser = parser;
		this.competitionRepository = competitionRepository;
	}

	@GetMapping("/api/parse")
	public Competition parse() {
		long id = 231;
		if (competitionRepository.existsByInternalId(id)) {
			return competitionRepository.findByInternalId(id);
		}
		return parser.parse(null, 231);
	}
}
