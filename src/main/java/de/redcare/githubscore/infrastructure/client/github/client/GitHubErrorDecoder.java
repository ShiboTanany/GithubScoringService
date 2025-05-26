package de.redcare.githubscore.infrastructure.client.github.client;

import de.redcare.githubscore.domain.exceptions.GitHubApiException;
import de.redcare.githubscore.domain.exceptions.RateLimitExceededException;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

public class GitHubErrorDecoder implements ErrorDecoder {

    private static final Logger logger = LoggerFactory.getLogger(GitHubErrorDecoder.class);
    private static final String NO_BODY = "<no response body>";
    private static final String BODY_READ_ERROR = "<failed to read response body>";

    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        try {
            final int status = response.status();
            final String requestUrl = response.request().url();
            final Map<String, String> headers = extractHeaders(response);
            final String responseBody = extractResponseBody(response);

            logErrorDetails(requestUrl, status, headers, responseBody);

            if (isRateLimitError(status, responseBody)) {
                return createRateLimitException(status, requestUrl, responseBody, headers);
            }

            if (isClientOrServerError(status)) {
                return createGitHubApiException(methodKey, status, requestUrl, responseBody, headers);
            }

            return defaultDecoder.decode(methodKey, response);
        } catch (Exception e) {
            return handleDecodingError(response, e);
        }
    }

    private void logErrorDetails(String url, int status, Map<String, String> headers, String body) {
        if (logger.isErrorEnabled()) {
            logger.error("GitHub API request failed - URL: {}, Status: {}", url, status);
            logger.debug("Response headers: {}", headers);
            logger.debug("Response body: {}", body);
        }
    }

    private boolean isRateLimitError(int status, String responseBody) {
        return status == 403 &&
                (responseBody.contains("API rate limit exceeded") ||
                        responseBody.contains("rate limit"));
    }

    private boolean isClientOrServerError(int status) {
        return status >= 400;
    }

    private RateLimitExceededException createRateLimitException(
            int status, String url, String body, Map<String, String> headers) {
        long resetTime = parseResetTime(headers.get("x-ratelimit-reset"));
        return new RateLimitExceededException(
                status,
                "API rate limit exceeded",
                url,
                body,
                headers,
                resetTime
        );
    }

    private GitHubApiException createGitHubApiException(
            String methodKey, int status, String url, String body, Map<String, String> headers) {
        String errorMessage = String.format(
                "GitHub API request failed [%s] - Status: %d",
                methodKey, status
        );
        return new GitHubApiException(
                status,
                errorMessage,
                url,
                body,
                headers
        );
    }

    private long parseResetTime(String resetHeader) {
        try {
            return resetHeader != null ? Long.parseLong(resetHeader) : 0;
        } catch (NumberFormatException e) {
            logger.warn("Failed to parse rate limit reset time", e);
            return 0;
        }
    }

    private Exception handleDecodingError(Response response, Exception e) {
        logger.error("Error processing GitHub API error response for {} - Status: {}",
                response.request().url(), response.status(), e);

        return new GitHubApiException(
                response.status(),
                "Failed to process error response: " + e.getMessage(),
                response.request().url(),
                BODY_READ_ERROR,
                extractHeadersSafe(response)
        );
    }

    private String extractResponseBody(Response response) {
        if (response.body() == null) {
            return NO_BODY;
        }

        try (InputStream bodyIs = response.body().asInputStream()) {
            return new String(bodyIs.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.warn("Failed to read response body", e);
            return BODY_READ_ERROR;
        }
    }

    private Map<String, String> extractHeaders(Response response) {
        return response.headers().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> String.join(", ", entry.getValue())
                ));
    }

    private Map<String, String> extractHeadersSafe(Response response) {
        try {
            return extractHeaders(response);
        } catch (Exception e) {
            logger.warn("Failed to extract response headers", e);
            return Collections.emptyMap();
        }
    }
}