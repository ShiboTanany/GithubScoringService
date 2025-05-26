package de.redcare.githubscore.domain.model;

import java.time.ZonedDateTime;

public record Repository(
        long id,
        String name,
        String url,
        long stars,
        long forks,
        String language,
        ZonedDateTime lastUpdated
) {}





