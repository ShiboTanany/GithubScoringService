package de.redcare.githubscore;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import de.redcare.githubscore.domain.repository.GithubRepository;
import de.redcare.githubscore.web.dto.RepositoryResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GithubRepositoryE2ETest {

    @Autowired
    private GithubRepository githubRepository;
    TestRestTemplate restTemplate = new TestRestTemplate();
    private static WireMockServer wireMockServer;
    @LocalServerPort
    private int port;

    @BeforeAll
    static void setup() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        wireMockServer.start();
        WireMock.configureFor(wireMockServer.port());
        System.setProperty("github.api.base-url", "http://localhost:" + wireMockServer.port());
    }

    @AfterAll
    static void tearDown() {
        wireMockServer.stop();
        System.clearProperty("github.api.base-url");
    }

    @Test
    void fetchRepositories_shouldReturnExpectedResults() {
        // Prepare dynamic query string with date
        String createdDate = LocalDate.now().toString(); // e.g., 2025-05-26
        String fullQuery = String.format("e2e-test language:Java created:>%s", createdDate);

        // Stub WireMock
        String jsonResponse = """
                {
                  "items": [
                    {
                      "id": 101,
                      "name": "e2e-repo",
                      "url": "https://github.com/user/e2e-repo",
                      "stargazers_count": 42,
                      "forks_count": 5,
                      "language": "Java",
                      "updated_at": "2025-05-23T10:15:30Z"
                    }
                  ]
                }
                """;

        wireMockServer.stubFor(get(urlPathEqualTo("/search/repositories"))
                .withQueryParam("q", equalTo(fullQuery))
                .withQueryParam("sort", equalTo("stars"))
                .withQueryParam("order", equalTo("desc"))
                .withQueryParam("page", equalTo("1"))
                .withQueryParam("perPage", equalTo("10"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(jsonResponse)
                        .withStatus(200)));

        // Call Feign client
        List<?> repos = githubRepository.fetchRepositories(
                "e2e-test", "Java", "stars", "desc", 1, 10, LocalDate.now());

        // Verify result
        assertThat(repos).hasSize(1);
        var repo = repos.getFirst();
        assertThat(repo).extracting("id").isEqualTo(101L);
        assertThat(repo).extracting("name").isEqualTo("e2e-repo");
        assertThat(repo).extracting("url").isEqualTo("https://github.com/user/e2e-repo");
        assertThat(repo).extracting("stars").isEqualTo(42L);
        assertThat(repo).extracting("forks").isEqualTo(5L);
        assertThat(repo).extracting("language").isEqualTo("Java");
    }


    @Test
    void getRepositories_shouldReturnScoredRepositories() {
        String createdDate = LocalDate.now().toString();
        String queryParam = String.format("e2e-test language:Java created:>%s", createdDate);

        String githubApiResponse = """
                {
                  "items": [
                    {
                      "id": 101,
                      "name": "e2e-repo",
                      "url": "https://github.com/user/e2e-repo",
                      "stargazers_count": 42,
                      "forks_count": 5,
                      "language": "Java",
                      "updated_at": "2025-05-23T10:15:30Z"
                    }
                  ]
                }
                """;

        wireMockServer.stubFor(get(urlPathEqualTo("/search/repositories"))
                .withQueryParam("q", equalTo(queryParam))
                .withQueryParam("sort", equalTo("stars"))
                .withQueryParam("order", equalTo("desc"))
                .withQueryParam("page", equalTo("1"))
                .withQueryParam("perPage", equalTo("10"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(githubApiResponse)
                        .withStatus(200)));

        // Call the REST API endpoint
        String url = String.format("http://localhost:%d/api/v1/repos?searchQuery=e2e-test&language=Java&sortBy=STARS&sortOrder=DESC&pageNumber=1&pageSize=10&createdAfter=%s", port, createdDate);


        ResponseEntity<List<RepositoryResponse>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );
        List<RepositoryResponse> repos = response.getBody();

        assertThat(repos).hasSize(1);

        final var repo = repos.getFirst();
        assertThat(repo.id()).isEqualTo(101);
        assertThat(repo.name()).isEqualTo("e2e-repo");
        assertThat(repo.url()).isEqualTo("https://github.com/user/e2e-repo");
        assertThat(repo.stars()).isEqualTo(42);
        assertThat(repo.forks()).isEqualTo(5);
        assertThat(repo.language()).isEqualTo("Java");
        assertThat(repo.score()).isEqualTo("20%");


    }
}

