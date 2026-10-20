package pl.ing.cookies;

import com.microsoft.playwright.options.Cookie;
import org.junit.jupiter.api.Test;
import pl.ing.cookies.fixtures.BrowserFixture;
import pl.ing.cookies.pages.CookieConsentPage;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CookieConsentAnalyticsTest extends BrowserFixture {

    /** ING cookie consent bitmap: 1 = reject all, 3 = technical + analytics, 4 = all categories. */
    private static final String EXPECTED_POLICY_ANALYTICS_ONLY = "3";

    private static final Set<String> MARKETING_COOKIES = Set.of("_fbp", "IDE", "_gcl_au", "_gcl_aw", "_gcl_dc");

    @Test
    void acceptsOnlyAnalyticsCookiesOnIngPl() {
        page.navigate("https://www.ing.pl/");

        CookieConsentPage consent = new CookieConsentPage(page);
        consent.waitForBanner();
        consent.clickCustomize();
        consent.enableAnalyticsOnly();
        consent.acceptSelected();
        consent.waitConsentSaved();

        Map<String, String> cookies = cookiesByName();
        assertTrue(cookies.containsKey("cookiePolicyGDPR"));
        assertTrue(cookies.containsKey("cookiePolicyGDPR__details"));
        assertEquals(EXPECTED_POLICY_ANALYTICS_ONLY, cookies.get("cookiePolicyGDPR"));

        page.reload();
        assertEventually(() -> hasGoogleAnalyticsCookie(cookiesByName()), 20_000L);

        for (String marketing : MARKETING_COOKIES) {
            assertFalse(cookiesByName().containsKey(marketing), "Unexpected marketing cookie: " + marketing);
        }
    }

    private Map<String, String> cookiesByName() {
        List<Cookie> cookies = context.cookies();
        return cookies.stream().collect(Collectors.toMap(c -> c.name, c -> c.value, (a, b) -> b));
    }

    private static boolean hasGoogleAnalyticsCookie(Map<String, String> cookies) {
        return cookies.containsKey("_ga")
                || cookies.containsKey("_gid")
                || cookies.keySet().stream().anyMatch(name -> name.startsWith("_ga_"));
    }

    private void assertEventually(BooleanSupplier condition, long timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            if (condition.getAsBoolean()) {
                return;
            }
            page.waitForTimeout(500);
        }
        assertTrue(condition.getAsBoolean(), "Condition not met within " + timeoutMs + "ms");
    }
}
