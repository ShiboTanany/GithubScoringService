package de.redcare.githubscore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@ConfigurationPropertiesScan("de.redcare.githubscore")
@EnableCaching
@EnableFeignClients(basePackages = "de.redcare.githubscore.infrastructure.client.github")
public class GitHubScoreServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GitHubScoreServiceApplication.class, args);
    }

}
