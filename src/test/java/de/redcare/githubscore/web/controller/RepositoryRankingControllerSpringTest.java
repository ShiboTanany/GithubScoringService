package de.redcare.githubscore.web.controller;

import de.redcare.githubscore.application.service.ScoringService;
import de.redcare.githubscore.domain.model.ScoredRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RepositoryRankingController.class)
class RepositoryRankingControllerSpringTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ScoringService scoringService;

    @Test
    void getRepositories_shouldReturn200WithResults() throws Exception {
        // Mock service response
        when(scoringService.fetchRepositoriesScores(any()))
                .thenReturn(List.of(
                        new ScoredRepository(
                                1,
                                "spring-boot",
                                "https://github.com/spring-projects/spring-boot",
                                "Java",
                                50000,
                                25000,
                                ZonedDateTime.of(
                                        LocalDate.of(2012, 4, 1).getYear(),
                                        4,
                                        1,
                                        0, 0, 0, 0,
                                        ZonedDateTime.now().getZone()),
                                0.955f
                        )
                ));

        // Execute and verify
        mockMvc.perform(get("/api/v1/repos")
                        .param("searchQuery", "spring")
                        .param("language", "java")
                        .param("sortBy", "SCORE")
                        .param("sortOrder", "DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("spring-boot")))
                .andExpect(jsonPath("$[0].score", is("96%")))
                .andExpect(jsonPath("$[0].language", is("Java")));
    }

    @Test
    void handleTypeMismatch_shouldReturnProperErrorResponse() throws Exception {
        // Test with a controller that would throw this exception
        mockMvc.perform(get("/api/v1/repos")
                        .param("searchQuery", "spring")
                        .param("pageSize", "not-a-number")) // This should trigger type mismatch
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.message").value("Invalid parameter value"))
                .andExpect(jsonPath("$.errorType").value("TypeMismatchException"))
                .andExpect(jsonPath("$.details[0].field").value("pageSize"))
                .andExpect(jsonPath("$.details[0].message").value(containsString("Invalid value")))
                .andExpect(jsonPath("$.details[0].message").value(containsString("expected")));
    }
    @Test
    void getRepositories_shouldHandleMissingParameters() throws Exception {
        when(scoringService.fetchRepositoriesScores(any()))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/repos"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getRepositories_shouldValidateParameters() throws Exception {
        mockMvc.perform(get("/api/v1/repos")
                        .param("searchQuery", "spring")
                        .param("pageSize", "101")) // exceeds max
                .andExpect(status().isBadRequest());
    }

    @Test
    void getRepositories_shouldHandleServiceErrors() throws Exception {
        when(scoringService.fetchRepositoriesScores(any()))
                .thenThrow(new RuntimeException("Service unavailable"));

        mockMvc.perform(get("/api/v1/repos")
                        .param("searchQuery", "spring"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void getRepositories_shouldReturnCorrectMediaType() throws Exception {
        when(scoringService.fetchRepositoriesScores(any()))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/repos")
                        .param("searchQuery", "spring")
                        .accept("application/json"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }
}