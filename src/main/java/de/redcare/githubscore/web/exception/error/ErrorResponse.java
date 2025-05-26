package de.redcare.githubscore.web.exception.error;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(
        int status,
        String message,
        String errorType,
        Instant timestamp,
        List<Detail> details
) {
    public record Detail(String field, String message) {}

    public ErrorResponse(int status, String message, String errorType) {
        this(status, message, errorType, Instant.now(), List.of());
    }

    public ErrorResponse withDetails(List<Detail> details) {
        return new ErrorResponse(status, message, errorType, timestamp, details);
    }
}