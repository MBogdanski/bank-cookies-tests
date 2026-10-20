# bank-cookies-tests

## Requirements

**Recommended (fast):** JDK 21 + Maven Wrapper (`./mvnw`). Playwright browsers installed on first run via the script below.

**Optional:** [Docker](https://docs.docker.com/get-docker/) — same stack as before, but pulls a ~2 GB image once; use only if you want an isolated environment without local browser installs.

## Running tests (lightweight)

First run installs one browser and OS libraries (minutes once per machine); later runs are ~10–20 s.

```bash
./scripts/local-test.sh
```

Other browsers:

```bash
./scripts/local-test.sh firefox
./scripts/local-test.sh webkit
```

Skip Playwright install when browsers are already present:

```bash
SKIP_PLAYWRIGHT_INSTALL=1 ./scripts/local-test.sh chromium
```

Single test:

```bash
./scripts/local-test.sh chromium -Dtest=CookieConsentTest#rejectAllCookiesShouldSetPolicyValueTo1
```

## Running tests (Docker, optional)

```bash
./scripts/docker-test.sh
./scripts/docker-test.sh firefox
```

First run downloads `mcr.microsoft.com/playwright/java:v1.63.0-noble` (~2 GB, can take several minutes). Subsequent runs reuse the cached image.

## GitHub Actions

Workflow [`.github/workflows/cookie-consent.yml`](.github/workflows/cookie-consent.yml) runs on every **push** and **pull request**.

- **Runner:** `ubuntu-latest` (no Docker image pull)
- **Per matrix job:** Playwright CLI `install --with-deps <browser>` then `./mvnw -B test`
- **Matrix:** `chromium`, `firefox`, `webkit` in parallel (`fail-fast: false`)
- **Typical time:** a few minutes per job (mostly first-time apt + browser download on a clean runner), tests ~15 s
- **On failure:** Surefire reports and screenshots as `reports-<browser>`

Run all three browsers locally (same as CI matrix):

```bash
./scripts/local-test-all.sh
```
