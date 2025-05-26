package de.redcare.githubscore.infrastructure.client.github.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "github.api")
public record GitHubApiProperties(
        boolean isDebug,
        String baseUrl,
        int timeout,          // connection timeout (ms)
        int maxTimeout,       // response timeout (ms)
        Retry retry
) {
    public record Retry(int maxAttempts, long backoffDelayMs) {
    }
}
