package de.redcare.githubscore.web.exception;

import de.redcare.githubscore.domain.exceptions.GitHubApiException;
import de.redcare.githubscore.domain.exceptions.RateLimitExceededException;
import de.redcare.githubscore.web.exception.error.ErrorResponse;
import feign.RetryableException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler({
            UnknownHostException.class,
            ConnectException.class,
            RetryableException.class
    })
    public ResponseEntity<ErrorResponse> handleNetworkErrors(Exception ex) {
        String message = switch (ex) {
            case UnknownHostException e -> "External service unavailable: Host not found";
            case ConnectException e -> "Connection to external service failed";
            case RetryableException e -> "Temporary service interruption";
            default -> "Network communication error";
        };

        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, message, ex);
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleRateLimitExceeded(RateLimitExceededException ex) {
        long resetInSeconds = ex.getRemainingTimeMillis() / 1000;
        String message = String.format("GitHub API rate limit exceeded. Please try again in %d seconds.", resetInSeconds);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-RateLimit-Reset", String.valueOf(ex.getResetTime()));

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.TOO_MANY_REQUESTS.value(),
                message,
                ex.getClass().getSimpleName()
        );

        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .headers(headers)
                .body(errorResponse);
    }

    @ExceptionHandler(GitHubApiException.class)
    public ResponseEntity<ErrorResponse> handleGitHubApi(GitHubApiException ex) {
        return buildResponse(
                HttpStatus.SERVICE_UNAVAILABLE,
                "GitHub API error: " + ex.getMessage(),
                ex
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<ErrorResponse.Detail> details = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(fieldError -> new ErrorResponse.Detail(
                        fieldError.getField(),
                        fieldError.getDefaultMessage()
                ))
                .collect(Collectors.toList());

        ErrorResponse response = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Request validation failed",
                "ValidationException"
        ).withDetails(details);

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(HandlerMethodValidationException ex) {
        List<ErrorResponse.Detail> details = ex.getAllErrors().stream()
                .map(error -> new ErrorResponse.Detail(
                        error instanceof FieldError fieldError
                                ? fieldError.getField()
                                : Arrays.toString(Objects.requireNonNull(error.getArguments())),
                        error.getDefaultMessage()
                ))
                .toList();

        return ResponseEntity.badRequest()
                .body(new ErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        "Validation failed",
                        ex.getClass().getSimpleName()
                ).withDetails(details));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParams(MissingServletRequestParameterException ex) {
        ErrorResponse.Detail detail = new ErrorResponse.Detail(
                ex.getParameterName(),
                "This parameter is required"
        );

        ErrorResponse response = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Missing required parameter",
                "MissingParameterException"
        ).withDetails(List.of(detail));

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        ErrorResponse.Detail detail = new ErrorResponse.Detail(
                ex.getName(),
                String.format("Invalid value '%s' - expected %s",
                        ex.getValue(),
                        ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "valid type")
        );

        ErrorResponse response = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Invalid parameter value",
                "TypeMismatchException"
        ).withDetails(List.of(detail));

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedErrors(Exception ex) {
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred",
                ex
        );
    }
    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String message, Exception ex) {
        return ResponseEntity.status(status).body(
                new ErrorResponse(status.value(), message, ex.getClass().getSimpleName())
        );
    }
}