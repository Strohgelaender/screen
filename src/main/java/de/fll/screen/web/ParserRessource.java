package de.fll.screen.web;

import de.fll.screen.model.Competition;
import de.fll.screen.service.parser.FLLRobotGameParser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ParserRessource {

	private final FLLRobotGameParser parser;

	public ParserRessource(FLLRobotGameParser parser) {
		this.parser = parser;
	}

	@GetMapping("/api/parse")
	public Competition parse() {
		return parser.parse(null, 231);
	}
}
