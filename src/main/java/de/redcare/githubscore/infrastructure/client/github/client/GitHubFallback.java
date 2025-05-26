package de.redcare.githubscore.infrastructure.client.github.client;

import de.redcare.githubscore.infrastructure.client.github.models.GitHubSearchResponse;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class GitHubFallback implements GitHubFeignClient {
    @Override
    public GitHubSearchResponse searchRepositories(
            String query,
            String sort,
            String order,
            int page,
            int perPage
    ) {

        return createEmptyResponse(query);
    }

    private GitHubSearchResponse createEmptyResponse(String query) {
        return new GitHubSearchResponse(
                0,
                false,
                Collections.emptyList()
        );
    }

}
