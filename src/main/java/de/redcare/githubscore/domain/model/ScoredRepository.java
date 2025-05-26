package de.redcare.githubscore.domain.model;

import java.time.ZonedDateTime;

public record ScoredRepository(
        long id,
        String name,
        String url,
        String language,
        long stars,
        long forks,
        ZonedDateTime lastUpdated,
        float score
) {
}

