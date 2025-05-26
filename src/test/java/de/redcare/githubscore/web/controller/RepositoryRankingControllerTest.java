package de.redcare.githubscore.web.controller;


import de.redcare.githubscore.application.service.ScoringService;
import de.redcare.githubscore.domain.model.ScoredRepository;
import de.redcare.githubscore.web.dto.RepositoryResponse;
import de.redcare.githubscore.web.dto.RepositorySearchRequest;
import de.redcare.githubscore.web.dto.SortBy;
import de.redcare.githubscore.web.dto.SortOrder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositoryRankingControllerTest {

    @Mock
    private ScoringService scoringService;

    @InjectMocks
    private RepositoryRankingController controller;

    @Test
    void getRepositories_shouldReturnSortedResults() {
        // Given
        RepositorySearchRequest expectedRequest = new RepositorySearchRequest(
                "spring",
                "java",
                SortBy.SCORE,
                SortOrder.DESC,
                1,
                10,
                LocalDate.of(2023, 1, 1));

        when(scoringService.fetchRepositoriesScores(any(RepositorySearchRequest.class)))
                .thenReturn(List.of(
                        createMockRepository(1, "spring-boot"),
                        createMockRepository(0.85f, "spring-framework"),
                        createMockRepository(0.95f, "spring-data")
                ));

        // When
        List<RepositoryResponse> result = controller.getRepositories(
                "spring",
                "java",
                SortBy.SCORE,
                SortOrder.DESC,
                1,
                10,
                LocalDate.of(2023, 1, 1));

        // Then
        assertEquals(3, result.size());
        assertEquals("spring-boot", result.getFirst().name());
        assertEquals(100 + "%", result.getFirst().score());
    }

    @Test
    void getRepositories_shouldHandleNullParameters() {
        // Given
        when(scoringService.fetchRepositoriesScores(any(RepositorySearchRequest.class)))
                .thenReturn(List.of(createMockRepository(0.50f, "test-repo")));

        // When
        List<RepositoryResponse> result = controller.getRepositories(
                null, null, null, null, null, null, null);

        // Then
        assertEquals(1, result.size());
    }

    @Test
    void getRepositories_shouldReturnEmptyListWhenNoResults() {
        // Given
        when(scoringService.fetchRepositoriesScores(any(RepositorySearchRequest.class)))
                .thenReturn(List.of());

        // When
        List<RepositoryResponse> result = controller.getRepositories(
                "nonexistent",
                "language",
                SortBy.STARS,
                SortOrder.ASC,
                1,
                10,
                LocalDate.now());

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void getRepositories_shouldMapAllFieldsCorrectly() {
        // Given
        RepositorySearchRequest expectedRequest = new RepositorySearchRequest(
                "query",
                "java",
                SortBy.FORKS,
                SortOrder.ASC,
                2,
                20,
                LocalDate.of(2022, 1, 1));

        when(scoringService.fetchRepositoriesScores(expectedRequest))
                .thenReturn(List.of(createMockRepository(0.75f, "sample-repo")));

        // When
        List<RepositoryResponse> result = controller.getRepositories(
                "query",
                "java",
                SortBy.FORKS,
                SortOrder.ASC,
                2,
                20,
                LocalDate.of(2022, 1, 1));

        // Then
        assertEquals(1, result.size());
        RepositoryResponse response = result.getFirst();
        assertEquals("sample-repo", response.name());
        assertEquals(75 + "%", response.score());
    }

    private ScoredRepository createMockRepository(float score, String name) {
        return new ScoredRepository(
                1,
                name,
                "https://github.com/test/" + name,
                "Java",
                100, // stars
                50,  // forks
                ZonedDateTime.now(),
                score
        );
    }
}