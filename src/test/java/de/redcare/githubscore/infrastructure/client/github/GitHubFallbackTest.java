package de.redcare.githubscore.infrastructure.client.github;

import de.redcare.githubscore.infrastructure.client.github.client.GitHubFallback;
import de.redcare.githubscore.infrastructure.client.github.models.GitHubSearchResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GitHubFallbackTest {

    private final GitHubFallback fallback = new GitHubFallback();

    @Test
    void searchRepositories_shouldReturnEmptyResponse() {
        // Given
        String query = "test query";
        String sort = "stars";
        String order = "desc";
        int page = 1;
        int perPage = 10;

        // When
        GitHubSearchResponse response = fallback.searchRepositories(query, sort, order, page, perPage);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.total_count()).isZero();
        assertThat(response.incomplete_results()).isFalse();
        assertThat(response.items()).isEmpty();
    }
}
