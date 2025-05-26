# Repository Scoring API

This project is a backend application built with Spring Boot that scores GitHub repositories based on their popularity. The scoring is determined using the number of stars, forks, and the recency of updates. The application is structured using Domain-Driven Design (DDD) principles.

## 🧩 Architecture

```text
[User API Request]
     ↓
[Spring Boot REST Controller]
     ↓
[Service Layer]
     ↓
[GitHub API Client] → [GitHub Search API]
     ↓
[Scoring Algorithm] → [Score each repository]
     ↓
[Return JSON result]
```

## ✅ Features
- Score GitHub repositories using a custom popularity algorithm
- Search by language, creation date
- Clean architecture using DDD principles
- Unit and integration testing
- RESTful API with JSON responses
- Uses openfeign for sync GitHub API repository
- Dockerized application 

## ⚙️ Design Decisions
- **Open Feign** was chosen for blocking HTTP calls, suitable for not high scalability and aligned with Spring Webmvc.
- if it was a high-load application, we would consider using **WebClient** for non-blocking calls.
- The solution avoids over-engineering by keeping the data model simple and focusing on clarity and testability.
- Domain-Driven Design was applied at a pragmatic level to separate core concerns, without excessive abstraction.

## 📦 Package Structure
```
de.redcare.githubscore
├── domain             → Core domain models
├── application        → Business logic and services
├── infrastructure     → GitHub API client integration
└── web.controller     → REST controllers
```

## ⚙️ API Usage
**Endpoint:** `GET /api/repos`

**Query Parameters:**
- `language` — Programming language to filter
- `createdAfter` — Repositories created after this date (YYYY-MM-DD)

**Example Request:**
```
curl --location 'http://localhost:8080/api/v1/repos?searchQuery=test&language=css&sortBy=forks&sortOrder=desc&pageNumber=1&pageSize=99&createdAfter=2025-05-23' \
--header 'accept: */*' \
```

**Example Response:**
```json
[
  {
    "id": 978406357,
    "name": "Discord-Token-Login",
    "url": "https://api.github.com/repos/xPOURY4/Discord-Token-Login",
    "language": "CSS",
    "stars": 46,
    "forks": 38,
    "lastUpdated": "2025-05-22T08:59:46Z",
    "score": "20%"
  }
]
```

## 🧪 Tests

- `GithubRepositoryE2ETest.java` — End-to-end test for the GitHub repository scoring service with wiremock
- `GithubScoringServiceTest`  - Test the core logic of getting data and assigning scores
- `GitHubFallbackTest` — check the fallback behavior of the gitHub API client
- `GitHubErrorDecoderTest` - test the error handling of the GitHub API client
and other unit tests for various components.

## 🛠️ Technologies Used
- Java 21
- Spring Boot 3.3.5
- Spring webmvc
- openfeign
- cache
- JUnit 5
- Mockito
- RESTful APIs

## 🚀 Getting Started
```bash
git clone https://github.com/ShiboTanany/GithubScoringService.git
cd GithubScoringService
./mvnw spring-boot:run
```
or 
```bash
git clone https://github.com/ShiboTanany/GithubScoringService.git
cd GithubScoringService
sh build-and-run.sh 
```

⚙️ Configuration
The application is configured via application.yml, with support for environment variable overrides to facilitate deployment flexibility.

🔧 General Settings

```yaml
yaml:
 spring:
  application:
   name: GitHubScoreService
```
Name: The application is registered as GitHubScoreService.

🚀 Server
```yaml
server:
  port: ${PORT:8080}

```

Override with PORT environment variable.

📦 Caching

```yaml
spring:
  cache:
    type: caffeine
    caffeine:
      spec: maximumSize=100,expireAfterWrite=1h


```

Maximum Entries: 100

Expiration: 1 hour after write

🔍 Cache Monitoring
```yaml
management:
  endpoint:
    cache:
      enabled: true
  endpoints:
    web:
      exposure:
        include: cache
```
Cache actuator endpoint is enabled and exposed for inspection.

🌐 GitHub API Integration
```yaml
github:
  api:
    debug: true
    base-url: ${GITHUB_URL:https://api.github.com}
    timeout: ${TIMEOUT:5000}
    max-timeout: ${MAX_TIMEOUT:10000}
    retry:
      max-attempts: ${MAX_ATTEMPT:3}
      backoff-delay-ms: ${BACKOFF_DELAY_MS:1000}
```
| Property           | Description                  | Default                  |
| ------------------ | ---------------------------- | ------------------------ |
| `GITHUB_URL`       | GitHub API base URL          | `https://api.github.com` |
| `TIMEOUT`          | Initial request timeout (ms) | `5000`                   |
| `MAX_TIMEOUT`      | Maximum timeout (ms)         | `10000`                  |
| `MAX_ATTEMPT`      | Maximum retry attempts       | `3`                      |
| `BACKOFF_DELAY_MS` | Delay between retries (ms)   | `1000`                   |
These properties can be overridden using environment variables for flexibility in different environments.


🧮 Scoring Configuration
```yaml
scoring:
  normalize: true
  weights:
    stars: ${STARS_WEIGHT:0.5}
    forks: ${FORKS_WEIGHT:0.3}
    recency: ${RECENCY_WEIGHT:0.2}
  maximums:
    stars: ${MAX_STARS:100000}
    forks: ${MAX_FORKS:50000}
    recencyDays: ${MAX_RECENCY_DAYS:365}
```
Weights: Control how much each metric contributes to the repository score

All values can be overridden using environment variables for easy tuning.

📡 Feign Compression
```yaml
feign:
  compression:
    request:
      enabled: true
    response:
      enabled: true

```
Enables gzip compression for Feign client requests and responses to optimize bandwidth usage.



ErrorResponse
The ErrorResponse is a standard way our API sends error information back to clients. It helps you understand what went wrong when a request fails.

What’s inside an ErrorResponse? 


| Field   | Description                          |
|---------|------------------------------------|
| `status`   | The HTTP status code (e.g., 404, 500, 503).     |
| `message`  | A clear message explaining the error.            |
| `errorType`| The type of error (usually the exception name). |
| `timestamp`| When the error occurred (useful for debugging). |
| `details`  | Optional: list of specific field errors.         |



Details (optional)
If an error involves specific fields (like form validation), details contains a list of those with messages:

| Field   | Description                          |
|---------|------------------------------------|
| `field` | The name of the field causing error|
| `message` | Description of what’s wrong with it|

