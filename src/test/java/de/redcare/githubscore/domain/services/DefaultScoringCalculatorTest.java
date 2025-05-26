package de.redcare.githubscore.domain.services;

import de.redcare.githubscore.domain.config.ScoringProperties;
import de.redcare.githubscore.domain.service.DefaultScoringCalculator;
import de.redcare.githubscore.domain.model.Repository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DefaultScoringCalculatorTest {

    private DefaultScoringCalculator scoringCalculator;

    @BeforeEach
    public void setUp() {
        ScoringProperties.Weights weights = new ScoringProperties.Weights(
                0.4f,  // stars
                0.3f,  // forks
                0.3f   // recency
        );

        ScoringProperties.Maximums maximums = new ScoringProperties.Maximums(
                1000f, // max stars
                500f,  // max forks
                365    // max recency days
        );

        ScoringProperties config = new ScoringProperties(weights, maximums, true);
        scoringCalculator = new DefaultScoringCalculator(config);
    }

    @Test
    public void testCalculatePopularityScore() {
        ZonedDateTime lastUpdated = OffsetDateTime.now().minusDays(100).toZonedDateTime();
        Repository repo = new Repository(
                500, "test", "url", 500, 250, "Java", lastUpdated
        );

        float expectedStars = (500f / 1000f) * 0.4f;  // 0.2
        float expectedForks = (250f / 500f) * 0.3f;   // 0.15
        float recencyFactor = 1 - (100f / 365f);      // ≈ 0.726
        float expectedRecency = recencyFactor * 0.3f; // ≈ 0.2178
        float expectedTotal = expectedStars + expectedForks + expectedRecency;

        float actual = scoringCalculator.calculatePopularityScore(repo);
        assertEquals(expectedTotal, actual, 0.001f);
    }
    @Test
    public void testFullScore() {
        Repository repo = new Repository(1, "repo", "url", 1000, 500, "Java",
                ZonedDateTime.now());
        float expectedScore = 0.4f + 0.3f + 0.3f;
        assertEquals(expectedScore, scoringCalculator.calculatePopularityScore(repo), 0.001f);
    }

    @Test
    public void testZeroStarsForksRecent() {
        Repository repo = new Repository(1, "repo", "url", 0, 0, "Java",
                ZonedDateTime.now());
        float expectedScore = 0 + 0 + 0.3f;
        assertEquals(expectedScore, scoringCalculator.calculatePopularityScore(repo), 0.001f);
    }

    @Test
    public void testOldRepository() {
        Repository repo = new Repository(1, "repo", "url", 500, 250, "Java",
                ZonedDateTime.now().minusDays(365));
        float stars = (500f / 1000f) * 0.4f;  // 0.2
        float forks = (250f / 500f) * 0.3f;   // 0.15
        float recency = 0.0f;                // too old
        assertEquals(stars + forks + recency, scoringCalculator.calculatePopularityScore(repo), 0.001f);
    }

    @Test
    public void testRecentRepositoryLowStats() {
        Repository repo = new Repository(1, "repo", "url", 10, 5, "Java",
                ZonedDateTime.now().minusDays(1));
        float stars = (10f / 1000f) * 0.4f;   // 0.004
        float forks = (5f / 500f) * 0.3f;     // 0.003
        float recency = ((365f - 1f) / 365f) * 0.3f; // ~0.299
        float expected = stars + forks + recency;
        assertEquals(expected, scoringCalculator.calculatePopularityScore(repo), 0.001f);
    }

    @Test
    public void testNoNormalization() {
        ScoringProperties properties = new ScoringProperties(
                new ScoringProperties.Weights(0.5f, 0.3f, 0.2f),
                new ScoringProperties.Maximums(100f, 100f, 30),
                false
        );
        scoringCalculator = new DefaultScoringCalculator(properties);

        Repository repo = new Repository(1, "repo", "url", 50, 50, "Java",
                ZonedDateTime.now());

        float stars = 50f * 0.5f; // 25
        float forks = 50f * 0.3f; // 15
        float recency = 1f * 0.2f; // 0.2
        float expected = stars + forks + recency;
        assertEquals(expected, scoringCalculator.calculatePopularityScore(repo), 0.001f);
    }
}