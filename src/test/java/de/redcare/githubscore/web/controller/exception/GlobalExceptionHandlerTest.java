package de.redcare.githubscore.web.controller.exception;


import de.redcare.githubscore.domain.exceptions.GitHubApiException;
import de.redcare.githubscore.domain.exceptions.RateLimitExceededException;
import de.redcare.githubscore.web.exception.GlobalExceptionHandler;
import de.redcare.githubscore.web.exception.error.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void handleNetworkErrors_withUnknownHostException_shouldReturnServiceUnavailable() {
        UnknownHostException ex = new UnknownHostException("Host not found");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleNetworkErrors(ex);

        assertTrue(response.getStatusCode().is5xxServerError());
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message())
                .isEqualTo("External service unavailable: Host not found");
        assertThat(response.getBody().errorType()).isEqualTo("UnknownHostException");
    }

    @Test
    void handleNetworkErrors_withConnectException_shouldReturnServiceUnavailable() {
        ConnectException ex = new ConnectException("Connection refused");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleNetworkErrors(ex);

        assertTrue(response.getStatusCode().is5xxServerError());
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message())
                .isEqualTo("Connection to external service failed");
        assertThat(response.getBody().errorType()).isEqualTo("ConnectException");
    }


    @Test
    void handleRateLimitExceeded_shouldReturnTooManyRequestsWithHeaders() {
        long resetTime = (System.currentTimeMillis() / 1000) + 60; // reset in 60 seconds
        RateLimitExceededException ex = new RateLimitExceededException(
                429,
                "Rate limit exceeded",
                "http://example.com",
                "{}",
                Map.of("X-RateLimit-Reset", String.valueOf(resetTime)),
                resetTime
        );

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleRateLimitExceeded(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(response.getHeaders()).containsKey("X-RateLimit-Reset");
        assertThat(response.getHeaders().getFirst("X-RateLimit-Reset")).isEqualTo(String.valueOf(resetTime));

        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.status()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
        assertThat(body.errorType()).isEqualTo("RateLimitExceededException");
        assertThat(body.message()).contains("GitHub API rate limit exceeded");
    }

    @Test
    void handleGitHubApi_shouldReturnServiceUnavailable() {
        GitHubApiException ex = new GitHubApiException(
                503,
                "Service unavailable",
                "http://example.com",
                "{}",
                Map.of()
        );

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGitHubApi(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);

        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.status()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.value());
        assertThat(body.message()).startsWith("GitHub API error:");
        assertThat(body.errorType()).isEqualTo("GitHubApiException");
    }
}
