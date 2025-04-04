package de.fll.screen.service.parser;

import de.fll.screen.model.Competition;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.List;

public interface Parser {

	List<String> getOwnCompetitionIds(String user, String password) throws Exception;

	@Nonnull
	Competition parse(@Nullable Competition competition, int id, String user, String password) throws Exception;
}
