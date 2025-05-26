package de.redcare.githubscore.web.controller;

import de.redcare.githubscore.application.service.ScoringService;
import de.redcare.githubscore.web.dto.RepositoryResponse;
import de.redcare.githubscore.web.dto.RepositorySearchRequest;
import de.redcare.githubscore.web.dto.SortBy;
import de.redcare.githubscore.web.dto.SortOrder;
import de.redcare.githubscore.web.mappers.RepositoryMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller that implements {@link RepositoryRankingApi} to expose endpoints for
 * retrieving scored GitHub repositories based on various filters and sorting options.
 */
@RestController
@RequestMapping("/api/v1/repos")
public class RepositoryRankingController implements RepositoryRankingApi {

    private final ScoringService githubScoringService;

    public RepositoryRankingController(ScoringService githubScoringService) {
        this.githubScoringService = githubScoringService;
    }

    @Override
    @GetMapping
    public List<RepositoryResponse> getRepositories(
            String searchQuery,
            String language,
            SortBy sortBy,
            SortOrder sortOrder,
            Integer pageNumber,
            Integer pageSize,
            LocalDate createdAfter
    ) {
        RepositorySearchRequest searchRequest = new RepositorySearchRequest(
                searchQuery,
                language,
                sortBy,
                sortOrder,
                pageNumber,
                pageSize,
                createdAfter);

        return githubScoringService.fetchRepositoriesScores(searchRequest)
                .stream()
                .map(RepositoryMapper::toResponse)
                .toList();
    }
}
