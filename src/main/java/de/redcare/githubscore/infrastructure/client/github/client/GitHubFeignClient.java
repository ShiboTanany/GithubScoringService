package de.redcare.githubscore.infrastructure.client.github.client;


import de.redcare.githubscore.infrastructure.client.github.config.GitHubFeignConfig;
import de.redcare.githubscore.infrastructure.client.github.models.GitHubSearchResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
//contextId = "GitHubApiClient",
@FeignClient( name = "GitHubApiClient", url = "${github.api.base-url}", configuration = GitHubFeignConfig.class, fallback = GitHubFallback.class)
public interface GitHubFeignClient {

    @GetMapping("/search/repositories")
    GitHubSearchResponse searchRepositories(
            @RequestParam("q") String query,
            @RequestParam("sort") String sort,
            @RequestParam("order") String order,
            @RequestParam("page") int page,
            @RequestParam("perPage") int perPage
    );
}