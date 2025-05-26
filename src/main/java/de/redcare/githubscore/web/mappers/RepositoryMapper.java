package de.redcare.githubscore.web.mappers;


import de.redcare.githubscore.domain.model.ScoredRepository;
import de.redcare.githubscore.web.dto.RepositoryResponse;

public class RepositoryMapper {

    public static RepositoryResponse toResponse(ScoredRepository repository) {
        return new RepositoryResponse(
                repository.id(),
                repository.name(),
                repository.url(),
                repository.language(),
                repository.stars(),
                repository.forks(),
                repository.lastUpdated(),
                Math.round(repository.score()*100) + "%"
        );
    }
}
