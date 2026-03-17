# Performance Test (k6)

This directory contains a k6 script for load testing the Exhibition Recommender backend API.

## Prerequisites

- [k6](https://k6.io/) installed. On macOS with Homebrew: `brew install k6`
- The backend application running (locally or remotely). The script assumes the base URL can be set via the `BASE_URL` environment variable (defaults to `http://localhost:8080`).
- For endpoints that require authentication, set the `ACCESS_TOKEN` environment variable with a valid JWT token.

## Running the Test

### Locally

1. Start the application (e.g., via `./gradlew bootRun` or your IDE).
2. Run the k6 script:

   ```bash
   k6 run api-load-test.js
   ```

   To specify a different base URL or token:

   ```bash
   BASE_URL=http://myhost:8080 ACCESS_TOKEN=my-token k6 run api-load-test.js
   ```

### In CI (GitHub Actions)

You can add a step to your CI workflow to run the load test. Example:

```yaml
- name: Set up k6
  uses: loadimpact/k6-action@v3
- name: Run load test
  env:
    BASE_URL: http://localhost:8080
    ACCESS_TOKEN: ${{ secrets.ACCESS_TOKEN }}
  run: k6 run performance-test/api-load-test.js
```

## Test Configuration

The script ramps up to 20 virtual users over 2 minutes, maintains that load for 5 minutes, then ramps down.

It tests the following endpoints:
- `GET /api/exhibitions`
- `GET /api/exhibitions/popular`
- `GET /api/records`
- `GET /api/bookmarks/my` (requires auth)
- `GET /api/likes/my` (requires auth)

Thresholds:
- 95% of HTTP responses must be under 500ms.
- Custom waiting time metric (time until first byte) must be under 400ms for 95% of requests.

## Results

k6 will output a summary to the console. For more detailed output, consider using the `--out` flag to send results to a database (e.g., InfluxDB + Grafana) or to JSON.

Example:
```bash
k6 run --out json=result.json api-load-test.js
```