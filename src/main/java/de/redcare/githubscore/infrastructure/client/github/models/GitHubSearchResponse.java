package de.redcare.githubscore.infrastructure.client.github.models;

import java.util.List;

public record GitHubSearchResponse(
        int total_count,
        boolean incomplete_results,
        List<GitHubRepositoryDto> items
) {
}
