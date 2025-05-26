package de.redcare.githubscore.application.service;

import de.redcare.githubscore.domain.model.Repository;

/**
 * Strategy interface for calculating a popularity score for a GitHub repository.
 * <p>
 * Implementations of this interface define how the popularity of a repository
 * is measured based on customizable metrics such as stars, forks, and activity.
 */
public interface ScoringCalculator {

    /**
     * Calculates the popularity score of the given repository.
     * <p>
     * The score should be normalized to a consistent range (e.g., 0 to 1 or 0 to 100),
     * depending on the implementation.
     *
     * @param repository the GitHub repository for which to calculate the score
     * @return the calculated popularity score
     */
    float calculatePopularityScore(Repository repository);
}
