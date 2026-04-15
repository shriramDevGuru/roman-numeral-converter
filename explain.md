# Design Notes — Roman Numeral API

## 1. Overview

Stateless Spring Boot service: integers in **1–3999** become Roman numerals over HTTP. Conversion and request rules live in plain Java; the framework handles transport, auth, and telemetry. There is nothing to persist or coordinate across requests, so the design stays intentionally small.

The implementation prioritizes correctness and a small, reviewable footprint for the assessment. It also includes a few production-oriented touches such as API security, metrics, and request correlation.

## 2. API Design Decisions

One **`GET /romannumeral`** keeps docs, tests, and clients simple. Callers use either **`query`** (single value) or **`min` + `max`** (inclusive range). The modes are mutually exclusive, so intent is clear and the server does not merge or infer parameters.

`RomanRequestValidator` owns all input rules (presence of params, bounds, `min < max`) and returns a sealed **`RequestMode`** (`Single` | `Range`). The controller only dispatches after validation, which keeps magic numbers and error text in one place.

## 3. Core Conversion Logic

Greedy **subtraction table**: fixed ordered pairs (value → symbol, including subtractive forms like `CM`). Walk the table, subtract while you can, append symbols. No recursion and no reverse parse; it is the usual approach for this bounded domain.

Work per value is effectively **O(1)** given the cap at 3999. Range requests reuse the same conversion logic for each integer.

## 4. Concurrency Design (Range Processing)

Each value in a range is independent, so **`CompletableFuture.supplyAsync`** with **`allOf` / `join`** is a simple fan-out.

Range tasks use a dedicated **`rangeExecutor`**: fixed pool size, configurable, with a CPU-based default. They do not use the JVM’s shared **`ForkJoinPool`**, so range conversion does not compete with other work on the common pool. That keeps behavior steadier in containers. A bounded queue and **`CallerRunsPolicy`** add backpressure so pending tasks do not pile up without limit.

Futures may finish in any order. The response is **sorted by numeric input** so the contract stays stable.

Trade-off: small ranges incur some scheduling overhead, while larger ranges benefit from parallel execution. Sorting is O(n log n) in range size, which is acceptable given the bounded input and the ability to utilize multiple cores compared to a purely sequential O(n) approach.

## 5. Error Handling and Validation

Expected bad input goes through **`InvalidRequestException`** (and explicit handling for missing params). **`@RestControllerAdvice`** maps those to **400** with **`{ "error": "…" }`**.

Unexpected failures map to **500** with a generic message. One JSON shape for client errors avoids controller special cases and keeps logs and metrics aligned with HTTP status.

## 6. Observability and Metrics

A request filter runs early in the chain. It accepts or generates **`x-request-id`**, stores it in **MDC**, and logs method, path, status, and duration once per request. That lines up logs with upstream gateways and clients.

**Micrometer** registers counters (single vs range vs invalid) and a latency timer for **`/romannumeral`**, using the same registry for local dashboards and export.

**OTLP → New Relic** is optional: flip it on where you actually run the service so local dev does not depend on a vendor. Same meters, different sink.

## 7. Security Approach

A configurable header carries a shared secret, which fits a stateless, machine-to-machine API. Comparison is **constant-time** to avoid leaking the key via timing.

For a real multi-tenant or user-facing product I would replace this with OAuth2 client credentials or an IdP-issued JWT (audience/scopes), plus rate limits and proper key rotation outside the process. Here the goal was a clear gate without pretending to be a full identity system.

## 8. Trade-offs and Scope Decisions

No database: outputs are derived from inputs only, so nothing needs durable storage. No microservices split: one feature, one deployable, fewer moving parts. That scope keeps review focused and matches the problem size.

## 9. Future Enhancements

- Add rate limiting on /romannumeral (especially for large ranges) to protect the service from heavy requests and return proper 429 responses.
- Use a hybrid approach for range processing: sequential for small ranges and parallel for larger ranges to reduce unnecessary overhead.
- Add alerting on existing metrics (latency, error rate, request volume) to detect issues early.
- Deploy to a cloud environment (e.g., AWS) with autoscaling and load balancing for better scalability.
- Introduce CI/CD pipelines to automate build, test, and deployment processes.

## Use of AI

I used AI to speed up planning and implementation, not to replace judgment. I owned the design, correctness, trade-offs, and what went into the final submission.

### 1. Tools used
- Cursor AI: primary IDE with AI-assisted development; used for multi-file edits and iterative changes across the codebase
- ChatGPT (OpenAI): used for structured planning, approach validation, trade-off discussion, and refining explanations

### 2. How AI was used

- Planning and requirements breakdown (API behavior, edge cases)
- Scaffolding and boilerplate support
- Debugging help and issue walkthroughs
- Documentation refinement (README.md, explain.md)
- Light code review support (sanity-checking approaches and alternatives)

### 3. What I owned directly

- Core Roman numeral conversion logic and how it behaves for valid inputs
- Time complexity analysis for conversion and range handling
- API design and validation (single vs range, errors)
- Concurrency and trade-off reasoning for range requests (ordering, parallelism, bounds)
- Observability, security scope, and overall project scope
- Final decision-making: what to accept, change, or reject from AI output

### 4. Verification and review process

- Design-first: clarified behavior before writing much code
- Manually reviewed AI suggestions and generated edits before keeping them
- Refined code and docs for consistency with the intended structure and style
- Tested the service and validated behavior end to end, including representative valid and invalid cases
