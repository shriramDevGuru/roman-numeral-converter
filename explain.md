# Design Notes — Roman Numeral API

## 1. Overview

Stateless Spring Boot service: integers in **1–3999** become Roman numerals over HTTP. Conversion and request rules live in plain Java; the framework handles transport, auth, and telemetry. There is nothing to persist or coordinate across requests, so the design stays intentionally small. The implementation prioritizes correctness and a small, reviewable footprint for the assessment, while still folding in proportional production-readiness (API security, metrics, request correlation).

## 2. API Design Decisions

One **`GET /romannumeral`** keeps docs, tests, and clients simple. Callers use either **`query`** (single value) or **`min` + `max`** (inclusive range); the modes are mutually exclusive, so request intent is unambiguous and the server does not merge or infer parameters.

`RomanRequestValidator` owns all input rules (presence of params, bounds, `min < max`) and returns a sealed **`RequestMode`** (`Single` | `Range`). The controller only dispatches after validation, which keeps magic numbers and error text in one place.

## 3. Core Conversion Logic

Greedy **subtraction table**: fixed ordered pairs (value → symbol, including subtractive forms like `CM`). Walk the table, subtract while you can, append symbols. No recursion and no reverse parse; it is the usual approach for this bounded domain.

Work per value is effectively **O(1)** given the cap at 3999. Range requests reuse the same conversion for each integer.

## 4. Concurrency Design (Range Processing)

Each value in a range is independent, so **`CompletableFuture.supplyAsync`** plus **`allOf` / `join`** is a straightforward fan-out. Tasks run on a dedicated **`rangeExecutor`** (fixed size, configurable, CPU-based default) instead of the JVM’s shared **`ForkJoinPool`**, so range work cannot starve other fork-join usage and stays predictable in containers. A bounded queue and **`CallerRunsPolicy`** add backpressure instead of accepting an unbounded backlog of pending tasks.

Futures may finish in any order; the response is **sorted by numeric input** so the contract is stable. Trade-off: tiny ranges pay scheduling overhead; wide ranges benefit from parallelism. Sorting is **O(n log n)** in span length, which is acceptable here versus a single-threaded **O(n)** loop that would not use multiple cores.

## 5. Error Handling and Validation

Expected bad input goes through **`InvalidRequestException`** (and explicit handling for missing params). **`@RestControllerAdvice`** maps those to **400** with **`{ "error": "…" }`**. Unexpected failures map to **500** with a generic message. One JSON shape for client errors avoids controller special cases and keeps logs and metrics aligned with HTTP status.

## 6. Observability and Metrics

Request filter (early in the chain): accept or generate **`x-request-id`**, store it in **MDC**, and log method, path, status, and duration once per request so logs line up with upstream gateways and clients.

**Micrometer** registers counters (single vs range vs invalid) and a latency timer for **`/romannumeral`**, using the same registry for local dashboards and export.

**OTLP → New Relic** is optional: flip it on where you actually run the service so local dev does not depend on a vendor. Same meters, different sink.

## 7. Security Approach

A configurable header carries a shared secret, which fits a stateless, machine-to-machine API. Comparison is **constant-time** to avoid leaking the key via timing.

For a real multi-tenant or user-facing product I would replace this with OAuth2 client credentials or an IdP-issued JWT (audience/scopes), plus rate limits and proper key rotation outside the process. Here the goal was a clear gate without pretending to be a full identity system.

## 8. Trade-offs and Scope Decisions

No database: outputs are derived from inputs only, so nothing needs durable storage. No microservices split: one feature, one deployable, fewer moving parts.

## 9. Future Enhancements

- Add rate limiting on **`/romannumeral`** (especially for large ranges) to protect the service from heavy requests and return proper **429** responses.
- Use a hybrid approach for range processing: sequential for small ranges and parallel for larger ranges to reduce unnecessary overhead.
- Add alerting on existing metrics (latency, error rate, request volume) to detect issues early.
- Deploy to a cloud environment (e.g., AWS) with autoscaling and load balancing for better scalability.
- Introduce CI/CD pipelines to automate build, test, and deployment processes.
