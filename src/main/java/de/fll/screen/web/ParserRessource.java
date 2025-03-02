package de.fll.screen.web;

import de.fll.screen.model.Category;
import de.fll.screen.model.Competition;
import de.fll.screen.repository.CompetitionRepository;
import de.fll.screen.service.parser.FLLRobotGameParser;
import de.fll.screen.web.dto.CompetitionDTO;
import de.fll.screen.web.dto.QuarterFinalCategoryDTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

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
	public CompetitionDTO parse(@RequestParam(value = "id", required = false) Integer id) {
		id = resolveId(id);
		// TODO don't always reparse, only when triggered by user
		/* if (competitionRepository.existsByInternalId(id)) { return competitionRepository.findByInternalId(id); } */

		Competition competition = parser.parse(null, id);
		return CompetitionDTO.of(competition);
	}

	@GetMapping("/api/quarter")
	public QuarterFinalCategoryDTO parseQuarterFinal(@RequestParam(value = "id", required = false) Integer id) {
		id = resolveId(id);

		// TODO reuse saved competition

		Competition competition = parser.parse(null, id);
		if (competition.getCategories().isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No categories available");
		}
		return QuarterFinalCategoryDTO.of(competition.getCategories().iterator().next());
	}

	@GetMapping("/api/testround")
	public Category parseTestRound(@RequestParam(value = "id", required = false) Integer id) {
		id = resolveId(id);

		// TODO reuse saved competition

		return parser.parseTestRound(null, id);
	}

	private Integer resolveId(Integer id) {
		if (id == null) {
			var competitions = parser.getOwnCompetitionIds();
			if (!competitions.isEmpty()) {
				return Integer.parseInt(competitions.getFirst());
			} else {
				// No personal competition available
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No competition available");
			}
		}
		return id;
	}
}
