package de.redcare.githubscore.domain.services;

import de.redcare.githubscore.application.service.ScoringCalculator;
import de.redcare.githubscore.domain.model.Repository;
import de.redcare.githubscore.domain.model.ScoredRepository;
import de.redcare.githubscore.domain.repository.GithubRepository;
import de.redcare.githubscore.domain.service.GithubScoringService;
import de.redcare.githubscore.web.dto.RepositorySearchRequest;
import de.redcare.githubscore.web.dto.SortBy;
import de.redcare.githubscore.web.dto.SortOrder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class GithubScoringServiceTest {

    @MockBean
    private GithubRepository githubApiClient;

    @Mock
    private ScoringCalculator scoringService;

    private GithubScoringService githubScoringService;

    @BeforeEach
    public void setUp() {
        githubScoringService = new GithubScoringService(githubApiClient, scoringService);
    }

    @Test
    void fetchRepositoriesScores_shouldReturnScoredRepositories() {
        // Given
        RepositorySearchRequest request = new RepositorySearchRequest(
                "spring",
                "java",
                SortBy.STARS,
                SortOrder.DESC,
                1,
                10,
                LocalDate.of(2023, 1, 1));

        Repository repo1 = createMockRepository("spring-boot", 50000, 25000);
        Repository repo2 = createMockRepository("spring-framework", 45000, 22000);

        when(githubApiClient.fetchRepositories(any(), any(), any(), any(), anyInt(), anyInt(), any()))
                .thenReturn(List.of(repo1, repo2));

        when(scoringService.calculatePopularityScore(repo1)).thenReturn(95.5f);
        when(scoringService.calculatePopularityScore(repo2)).thenReturn(90.2f);

        // When
        List<ScoredRepository> result = githubScoringService.fetchRepositoriesScores(request);

        // Then
        assertEquals(2, result.size());
        assertEquals("spring-boot", result.getFirst().name());
        assertEquals(95.5f, result.getFirst().score());
        assertEquals(50000, result.getFirst().stars());

        verify(githubApiClient).fetchRepositories(
                "spring", "java", "stars", "desc", 1, 10, LocalDate.of(2023, 1, 1));
    }

    @Test
    void fetchRepositoriesScores_shouldSortByScoreWhenRequested() {
        // Given
        RepositorySearchRequest request = new RepositorySearchRequest(
                "spring",
                "java",
                SortBy.SCORE,
                SortOrder.DESC,
                1,
                10,
                null);

        Repository repo1 = createMockRepository("repo-medium", 100, 50);
        Repository repo2 = createMockRepository("repo-high", 200, 100);
        Repository repo3 = createMockRepository("repo-low", 50, 25);

        when(githubApiClient.fetchRepositories(any(), any(), isNull(), isNull(), anyInt(), anyInt(), isNull()))
                .thenReturn(List.of(repo1, repo2, repo3));

        when(scoringService.calculatePopularityScore(repo1)).thenReturn(75.0f);
        when(scoringService.calculatePopularityScore(repo2)).thenReturn(95.0f);
        when(scoringService.calculatePopularityScore(repo3)).thenReturn(50.0f);

        // When
        List<ScoredRepository> result = githubScoringService.fetchRepositoriesScores(request);

        // Then
        assertEquals(3, result.size());
        assertEquals("repo-high", result.getFirst().name()); // Highest score first
        assertEquals("repo-medium", result.get(1).name());
        assertEquals("repo-low", result.get(2).name());
    }

    @Test
    void fetchRepositoriesScores_shouldSortByScoreAscendingWhenRequested() {
        // Given
        RepositorySearchRequest request = new RepositorySearchRequest(
                "spring",
                "java",
                SortBy.SCORE,
                SortOrder.ASC,
                1,
                10,
                null);

        Repository repo1 = createMockRepository("repo-medium", 100, 50);
        Repository repo2 = createMockRepository("repo-high", 200, 100);

        when(githubApiClient.fetchRepositories(any(), any(), isNull(), isNull(), anyInt(), anyInt(), isNull()))
                .thenReturn(List.of(repo1, repo2));

        when(scoringService.calculatePopularityScore(repo1)).thenReturn(75.0f);
        when(scoringService.calculatePopularityScore(repo2)).thenReturn(95.0f);

        // When
        List<ScoredRepository> result = githubScoringService.fetchRepositoriesScores(request);

        // Then
        assertEquals(2, result.size());
        assertEquals("repo-medium", result.getFirst().name()); // Lower score first
        assertEquals("repo-high", result.get(1).name());
    }

    @Test
    void fetchRepositoriesScores_shouldHandleEmptyResults() {
        // Given
        RepositorySearchRequest request = new RepositorySearchRequest(
                "nonexistent",
                "language",
                SortBy.STARS,
                SortOrder.ASC,
                1,
                10,
                null);

        when(githubApiClient.fetchRepositories(any(), any(), any(), any(), anyInt(), anyInt(), any()))
                .thenReturn(List.of());

        // When
        List<ScoredRepository> result = githubScoringService.fetchRepositoriesScores(request);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void fetchRepositoriesScores_shouldUseParallelStreamForScoring() {
        // Given
        RepositorySearchRequest request = new RepositorySearchRequest(
                "spring",
                null,
                SortBy.SCORE,
                SortOrder.DESC,
                1,
                100,
                null);

        List<Repository> repositories = List.of(
                createMockRepository("repo1", 100, 50),
                createMockRepository("repo2", 200, 100),
                createMockRepository("repo3", 300, 150)
        );

        when(githubApiClient.fetchRepositories(any(), isNull(), isNull(), isNull(), anyInt(), anyInt(), isNull()))
                .thenReturn(repositories);

        when(scoringService.calculatePopularityScore(any())).thenReturn(80.0f);

        // When
        List<ScoredRepository> result = githubScoringService.fetchRepositoriesScores(request);

        // Then
        assertEquals(3, result.size());
        verify(scoringService, times(3)).calculatePopularityScore(any());
    }

    private Repository createMockRepository(String name, int stars, int forks) {

        return new Repository(1, name, "https://github.com/test/" + name,
                stars, forks, "Java",
                ZonedDateTime.now());

    }
}
