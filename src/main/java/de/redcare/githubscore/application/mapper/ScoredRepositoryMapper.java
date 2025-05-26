package de.redcare.githubscore.application.mapper;

import de.redcare.githubscore.domain.model.ScoredRepository;
import de.redcare.githubscore.domain.model.Repository;

public class ScoredRepositoryMapper {
    public static ScoredRepository toScoredRepository(Repository repository, float score) {
        return new ScoredRepository(
                repository.id(),
                repository.name(),
                repository.url(),
                repository.language(),
                repository.stars(),
                repository.forks(),
                repository.lastUpdated(),
                score
        );
    }
}
