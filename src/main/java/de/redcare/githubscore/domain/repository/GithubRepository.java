package de.redcare.githubscore.domain.repository;

import de.redcare.githubscore.domain.model.Repository;
import de.redcare.githubscore.infrastructure.client.github.client.GitHubFeignClient;
import de.redcare.githubscore.infrastructure.client.github.models.GitHubRepositoryDto;
import de.redcare.githubscore.infrastructure.client.github.models.GitHubSearchResponse;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class GithubRepository {
    private final GitHubFeignClient gitHubFeignClient;

    public GithubRepository(GitHubFeignClient gitHubFeignClient) {
        this.gitHubFeignClient = gitHubFeignClient;
    }

    @Cacheable(value = "repositories", key = "{#query,#language,#sortBy,#sortOrder,#page,#perPage, #createdAfter}")
    public List<Repository> fetchRepositories(String query, String language, String sortBy, String sortOrder, int page, int perPage, LocalDate createdAfter) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Query must not be null or empty");
        }

        GitHubSearchResponse response = gitHubFeignClient.searchRepositories(
                createSearchQuery(query, language, createdAfter).toString(), sortBy, sortOrder, page, perPage);
        return response.items().stream().map(this::toDomainRepository).toList();

    }

    private static StringBuilder createSearchQuery(String query, String language, LocalDate createdAfter) {
        StringBuilder searchQuery = new StringBuilder(query);
        if (language != null) searchQuery.append(" language:").append(language);
        if (createdAfter != null) searchQuery.append(" created:>").append(createdAfter);
        return searchQuery;
    }

    private Repository toDomainRepository(GitHubRepositoryDto dto) {
        return new Repository(
                dto.id(),
                dto.name(),
                dto.url(),
                dto.stars(),
                dto.forksCount(),
                dto.language(),
                dto.updated_at()
        );
    }
}