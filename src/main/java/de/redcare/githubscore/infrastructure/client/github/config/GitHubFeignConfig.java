package de.redcare.githubscore.infrastructure.client.github.config;


import de.redcare.githubscore.infrastructure.client.github.client.GitHubErrorDecoder;
import feign.Logger;
import feign.Request;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class GitHubFeignConfig {
    private static final int CONNECTION_TIMEOUT_MS = 1500;
    private static final int READ_TIMEOUT_MS = 3000;

    private final GitHubApiProperties properties;

    public GitHubFeignConfig(GitHubApiProperties properties) {
        this.properties = properties;
    }

    @Bean
    public Request.Options requestOptions() {
        return new Request.Options(
                Math.min(properties.timeout(), CONNECTION_TIMEOUT_MS),
                TimeUnit.MILLISECONDS,
                Math.min(properties.maxTimeout(), READ_TIMEOUT_MS),
                TimeUnit.MILLISECONDS,
                true
        );
    }

    @Bean
    public Logger.Level feignLoggerLevel() {
        return properties.isDebug() ? Logger.Level.FULL : Logger.Level.BASIC;
    }

    @Bean
    public Retryer retryer() {
        return new Retryer.Default(
                properties.retry().backoffDelayMs(),
                properties.retry().backoffDelayMs() * 2, // Reduced from 3x to 2x
                properties.retry().maxAttempts()
        );
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return new GitHubErrorDecoder();
    }
}