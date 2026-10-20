package pl.ing.cookies;

import com.microsoft.playwright.options.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.ing.cookies.fixtures.BrowserFixture;
import pl.ing.cookies.pages.CookieConsentModal;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CookieConsentTest extends BrowserFixture {

    @Test
    @DisplayName("Reject all cookies should set cookiePolicyGDPR to 1")
    void rejectAllCookiesShouldSetPolicyValueTo1() {
        openIngHome();
        CookieConsentModal.on(page).rejectAll();

        assertGdprCookiesPresent();
        assertEquals("1", cookiesByName().get("cookiePolicyGDPR"));
    }

    @Test
    @DisplayName("Marketing-only consent should set cookiePolicyGDPR to 2")
    void marketingOnlyConsentShouldSetPolicyValueTo2() {
        openIngHome();
        CookieConsentModal consent = CookieConsentModal.on(page);
        consent.customize();
        consent.selectMarketingOnly();
        consent.confirmSelection();

        assertGdprCookiesPresent();
        assertEquals("2", cookiesByName().get("cookiePolicyGDPR"));
    }

    @Test
    @DisplayName("Analytics-only consent should set cookiePolicyGDPR to 3")
    void analyticsOnlyConsentShouldSetPolicyValueTo3() {
        openIngHome();
        CookieConsentModal consent = CookieConsentModal.on(page);
        consent.customize();
        consent.selectAnalyticsOnly();
        consent.confirmSelection();

        assertGdprCookiesPresent();
        assertEquals("3", cookiesByName().get("cookiePolicyGDPR"));
    }

    @Test
    @DisplayName("Accept all cookies should set cookiePolicyGDPR to 4")
    void acceptAllCookiesShouldSetPolicyValueTo4() {
        openIngHome();
        CookieConsentModal.on(page).acceptAll();

        assertGdprCookiesPresent();
        assertEquals("4", cookiesByName().get("cookiePolicyGDPR"));
    }

    @Test
    @DisplayName("Custom selection of all consents should set cookiePolicyGDPR to 4")
    void customSelectionOfAllConsentsShouldSetPolicyValueTo4() {
        openIngHome();
        CookieConsentModal consent = CookieConsentModal.on(page);
        consent.customize();
        consent.selectAllConsents();
        consent.confirmSelection();

        assertGdprCookiesPresent();
        assertEquals("4", cookiesByName().get("cookiePolicyGDPR"));
    }

    @Test
    @DisplayName("Disabling technical cookies in DOM should still set cookiePolicyGDPR to 1")
    void disablingTechnicalCookiesInDomShouldStillSetPolicyValueTo1() {
        openIngHome();
        CookieConsentModal consent = CookieConsentModal.on(page);
        consent.customize();
        consent.attemptToDisableTechnicalCookies();

        assertFalse(consent.isTechnicalConsentEnabled(),
                "After UI bypass attempt, technical consent should appear disabled in the DOM");

        consent.confirmSelection();

        assertGdprCookiesPresent();
        assertEquals("1", cookiesByName().get("cookiePolicyGDPR"));
    }

    @Test
    @DisplayName("Page reload should preserve GDPR consent cookies after reject all")
    void reloadShouldPreserveGdprConsentCookiesAfterRejectAll() {
        openIngHome();
        CookieConsentModal.on(page).rejectAll();

        String policyBeforeReload = cookiesByName().get("cookiePolicyGDPR");
        String detailsBeforeReload = cookiesByName().get("cookiePolicyGDPR__details");
        assertNotNull(policyBeforeReload);
        assertNotNull(detailsBeforeReload);

        page.reload();

        assertGdprCookiesPresent();
        assertEquals(policyBeforeReload, cookiesByName().get("cookiePolicyGDPR"));
        assertEquals(detailsBeforeReload, cookiesByName().get("cookiePolicyGDPR__details"));
    }

    @Test
    @DisplayName("Pre-seeded consent cookie should not hide the consent modal")
    void preseededConsentShouldNotHideConsentModal() {
        seedGdprCookies("4");
        openIngHome();

        CookieConsentModal modal = CookieConsentModal.on(page);
        modal.waitForOpen();
        assertTrue(modal.isOpen(),
                "Injected cookiePolicyGDPR on www.ing.pl must not skip the consent modal");
    }

    @Test
    @DisplayName("Pre-seeded www cookie should remain while reject all writes CMP cookie on parent domain")
    void preseededWwwCookieShouldRemainWhileRejectWritesCmpCookieOnParentDomain() {
        seedGdprCookies("4");
        openIngHome();

        CookieConsentModal.on(page).rejectAll();

        assertEquals("4", requireCookiePolicyGdprOnDomain(WWW_ING_PL),
                "Pre-seeded cookiePolicyGDPR on www.ing.pl is not removed by reject all");
        assertEquals("1", requireCookiePolicyGdprOnDomain(".ing.pl"),
                "Reject all still persists CMP decision on .ing.pl");

        Set<String> policyValuesForWwwUrl = cookiePolicyGdprValuesForWwwUrl();
        assertTrue(policyValuesForWwwUrl.contains("4"),
                "https://www.ing.pl/ should include pre-seeded cookiePolicyGDPR=4 (www.ing.pl)");
        assertTrue(policyValuesForWwwUrl.contains("1"),
                "https://www.ing.pl/ should include CMP cookiePolicyGDPR=1 (.ing.pl)");
    }

    private void openIngHome() {
        page.navigate("https://www.ing.pl/");
    }

    private static final String WWW_ING_PL = "www.ing.pl";

    private void seedGdprCookies(String policyValue) {
        context.addCookies(List.of(
                new Cookie("cookiePolicyGDPR", policyValue).setDomain(WWW_ING_PL).setPath("/"),
                new Cookie("cookiePolicyGDPR__details", "{\"cookieCreateTimestamp\":1}")
                        .setDomain(WWW_ING_PL)
                        .setPath("/")
        ));
    }

    private String requireCookiePolicyGdprOnDomain(String domain) {
        return cookiePolicyGdprOnDomain(domain).orElseThrow(() -> new RuntimeException(
                "Expected cookiePolicyGDPR cookie on domain '" + domain + "', but none was present"));
    }

    private Optional<String> cookiePolicyGdprOnDomain(String domain) {
        return context.cookies().stream()
                .filter(cookie -> "cookiePolicyGDPR".equals(cookie.name) && domain.equals(cookie.domain))
                .map(cookie -> cookie.value)
                .findFirst();
    }

    private Set<String> cookiePolicyGdprValuesForWwwUrl() {
        return context.cookies("https://www.ing.pl/").stream()
                .filter(cookie -> "cookiePolicyGDPR".equals(cookie.name))
                .map(cookie -> cookie.value)
                .collect(Collectors.toSet());
    }

    private void assertGdprCookiesPresent() {
        Map<String, String> cookies = cookiesByName();
        assertTrue(cookies.containsKey("cookiePolicyGDPR"));
        assertTrue(cookies.containsKey("cookiePolicyGDPR__details"));
    }

    private Map<String, String> cookiesByName() {
        List<Cookie> cookies = context.cookies();
        return cookies.stream().collect(Collectors.toMap(
                cookie -> cookie.name,
                cookie -> cookie.value,
                (existing, replacement) -> replacement));
    }
}
