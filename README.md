# bank-cookies-tests

## Requirements

- [Docker](https://docs.docker.com/get-docker/) with Compose (`docker compose`)

Tests run inside the official Playwright Java image (`v1.63.0-noble`, aligned with `pom.xml`). No local JDK or browser install is required.

**First run:** Docker downloads the image once (~2 GB, browsers + OS libs). That can take several minutes; later runs reuse the local cache and start much faster.

Optional prefetch:

```bash
docker pull mcr.microsoft.com/playwright/java:v1.63.0-noble
```

## Running tests

Default: Chromium, headless.

```bash
./scripts/docker-test.sh
```

Other browsers:

```bash
./scripts/docker-test.sh firefox
./scripts/docker-test.sh webkit
```

Single test class or method:

```bash
./scripts/docker-test.sh chromium -Dtest=CookieConsentTest
./scripts/docker-test.sh chromium -Dtest=CookieConsentTest#rejectAllCookiesShouldSetPolicyValueTo1
```

Maven dependencies are cached in the Docker volume `maven-repo` between runs.

## GitHub Actions

Workflow [`.github/workflows/cookie-consent.yml`](.github/workflows/cookie-consent.yml) runs on every **push** and **pull request**.

- **Runner:** `ubuntu-latest` (host Docker, not a nested job container)
- **Command:** same as locally — `./scripts/docker-test.sh <browser>`
- **Matrix:** `chromium`, `firefox`, `webkit` in parallel (`fail-fast: false`)
- **Timeout:** 25 minutes per job
- **On failure:** uploads Surefire reports and screenshots as `reports-<browser>`
