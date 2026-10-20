# bank-cookies-tests

## Requirements

- JDK 21
- Maven Wrapper (`./mvnw`)

## Running tests

First time per browser — OS libs + browser binary ([Playwright Java](https://playwright.dev/java/docs/browsers)):

```bash
chmod +x mvnw
./mvnw -B install -DskipTests --no-transfer-progress
./mvnw -B exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install-deps chromium"
./mvnw -B exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install chromium"
```

Repeat `install-deps` / `install` for `firefox` (and `webkit` if needed). Do **not** run bare `install` or `install --with-deps` without a browser name — that pulls **all** browsers.

Run tests:

```bash
./mvnw test
./mvnw test -Dbrowser=firefox
```

If you see a false “missing dependencies” warning after install on Linux, see [playwright#30592](https://github.com/microsoft/playwright/issues/30592). CI sets `PLAYWRIGHT_SKIP_VALIDATE_HOST_REQUIREMENTS=true` when deps were installed via `install-deps`.

## GitHub Actions

[`.github/workflows/cookie-consent.yml`](.github/workflows/cookie-consent.yml): `ubuntu-latest`, matrix **chromium** / **firefox** / **webkit**, system libraries via `apt`, then Playwright `install-deps` + `install`, `./mvnw test`.
