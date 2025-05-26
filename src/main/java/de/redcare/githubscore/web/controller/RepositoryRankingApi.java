package de.redcare.githubscore.web.controller;


import de.redcare.githubscore.web.dto.RepositoryResponse;
import de.redcare.githubscore.web.dto.SortBy;
import de.redcare.githubscore.web.dto.SortOrder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

/**
 * API contract for fetching scored GitHub repositories with sorting, filtering, and pagination support.
 */
public interface RepositoryRankingApi {

    /**
     * Fetches a list of repositories based on query parameters and returns them scored and sorted.
     *
     * @param searchQuery  the text to search for in repository name/description.
     * @param language     the programming language to filter by (optional).
     * @param sortBy       the field to sort results by (stars, forks, updated, score).
     * @param sortOrder    the direction of sorting (asc or desc).
     * @param pageNumber   the page number for pagination (starting from 1).
     * @param pageSize     the number of repositories per page.
     * @param createdAfter filter for repositories created after the given date (optional).
     * @return a list of scored and sorted repositories.
     */
    @Operation(
            summary = "Get scored repositories",
            parameters = {
                    @Parameter(name = "searchQuery", in = ParameterIn.QUERY, required = true),
                    @Parameter(name = "language", in = ParameterIn.QUERY),
                    @Parameter(name = "sortBy", in = ParameterIn.QUERY, schema = @Schema(allowableValues = {"stars", "forks", "score"}, defaultValue = "stars")),
                    @Parameter(name = "sortOrder", in = ParameterIn.QUERY, schema = @Schema(allowableValues = {"asc", "desc"}, defaultValue = "desc")),
                    @Parameter(name = "pageNumber", in = ParameterIn.QUERY, schema = @Schema(defaultValue = "1")),
                    @Parameter(name = "pageSize", in = ParameterIn.QUERY, schema = @Schema(defaultValue = "10")),
                    @Parameter(name = "createdAfter", in = ParameterIn.QUERY, description = "Only include repositories created after this date (YYYY-MM-DD)")
            }
    )
    List<RepositoryResponse> getRepositories(
            @NotBlank @RequestParam(name = "searchQuery") String searchQuery,
            @RequestParam(name = "language", required = false) String language,
            @RequestParam(name = "sortBy", required = false, defaultValue = "STARS") SortBy sortBy,
            @RequestParam(name = "sortOrder", required = false, defaultValue = "DESC") SortOrder sortOrder,
            @Min(1) @RequestParam(name = "pageNumber", required = false, defaultValue = "1") Integer pageNumber,
            @Min(1) @Max(100) @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            @Schema(description = "Only include repositories created after this date (YYYY-MM-DD)", example = "2023-01-01")
            @RequestParam(name = "createdAfter", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdAfter
    );
}

