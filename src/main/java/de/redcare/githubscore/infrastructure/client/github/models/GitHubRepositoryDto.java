package de.redcare.githubscore.infrastructure.client.github.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.ZonedDateTime;

public record GitHubRepositoryDto(
        long id,
        String name,
        @JsonProperty("full_name")
        String fullName,
        String url,
        String description,
        String language,
        @JsonProperty("forks_count")
        long forksCount,
        @JsonProperty("stargazers_count")
        long stars,
        ZonedDateTime updated_at
) {
}

