
package de.redcare.githubscore.domain.exceptions;

import java.util.Map;

/**
 * Exception thrown when the GitHub API returns an error response.
 */
public class GitHubApiException extends GithubException {
    public GitHubApiException(int status, String message, String requestUrl,
                              String responseBody, Map<String, String> headers) {
        super(status, message, requestUrl, responseBody, headers);
    }

    /**
     * Checks if this is a client error (4xx status code).
     */
    public boolean isClientError() {
        return getStatus() >= 400 && getStatus() < 500;
    }

    /**
     * Checks if this is a server error (5xx status code).
     */
    public boolean isServerError() {
        return getStatus() >= 500 && getStatus() < 600;
    }
}