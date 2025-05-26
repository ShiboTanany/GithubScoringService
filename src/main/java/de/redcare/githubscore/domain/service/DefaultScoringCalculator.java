package de.redcare.githubscore.domain.service;

import de.redcare.githubscore.application.service.ScoringCalculator;
import de.redcare.githubscore.domain.config.ScoringProperties;
import de.redcare.githubscore.domain.model.Repository;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.ZonedDateTime;

@Component
public class DefaultScoringCalculator implements ScoringCalculator {

    private final ScoringProperties config;

    public DefaultScoringCalculator(ScoringProperties config) {
        this.config = config;
    }

    @Override
    public float calculatePopularityScore(Repository repo) {
        float stars = score(repo.stars(), config.maximums().stars(), config.weights().stars());
        float forks = score(repo.forks(), config.maximums().forks(), config.weights().forks());
        float recency = scoreRecency(repo.lastUpdated());
        return stars + forks + recency;
    }

    private float score(float value, float max, float weight) {
        float ratio = config.normalize() ? Math.min(value / max, 1.0f) : value;
        return ratio * weight;
    }

    private float scoreRecency(ZonedDateTime lastUpdated) {
        long days = Duration.between(lastUpdated, ZonedDateTime.now()).toDays();
        float ratio = 1 - Math.min(days / (float) config.maximums().recencyDays(), 1.0f);
        return ratio * config.weights().recency();
    }
}
