# bank-cookies-tests

## Requirements

- JDK 21
- Maven 3.9+

## Running tests

First run — build and install Playwright browsers ([ci-intro](https://playwright.dev/java/docs/ci-intro)):

```bash
mvn -B install -DskipTests --no-transfer-progress
mvn -B exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install --with-deps"
```

Run tests:

```bash
mvn test
mvn test -Dbrowser=firefox
mvn test -Dbrowser=webkit
```

Single test:

```bash
mvn test -Dtest=CookieConsentTest#rejectAllCookiesShouldSetPolicyValueTo1
```

## GitHub Actions

[`.github/workflows/cookie-consent.yml`](.github/workflows/cookie-consent.yml): `ubuntu-latest`, `mvn`, `install --with-deps`, matrix **chromium** / **firefox** / **webkit** with `-Dbrowser=…`.

Live E2E against ing.pl may fail on GitHub-hosted runners when **Imperva/Incapsula** blocks datacenter IPs (no cookie banner). Tests pass locally; CI logs show `_Incapsula_Resource` redirects. Use a self-hosted runner or run `./mvn test` locally for a full green run against production.
