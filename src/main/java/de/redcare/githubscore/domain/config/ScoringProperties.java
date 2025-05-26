package de.redcare.githubscore.domain.config;


import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "scoring")
public record ScoringProperties(
        @Valid Weights weights,
        @Valid Maximums maximums,
        boolean normalize
) {

    public record Weights(
            @Positive float stars,
            @Positive float forks,
            @Positive float recency
    ) {
        public Weights {
            float total = stars + forks + recency;
            if (Math.abs(total - 1.0f) > 0.001f) {
                throw new IllegalArgumentException(
                        "Weights must sum to 1.0 (current sum: " + total + ")"
                );
            }
        }
    }

    public record Maximums(
            @Positive float stars,
            @Positive float forks,
            @Positive int recencyDays
    ) {
    }
}