package pl.ing.cookies.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;

public class CookieConsentModal {

    private final Locator root;
    private final Locator technicalSwitch;
    private final Locator analyticsSwitch;
    private final Locator marketingSwitch;

    public CookieConsentModal(Locator root) {
        this.root = root;
        this.technicalSwitch = root.locator("[role=switch][name='CpmTechnicalOption']");
        this.analyticsSwitch = root.locator("[role=switch][name='CpmAnalyticalOption']");
        this.marketingSwitch = root.locator("[role=switch][name='CpmMarketingOption']");
    }

    public static CookieConsentModal on(Page page) {
        return new CookieConsentModal(page.getByRole(AriaRole.DIALOG));
    }

    public void customize() {
        button("Dostosuj").click();
        button("Wyjdź z ustawień").waitFor();
    }

    public void selectAnalyticsOnly() {
        analyticsSwitch.click();
    }

    public void selectMarketingOnly() {
        marketingSwitch.click();
    }

    public void selectAllConsents() {
        analyticsSwitch.click();
        marketingSwitch.click();
    }

    public void attemptToDisableTechnicalCookies() {
        technicalSwitch.evaluate("""
                el => {
                  el.setAttribute('aria-checked', 'false');
                  el.classList.remove('state-disabled');
                  el.removeAttribute('aria-disabled');
                }
                """);
    }

    public boolean isTechnicalConsentEnabled() {
        return "true".equals(technicalSwitch.getAttribute("aria-checked"));
    }

    public void confirmSelection() {
        button("Zaakceptuj zaznaczone").click();
        waitForModalClosed();
    }

    public void rejectAll() {
        button("Odrzuć wszystkie").click();
        waitForModalClosed();
    }

    public void acceptAll() {
        button("Zaakceptuj wszystkie").click();
        waitForModalClosed();
    }

    public void waitForOpen() {
        root.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
    }

    public boolean isOpen() {
        return root.isVisible();
    }

    private Locator button(String accessibleName) {
        return root.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName(accessibleName));
    }

    private void waitForModalClosed() {
        root.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
    }
}
