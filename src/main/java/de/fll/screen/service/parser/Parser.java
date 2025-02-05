package de.fll.screen.service.parser;

import de.fll.screen.model.Competition;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public interface Parser {

	@Nonnull
	Competition parse(@Nullable Competition competition, int id, String user, String password);
}
