package de.redcare.githubscore.infrastructure.client.github;


import de.redcare.githubscore.domain.exceptions.GitHubApiException;
import de.redcare.githubscore.domain.exceptions.RateLimitExceededException;
import de.redcare.githubscore.infrastructure.client.github.client.GitHubErrorDecoder;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GitHubErrorDecoderTest {

    private final GitHubErrorDecoder decoder = new GitHubErrorDecoder();

    private Response createResponse(int status, String body, Map<String, String> headers) {
        return Response.builder()
                .status(status)
                .reason("Error")
                .request(Request.create(
                        Request.HttpMethod.GET,
                        "https://api.github.com/test",
                        Map.of(),
                        null,
                        StandardCharsets.UTF_8,
                        null
                ))
                .headers(headers.entrySet().stream().collect(
                        java.util.stream.Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> java.util.Set.of(entry.getValue())
                        )
                ))
                .body(body, StandardCharsets.UTF_8)
                .build();
    }

    @Test
    void shouldDecodeRateLimitExceeded() {
        String responseBody = "{\"message\":\"API rate limit exceeded\"}";
        Response response = createResponse(403, responseBody, Map.of("x-ratelimit-reset", "1234567890"));

        Exception ex = decoder.decode("testMethod", response);

        assertInstanceOf(RateLimitExceededException.class, ex);
        assertTrue(ex.getMessage().contains("API rate limit exceeded"));
    }

    @Test
    void shouldDecodeGitHubApiException() {
        String responseBody = "{\"message\":\"Internal Server Error\"}";
        Response response = createResponse(500, responseBody, Map.of());

        Exception ex = decoder.decode("testMethod", response);

        assertInstanceOf(GitHubApiException.class, ex);
        assertTrue(ex.getMessage().contains("GitHub API request failed"));
    }

    @Test
    void shouldFallbackToDefaultDecoderOnUnhandledStatus() {
        String responseBody = "{\"message\":\"Redirect\"}";
        Response response = createResponse(301, responseBody, Map.of());

        Exception ex = decoder.decode("testMethod", response);

        // Default decoder usually returns a FeignException
        assertTrue(ex instanceof feign.FeignException || ex instanceof GitHubApiException);
    }

    @Test
    void shouldHandleInvalidResetHeaderGracefully() {
        String responseBody = "{\"message\":\"API rate limit exceeded\"}";
        Response response = createResponse(403, responseBody, Map.of("x-ratelimit-reset", "not-a-number"));

        Exception ex = decoder.decode("testMethod", response);

        assertInstanceOf(RateLimitExceededException.class, ex);
    }
}
