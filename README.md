## Roman Numeral Converter API

Spring Boot 3 REST service that converts integers **1–3999** to Roman numerals, with API-key security, validation, async range conversion, Micrometer metrics, optional New Relic export, and OpenAPI (Swagger UI).

---

### Table of contents

- [What’s implemented](#whats-implemented)
- [Architecture](#architecture)
- [Project structure](#project-structure)
- [Local setup (Docker)](#local-setup-docker)
- [Format, tests, and coverage (Maven)](#format-tests-and-coverage-maven)
- [API reference](#api-reference)
- [OpenAPI / Swagger](#openapi--swagger)
- [Monitoring and metrics](#monitoring-and-metrics)
- [Validation and errors](#validation-and-errors)
- [Future enhancements](#future-enhancements)

---

### What’s implemented

| Area | Details |
|------|---------|
| **REST API** | `GET /romannumeral` — single integer (`query`) or inclusive range (`min` & `max`) |
| **Domain** | Pure converter `1…3999` → Roman string; range results **sorted ascending** |
| **Concurrency** | Range uses `CompletableFuture` + fixed `Executor`; pool size from `RANGE_EXECUTOR_THREADS` or CPU-based default |
| **Security** | Stateless API key filter (`API_KEY_HEADER` / `APP_API_KEY`); role `API` for protected routes |
| **Validation** | Shared `RomanRequestValidator`; invalid input → **400** + JSON `{ "error": "…" }` |
| **Observability** | Request MDC `requestId` (from `x-request-id` or generated), structured Logback; **Micrometer** custom meters + JVM/process/system |
| **Metrics** | AOP (`RomanMetricsAspect`) around controller: `roman.single.requests`, `roman.range.requests`, `roman.invalid.requests`, `roman.request.latency` |
| **Actuator** | `/actuator/health` (public), `/actuator/metrics` (protected, same API key) |
| **New Relic (optional)** | Micrometer **OTLP** to New Relic (`NEW_RELIC_METRICS_EXPORT`); **Java agent** mounted via Compose (`./newrelic` → `/opt/newrelic`) for APM/logs |
| **Docs** | **springdoc-openapi** — Swagger UI + OpenAPI 3 JSON |
| **Quality** | **Spotless** (Google Java Format), **JaCoCo** (unit + integration reports), JUnit 5 |

---

### Architecture

High-level request flow:

```mermaid
flowchart LR
  subgraph Client
    C[HTTP Client]
  end
  subgraph Spring_Boot["Spring Boot application"]
    SEC[Security + API key filter]
    LOG[Request logging + MDC]
    CTL[RomanNumeralController]
    VAL[RomanRequestValidator]
    SVC[RomanNumeralService]
    DOM[RomanNumeralConverter]
    MET[AOP / ApiMetrics]
    ACT[Actuator]
  end
  C --> SEC
  SEC --> LOG
  LOG --> CTL
  CTL --> VAL
  CTL --> SVC
  SVC --> DOM
  CTL --> MET
  SEC --> ACT
```

Range conversion uses an **executor-backed** async path; Micrometer timers/counters are recorded via **AOP** on the controller.

```mermaid
flowchart TB
  subgraph Optional["Optional telemetry"]
    OTLP[Micrometer OTLP → New Relic]
    NRAG[NR Java agent - Docker Compose mount]
  end
  subgraph App["Application"]
    MM[Micrometer registry]
    MM --> OTLP
  end
  NRAG -.->|APM / forwarded logs| NR[New Relic]
  OTLP --> NR
```

---

### Project structure

```text
.
├── Dockerfile
├── docker-compose.yml
├── pom.xml
├── README.md
├── .env.example
├── scripts/
│   └── download-newrelic-agent.sh
├── docs/      
└── src/
    ├── main/
    │   ├── java/com/example/romanapi/...
    │   └── resources/
    │       ├── application.yml
    │       └── logback-spring.xml
    └── test/
        └── java/com/example/romanapi/...
```

---

### Local setup (Docker)

**Prerequisites:**

- Docker Desktop (or Engine) with Compose v2
- Java 17 + Maven (recommended for running `mvn spotless:check`, `mvn test`, `mvn verify` locally)

1. **Environment**

   ```bash
   cp .env.example .env
   ```

   Edit `.env` and set at least:

   - `APP_API_KEY` — secret the API must receive (e.g. `my-test-key`)
   - `API_KEY_HEADER` — defaults to `x-api-key`

   Optional: `APP_NAME`, `PORT`, `RANGE_EXECUTOR_THREADS`, New Relic keys (see `.env.example`).

2. **New Relic Java agent (optional)**

   For APM + log forwarding with Compose, install the agent once on the host:

   ```bash
   chmod +x scripts/download-newrelic-agent.sh
   ./scripts/download-newrelic-agent.sh
   ```

   This creates `./newrelic/newrelic.jar` (ignored by git). If the directory is missing, Compose still starts the app **without** the agent and prints a hint.

3. **Run the API**

   ```bash
   docker compose up -d --build
   ```

   The service listens on `PORT` from `.env` (default **8080**). Map matches `server.port` in `application.yml`.

   <details>
   <summary>Docker container screenshot</summary>

   ![Docker container running](docs/images/docker-container.png)

   </details>

   <details>
   <summary>Docker logs screenshot</summary>

   ![Docker logs](docs/images/docker-logs.png)

   </details>

4. **Stop**

   ```bash
   docker compose down
   ```

---

### Format, tests, and coverage (Maven)

Use **JDK 17** and Maven on the host (not via Compose).

From the repository root:

| Goal | Command |
|------|---------|
| Format check | `mvn spotless:check` |
| Auto-format | `mvn spotless:apply` |
| Unit tests | `mvn test` |
| Full verify (unit + IT + JaCoCo) | `mvn verify` |

After `mvn verify`, open:

- Unit coverage: `target/site/jacoco-ut/index.html`
- Integration coverage: `target/site/jacoco-it/index.html`

<details>
<summary>Unit test coverage screenshot</summary>

![Unit test coverage](docs/images/unit-test-coverage.png)

</details>

<details>
<summary>Integration test coverage screenshot</summary>

![Integration test coverage](docs/images/integration-test-coverage.png)

</details>

---

### API reference

Base URL: `http://localhost:8080` (or your `PORT`).

All protected calls must send the API key using the configured header (default **`x-api-key`**).

#### Health (no API key)

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/actuator/health` | Liveness/readiness JSON |

#### Roman numeral conversion (API key required)

| Method | Path | Query | Success |
|--------|------|-------|---------|
| `GET` | `/romannumeral` | `query=<int>` | **200** JSON single conversion |
| `GET` | `/romannumeral` | `min=<int>&max=<int>` | **200** JSON list of conversions (sorted) |

**Single response** (`200`):

```json
{
  "input": "9",
  "output": "IX"
}
```

**Range response** (`200`):

```json
{
  "conversions": [
    { "input": "1", "output": "I" },
    { "input": "2", "output": "II" }
  ]
}
```

**Examples**

```bash
# Health
curl -sS http://localhost:8080/actuator/health

# Single (replace key with your APP_API_KEY)
curl -sS -H "x-api-key: my-test-key" "http://localhost:8080/romannumeral?query=9"

# Range
curl -sS -H "x-api-key: my-test-key" "http://localhost:8080/romannumeral?min=1&max=3"

# Metrics (protected)
curl -sS -H "x-api-key: my-test-key" "http://localhost:8080/actuator/metrics"
```

<details>
<summary>Single conversion API screenshot</summary>

![Single conversion API](docs/images/roman-conversion-single-query-api.png)

</details>

<details>
<summary>Range conversion API (Postman) screenshot</summary>

![Range conversion API](docs/images/roman-conv-range-query-api-postman.png)

</details>

**Common status codes**

| Code | Meaning |
|------|---------|
| **200** | OK |
| **400** | Validation error — body `{ "error": "…" }` |
| **401** | Missing/invalid API key |

---

### OpenAPI / Swagger

Interactive docs are **unauthenticated** (see `SecurityConfig`).

| Resource | URL |
|----------|-----|
| **Swagger UI** | http://localhost:8080/swagger-ui.html |
| **OpenAPI JSON** | http://localhost:8080/v3/api-docs |

Use **Authorize** in Swagger UI if operations are documented with the API key security scheme (header name from `API_KEY_HEADER`).

<details>
<summary>Swagger UI screenshot</summary>

![Swagger UI](docs/images/swagger-ui-execution.png)

</details>

---

### Monitoring and metrics

| Feature | Where |
|---------|--------|
| **Spring Boot Actuator** | `/actuator/health`, `/actuator/metrics` (metrics endpoint requires API key) |
| **Micrometer** | JVM, process, system meters; custom `roman.*` counters/timer |
| **Request correlation** | Optional header `x-request-id`; otherwise a UUID is generated and logged (MDC `requestId`) |
| **New Relic OTLP** | Set `NEW_RELIC_METRICS_EXPORT=true` and `NEW_RELIC_LICENSE_KEY` in `.env`; US endpoint default in `application.yml` |
| **New Relic Java agent (implemented)** | Compose mounts `./newrelic`; JVM flags set in `docker-compose.yml` for app name + log forwarding. Set `NEW_RELIC_LICENSE_KEY`, `NEW_RELIC_APP_NAME` (or rely on `APP_NAME`). Use `DISABLE_NEW_RELIC_AGENT=true` to skip the agent. |

<details>
<summary>New Relic APM summary screenshot</summary>

![New Relic APM summary](docs/images/newrelic-APM-summary.png)

</details>

<details>
<summary>New Relic dashboard screenshot</summary>

![New Relic dashboard](docs/images/newrelic-dashboard.png)

</details>

<details>
<summary>New Relic logs screenshot</summary>

![New Relic logs](docs/images/newrelic-logs.png)

</details>

<details>
<summary>JVM metrics screenshot</summary>

![JVM metrics](docs/images/JVM-metrics.png)

</details>

---

### Validation and errors

- Provide **`query`** **or** both **`min`** and **`max`** (not both modes at once in a way that violates the validator rules).
- Integers must be in **1–3999**; for range, **`min < max`**.
- On failure, the API returns **400** with JSON: `{ "error": "<message>" }`.

---

### Future enhancements

The current implementation focuses on correctness, testability, and observability. The architecture is intentionally extensible for production-grade systems. Potential enhancements include:

#### 1. Security and authentication

- Introduce OAuth2/OIDC-based authentication and authorization
- Support machine-to-machine (M2M) access using OAuth2 Client Credentials flow
- Integrate with an external identity provider such as AWS Cognito
- Configure the service as a resource server validating JWT access tokens
- Implement scope-based authorization (e.g. `roman.read`, `roman.range`)

#### 2. Observability and monitoring

- Build on existing New Relic integration (APM, logs, Micrometer OTLP metrics where enabled)
- Broaden OpenTelemetry usage beyond metrics (e.g. traces and log correlation via OTLP) or consolidate on a single collector
- Add distributed tracing for end-to-end request visibility
- Define alerts and dashboards for error rates, latency, and throughput
- Create New Relic alerts (e.g. high error rate, high latency, elevated 5xx, JVM memory pressure)

#### 3. Performance and load testing

- Introduce load and stress testing using tools such as JMeter or k6
- Measure system behavior under concurrent range requests
- Benchmark latency and throughput for different input sizes
- Identify bottlenecks and optimize thread pool configuration
- Add rate limiting to protect against excessive load

#### 4. Deployment and infrastructure

The service is **already containerized** (multi-stage `Dockerfile`, `docker compose` for local runs). Potential next steps:

- Publish images to a registry (e.g. Amazon ECR, GHCR) with immutable version tags and vulnerability scanning
- Deploy to AWS (ECS/Fargate, EKS, or EC2) with health checks, autoscaling, and rolling updates
- Use AWS CloudWatch (or equivalent) for centralized logging and platform metrics alongside application telemetry
- Manage infrastructure as code (e.g. Terraform, CloudFormation)
- Store secrets in AWS Secrets Manager or Parameter Store instead of plain `.env` in production

#### 5. Scalability and performance optimization

- Introduce caching (e.g. Redis) for frequently requested conversions
- Optimize parallel execution strategy for range queries
- Add horizontal scaling behind a load balancer
- Implement circuit breakers and resilience patterns

#### 6. API and feature enhancements

- Add reverse conversion (Roman → integer)
- Support batch conversion APIs
- Introduce API versioning strategy
- Enhance error handling with structured error responses
- Introduce pagination for future endpoints that may return larger datasets (e.g. conversion history, batch jobs, administrative reporting)

#### 7. Code quality and CI/CD

- Add CI pipelines (e.g. GitHub Actions) that run `mvn verify`, Spotless, and Docker image builds on every PR
- Integrate static analysis tools such as SonarQube
- Gate merges on quality checks and keep Spotless/Java style enforcement in CI

