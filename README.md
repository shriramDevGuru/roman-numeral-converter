## Roman Numeral Converter API

Spring Boot REST API that converts integers to Roman numerals.

### Endpoints

- **GET** `/romannumeral?query={integer}`
- **GET** `/romannumeral?min={integer}&max={integer}`

### Auth (temporary)

This service currently uses a simple API-key gate:

- Send header **`x-api-key`** (configurable via `API_KEY_HEADER`)
- Configure the expected key via **`APP_API_KEY`**

Example:

```bash
curl -sS \
  -H "x-api-key: my-test-key" \
  "http://localhost:8080/romannumeral?query=9"
```

### Local run (Docker)

1. Copy `.env.example` to `.env` and set `APP_API_KEY`:

```bash
cp .env.example .env
```

2. Start:

```bash
docker compose up -d --build
```

### Build & quality (local Maven, JDK 17)

From the repo root:

```bash
mvn spotless:check    # formatting (use mvn spotless:apply to fix)
mvn test              # unit tests
mvn verify            # unit + integration tests + JaCoCo reports
```

JaCoCo HTML reports after `mvn verify`:

- Unit tests: `target/site/jacoco-ut/index.html`
- Integration tests: `target/site/jacoco-it/index.html`

### Validation behavior

- `query` OR (`min` and `max`) must be provided
- values must be 1..3999
- `min < max`
- invalid input returns **HTTP 400** with JSON `{ "error": "..." }`

### Observability (lightweight)

- Actuator: `/actuator/health`, `/actuator/metrics`
- Micrometer metrics are emitted via Actuator.

Future integration with systems like **Datadog** or **New Relic** is straightforward since the app already uses **Actuator + Micrometer**.