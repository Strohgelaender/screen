package de.fll.screen.web;

import de.fll.screen.model.ScreenSettings;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ScreenSettingsRessource {

	private static final ScreenSettings DEFAULT_SETTINGS = new ScreenSettings(true, "/images/Hintergrund.png", List.of("/images/FLL-RGB_Challenge-horiz-full-color.png"));

	@GetMapping("/api/settings")
	public ScreenSettings getSettings(@RequestParam(value = "id", required = false) Integer id) {
		if (id == null) {
			return DEFAULT_SETTINGS;
		}
		// TODO find settings by id
		return DEFAULT_SETTINGS;
	}
}
