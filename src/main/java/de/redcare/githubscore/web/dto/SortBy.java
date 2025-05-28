package de.redcare.githubscore.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Allowed fields for sorting the repository results")
public enum SortBy {
    @Schema(description = "Sort by GitHub stars")
    STARS,

    @Schema(description = "Sort by number of forks")
    FORKS,

    @Schema(description = "Sort by calculated score")
    SCORE;

}
