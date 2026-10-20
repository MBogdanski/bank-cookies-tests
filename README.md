# bank-cookies-tests

Testy E2E zgód cookies na [ing.pl](https://www.ing.pl) (Java 21, JUnit 5, Playwright) — cztery warianty wartości `cookiePolicyGDPR` (1–4).

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

## Scenariusze

Każdy test startuje w czystym kontekście na `https://www.ing.pl/` (poza testem nadpisania cookies) i po akcji w banerze sprawdza `cookiePolicyGDPR` oraz `cookiePolicyGDPR__details`. Cookies techniczne są zawsze aktywne w banerze.

| Test | Akcja w banerze | Oczekiwane `cookiePolicyGDPR` |
|------|-----------------|-------------------------------|
| Odrzuć wszystkie | **Odrzuć wszystkie** | `1` |
| Tylko marketingowe | **Dostosuj** → marketing ON → **Zaakceptuj zaznaczone** | `2` |
| Tylko analityczne | **Dostosuj** → analityczne ON, marketing OFF → **Zaakceptuj zaznaczone** | `3` |
| Wszystkie | **Zaakceptuj wszystkie** | `4` |
| Obejście UI (techniczne) | **Dostosuj** → manipulacja DOM switcha technicznego → **Zaakceptuj zaznaczone** | `1` |
| Nadpisanie cookies | `addCookies` z `cookiePolicyGDPR=4` → **Odrzuć wszystkie** | `1` na `.ing.pl` |
| Trwałość po reload | **Odrzuć wszystkie** → `page.reload()` | te same wartości `cookiePolicyGDPR` i `cookiePolicyGDPR__details` |

Implementacja: [`CookieConsentModal`](src/test/java/pl/ing/cookies/pages/CookieConsentModal.java), [`CookieConsentTest`](src/test/java/pl/ing/cookies/CookieConsentTest.java).

## CI (GitHub Actions)

Workflow [`.github/workflows/cookie-consent.yml`](.github/workflows/cookie-consent.yml) uruchamia ten sam test równolegle na **Chromium, Firefox i WebKit** (`strategy.matrix`, `fail-fast: false`).

Wyniki: [Actions](https://github.com/MBogdanski/bank-cookies-tests/actions)

Przy błędzie job uploaduje raporty Surefire i zrzuty ekranu z `target/screenshots/`.

## Powtarzalność

- Nowy `BrowserContext` na każdy test (locale `pl-PL`, viewport 1280×720).
- Headless w CI; lokalnie domyślnie headless.
- Oczekiwania na elementy UI zamiast stałych opóźnień (poza krótkim pollingiem na cookies GA po reload).
