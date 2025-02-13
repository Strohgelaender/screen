package de.fll.screen.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebClientRessource {

	@GetMapping("/")
	public String index() {
		return "forward:/index.html";
	}

	@GetMapping("/screen")
	public String screen() {
		return "forward:/screen.html";
	}
}
