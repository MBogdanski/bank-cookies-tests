# bank-cookies-tests

## Requirements

- [Docker](https://docs.docker.com/get-docker/)
- Maven Wrapper runs inside the container — no local JDK or Playwright install

Tests use `mcr.microsoft.com/playwright/java:v1.63.0-noble` (matches Playwright in `pom.xml`).

**First run:** Docker pulls the image once (~2 GB). That can take several minutes; later runs reuse the local cache.

## Running tests

All three browsers (same as CI):

```bash
./scripts/docker-test-all.sh
```

Single browser:

```bash
./scripts/docker-test.sh
./scripts/docker-test.sh firefox
./scripts/docker-test.sh webkit
```

Single test:

```bash
./scripts/docker-test.sh chromium -Dtest=CookieConsentTest#rejectAllCookiesShouldSetPolicyValueTo1
```

Maven dependencies are cached in the Docker volume `bank-cookies-tests-maven-repo`.

## GitHub Actions

Workflow [`.github/workflows/cookie-consent.yml`](.github/workflows/cookie-consent.yml) runs on every **push** and **pull request**.

- **Runner:** `ubuntu-latest` + host Docker
- **Command:** `./scripts/docker-test-all.sh` — one image pull, then chromium → firefox → webkit
- **Timeout:** 35 minutes (first run on a cold runner includes ~2 GB image pull)
- **On failure:** Surefire reports and screenshots as artifact `reports`
