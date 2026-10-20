# bank-cookies-tests

## Requirements

- JDK 21
- Maven Wrapper (`./mvnw`)

## Running tests

First time only — install browsers and OS libraries ([Playwright Java CI](https://playwright.dev/java/docs/ci-intro)):

```bash
chmod +x mvnw
./mvnw -B install -DskipTests --no-transfer-progress
./mvnw -B exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install --with-deps chromium"
```

For all three browsers locally, repeat with `firefox` and `webkit`, or:

```bash
for b in chromium firefox webkit; do
  ./mvnw -B exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install --with-deps $b"
done
```

Run tests:

```bash
./mvnw test
./mvnw test -Dbrowser=firefox
./mvnw test -Dbrowser=webkit
```

Single test:

```bash
./mvnw test -Dtest=CookieConsentTest#rejectAllCookiesShouldSetPolicyValueTo1
```

## GitHub Actions

[`.github/workflows/cookie-consent.yml`](.github/workflows/cookie-consent.yml) on `ubuntu-latest`:

1. `install -DskipTests`
2. `install-deps` + `install` per matrix browser
3. `mvn test -Dbrowser=…`

Matrix: **chromium**, **firefox** in parallel (WebKit temporarily disabled in CI).
