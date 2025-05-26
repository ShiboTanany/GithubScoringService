package de.redcare.githubscore.infrastructure.client.github;


import de.redcare.githubscore.domain.model.Repository;
import de.redcare.githubscore.domain.repository.GithubRepository;
import de.redcare.githubscore.infrastructure.client.github.client.GitHubFeignClient;
import de.redcare.githubscore.infrastructure.client.github.models.GitHubRepositoryDto;
import de.redcare.githubscore.infrastructure.client.github.models.GitHubSearchResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GithubRepositoryTest {

    private GitHubFeignClient gitHubFeignClient;
    private GithubRepository githubRepository;

    @BeforeEach
    void setUp() {
        gitHubFeignClient = Mockito.mock(GitHubFeignClient.class);
        githubRepository = new GithubRepository(gitHubFeignClient);
    }

    @Test
    void fetchRepositories_shouldReturnMappedRepositories_whenFeignClientReturnsResponse() {
        // Arrange
        GitHubRepositoryDto dto = new GitHubRepositoryDto(
                123L,
                "repo-name",
                "fullname",
                "https://github.com/repo",
                "my-org/my-repo",
                "Java",
                10,
                2,
                OffsetDateTime.parse("2025-05-23T10:15:30+00:00").toZonedDateTime()
        );
        GitHubSearchResponse response = new GitHubSearchResponse(100, false, List.of(dto));

        when(gitHubFeignClient.searchRepositories(anyString(), anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(response);

        // Act
        List<Repository> repositories = githubRepository.fetchRepositories("test-query", "Java", "stars", "desc", 1, 10, LocalDate.now());

        // Assert
        assertThat(repositories).hasSize(1);
        Repository repo = repositories.getFirst();
        assertThat(repo.id()).isEqualTo(123L);
        assertThat(repo.name()).isEqualTo("repo-name");
        assertThat(repo.url()).isEqualTo("https://github.com/repo");
        assertThat(repo.stars()).isEqualTo(2);
        assertThat(repo.forks()).isEqualTo(10);
        assertThat(repo.language()).isEqualTo("Java");
    }


    @Test
    void fetchRepositories_shouldAppendLanguageAndCreatedQueryParts() {
        // Arrange
        GitHubSearchResponse response = new GitHubSearchResponse(1, true, Collections.emptyList());
        when(gitHubFeignClient.searchRepositories(anyString(), anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(response);
        final var localDateNow = LocalDate.now();
        // Act
        githubRepository.fetchRepositories("test", "Kotlin", "stars", "desc", 1, 10, localDateNow);

        // Assert
        verify(gitHubFeignClient).searchRepositories(
                argThat(query -> query.contains("test") &&
                        query.contains("language:Kotlin")),
                eq("stars"),
                eq("desc"),
                eq(1),
                eq(10)
        );
    }
}

