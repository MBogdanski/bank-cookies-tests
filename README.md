# bank-cookies-tests

Test rekrutacyjny E2E: scenariusz zgody na **cookies analityczne** na [ing.pl](https://www.ing.pl) (Java 21, JUnit 5, Playwright).

Repozytorium: [github.com/MBogdanski/bank-cookies-tests](https://github.com/MBogdanski/bank-cookies-tests)

## Wymagania

- **JDK 21** (LTS)
- Maven globalny jest **opcjonalny** — w projekcie jest Maven Wrapper (`./mvnw`)

## Instalacja przeglądarek Playwright

Przed pierwszym uruchomieniem testów lokalnie:

```bash
./mvnw exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install --with-deps"
```

Na macOS/Linux wystarczy często samo `install chromium` (lub wybrana przeglądarka).

## Uruchomienie testów

Domyślnie **Chromium**, tryb headless:

```bash
./mvnw test
```

Inna przeglądarka:

```bash
./mvnw test -Dbrowser=firefox
./mvnw test -Dbrowser=webkit
```

Podgląd okna przeglądarki (debug):

```bash
./mvnw test -Dheaded=true
```

## Scenariusz

1. Wejście na `https://www.ing.pl/` w czystym kontekście (bez zapisanych cookies).
2. **Dostosuj** → włączenie **Cookies analityczne**, wyłączenie **Cookies marketingowe**.
3. **Zaakceptuj zaznaczone**.
4. Asercje:
   - obecność `cookiePolicyGDPR` i `cookiePolicyGDPR__details`
   - wartość `cookiePolicyGDPR=3` (techniczne + analityczne, bez marketingu)
   - po przeładowaniu strony — co najmniej jedno z: `_ga`, `_ga_*`, `_gid`
   - brak typowych cookies marketingowych (`_fbp`, `IDE`, `_gcl_*`)

Implementacja: Page Object [`CookieConsentPage`](src/test/java/pl/ing/cookies/pages/CookieConsentPage.java), test [`CookieConsentAnalyticsTest`](src/test/java/pl/ing/cookies/CookieConsentAnalyticsTest.java).

## CI (GitHub Actions)

Workflow [`.github/workflows/cookie-consent.yml`](.github/workflows/cookie-consent.yml) uruchamia ten sam test równolegle na **Chromium, Firefox i WebKit** (`strategy.matrix`, `fail-fast: false`).

Wyniki: [Actions](https://github.com/MBogdanski/bank-cookies-tests/actions)

Przy błędzie job uploaduje raporty Surefire i zrzuty ekranu z `target/screenshots/`.

## Powtarzalność

- Nowy `BrowserContext` na każdy test (locale `pl-PL`, viewport 1280×720).
- Headless w CI; lokalnie domyślnie headless.
- Oczekiwania na elementy UI zamiast stałych opóźnień (poza krótkim pollingiem na cookies GA po reload).
