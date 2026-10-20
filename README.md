# bank-cookies-tests

## Requirements

- JDK 21
- Maven Wrapper (`./mvnw`) — a global Maven install is optional

## Playwright browsers

Run once before the first local test run:

```bash
./mvnw exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install --with-deps"
```

On macOS/Linux, installing a single browser is often enough, e.g. `install chromium`.

## Running tests

Default: Chromium, headless.

```bash
./mvnw test
```

Other browsers:

```bash
./mvnw test -Dbrowser=firefox
./mvnw test -Dbrowser=webkit
```

Headed mode (debug):

```bash
./mvnw test -Dheaded=true
```

Run a single test class or method:

```bash
./mvnw test -Dtest=CookieConsentTest
./mvnw test -Dtest=CookieConsentTest#rejectAllCookiesShouldSetPolicyValueTo1
```

## GitHub Actions

Workflow [`.github/workflows/cookie-consent.yml`](.github/workflows/cookie-consent.yml) runs on every **push** and **pull request**.

- **Runner:** `ubuntu-latest`
- **JDK:** 21 (Temurin), Maven dependency cache enabled
- **Matrix:** `chromium`, `firefox`, `webkit` in parallel (`fail-fast: false`)
- **Steps:** checkout → compile test sources → install the matrix browser with Playwright (`install --with-deps`) → `./mvnw -B test -Dbrowser=<matrix browser>`
- **On failure:** uploads Surefire reports (`target/surefire-reports/`) and screenshots (`target/screenshots/`) as artifacts named `reports-<browser>`
