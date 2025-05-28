package de.redcare.githubscore.domain.service;

import de.redcare.githubscore.application.mapper.ScoredRepositoryMapper;
import de.redcare.githubscore.application.service.ScoringCalculator;
import de.redcare.githubscore.application.service.ScoringService;
import de.redcare.githubscore.domain.model.Repository;
import de.redcare.githubscore.domain.model.ScoredRepository;
import de.redcare.githubscore.domain.repository.GithubRepository;
import de.redcare.githubscore.web.dto.RepositorySearchRequest;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Service implementation that fetches GitHub repositories and calculates popularity scores.
 * <p>
 * If {@code sortBy=score} is provided, sorting is handled manually after score calculation.
 * Otherwise, sorting is delegated to GitHub API.
 */
@Service
public class GithubScoringService implements ScoringService {

    private static final String SCORE_SORT = "score";

    private final GithubRepository githubRepository;
    private final ScoringCalculator scoringCalculator;

    /**
     * Constructs a new {@link GithubScoringService} instance.
     *
     * @param githubRepository  client to fetch repositories from GitHub
     * @param scoringCalculator scoring logic to compute popularity
     */
    public GithubScoringService(GithubRepository githubRepository,
                                ScoringCalculator scoringCalculator) {
        this.githubRepository = githubRepository;
        this.scoringCalculator = scoringCalculator;
    }

    /**
     * Fetches repositories based on the provided search criteria and assigns popularity scores.
     * <p>
     * If sorting by score is requested, this method applies in-memory sorting after scoring.
     * For other sort fields (e.g. stars, forks, updated), it relies on GitHub's native sorting.
     *
     * @param request the repository search parameters
     * @return a list of repositories enriched with popularity scores
     */
    @Override
    public List<ScoredRepository> fetchRepositoriesScores(RepositorySearchRequest request) {
        final String sortBy = request.sortBy().name().toLowerCase();
        final boolean sortByScore = SCORE_SORT.equals(sortBy);
        final String sortOrder = request.sortOrder().name().toLowerCase();

        // Fetch raw repositories from GitHub API with sorting (unless sorting by score)
        List<Repository> repositories = githubRepository.fetchRepositories(
                request.searchQuery(),
                request.language(),
                sortByScore ? null : sortBy,
                sortByScore ? null : sortOrder,
                request.pageNumber(),
                request.pageSize(),
                request.createdAfter()
        );

        // Map each repository to a scored repository
        Stream<ScoredRepository> scoredStream = repositories.stream()
                .map(repo ->
                        ScoredRepositoryMapper.toScoredRepository(
                                repo,
                                scoringCalculator.calculatePopularityScore(repo)

                        ));

        // If sort by score is requested, sort manually based on calculated score
        if (sortByScore) {
            Comparator<ScoredRepository> comparator = Comparator.comparing(ScoredRepository::score);
            if ("desc".equals(sortOrder)) {
                comparator = comparator.reversed();
            }
            scoredStream = scoredStream.sorted(comparator);
        }

        return scoredStream.toList();
    }
}
