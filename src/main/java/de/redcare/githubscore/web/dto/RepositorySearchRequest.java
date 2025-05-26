package de.redcare.githubscore.web.dto;

import java.time.LocalDate;

public record RepositorySearchRequest(
        String searchQuery,
        String language,
        SortBy sortBy,
        SortOrder sortOrder,
        Integer pageNumber,


        Integer pageSize,
        LocalDate createdAfter
) {
}

