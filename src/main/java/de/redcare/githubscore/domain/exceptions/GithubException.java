package de.redcare.githubscore.domain.exceptions;

import java.util.Collections;
import java.util.Map;

/**
 * Base exception for GitHub API related errors.
 * Contains detailed information about the failed request.
 */
public class GithubException extends RuntimeException {
    private final int status;
    private final String requestUrl;
    private final String responseBody;
    private final Map<String, String> headers;
    private final long timestamp;

    public GithubException(int status, String message, String requestUrl,
                           String responseBody, Map<String, String> headers) {
        super(message);
        this.status = status;
        this.requestUrl = requestUrl;
        this.responseBody = responseBody;
        this.headers = headers != null ? headers : Collections.emptyMap();
        this.timestamp = System.currentTimeMillis();
    }

    public int getStatus() {
        return status;
    }
    @Override
    public String toString() {
        return String.format("%s [Status: %d, URL: %s, Timestamp: %d]",
                getClass().getSimpleName(), status, requestUrl, timestamp);
    }
}