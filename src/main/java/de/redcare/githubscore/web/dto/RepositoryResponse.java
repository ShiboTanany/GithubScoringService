package de.redcare.githubscore.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.ZonedDateTime;

@Schema(description = "Repository with calculated score")
public record RepositoryResponse(
        long id,
        String name,
        String url,
        String language,
        long stars,
        long forks,
        ZonedDateTime lastUpdated,
        String score
) {

}
