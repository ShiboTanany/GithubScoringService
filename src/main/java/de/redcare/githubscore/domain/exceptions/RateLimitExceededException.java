
package de.redcare.githubscore.domain.exceptions;

import java.util.Map;

/**
 * Exception thrown when GitHub API rate limits are exceeded.
 */
public class RateLimitExceededException extends GitHubApiException {
    private final long resetTime;

    public RateLimitExceededException(int status, String message, String requestUrl,
                                      String responseBody, Map<String, String> headers,
                                      long resetTime) {
        super(status, message, requestUrl, responseBody, headers);
        this.resetTime = resetTime;
    }

    /**
     * @return The timestamp (in seconds since epoch) when the rate limit will reset
     */
    public long getResetTime() {
        return resetTime;
    }

    /**
     * @return The remaining time in milliseconds until the rate limit resets
     */
    public long getRemainingTimeMillis() {
        return Math.max(0, (resetTime * 1000) - System.currentTimeMillis());
    }

    @Override
    public String toString() {
        return String.format("%s [Reset in: %d seconds]",
                super.toString(), getRemainingTimeMillis() / 1000);
    }
}