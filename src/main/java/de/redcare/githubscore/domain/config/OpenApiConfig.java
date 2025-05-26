package de.redcare.githubscore.domain.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI githubScoreOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("GitHub Repository Scoring API")
                        .description("API for scoring GitHub repositories")
                        .version("1.0"));
    }
}
