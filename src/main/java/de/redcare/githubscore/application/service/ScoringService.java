package de.redcare.githubscore.application.service;

import de.redcare.githubscore.domain.model.ScoredRepository;
import de.redcare.githubscore.web.dto.RepositorySearchRequest;

import java.util.List;

/**
 * Service interface for calculating and retrieving scored GitHub repositories.
 * <p>
 * Implementations of this interface are responsible for querying repositories
 * based on user-defined search criteria and returning a scored representation
 * of each repository.
 */
public interface ScoringService {

    /**
     * Fetches repositories from GitHub based on the provided search request,
     * calculates a popularity score for each, and returns the scored results.
     *
     * @param repositorySearchRequest request containing search parameters such as
     *                                keywords, language, sorting, and pagination
     * @return a list of repositories enriched with popularity scores
     */
    List<ScoredRepository> fetchRepositoriesScores(RepositorySearchRequest repositorySearchRequest);
}
