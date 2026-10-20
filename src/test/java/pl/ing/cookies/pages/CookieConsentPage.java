package pl.ing.cookies.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;

public class CookieConsentPage {

    private static final String ANALYTICS_SWITCH = "[aria-labelledby='header-2-analytical'][role='switch']";
    private static final String MARKETING_SWITCH = "[aria-labelledby='header-3-marketing'][role='switch']";

    private final Page page;

    public CookieConsentPage(Page page) {
        this.page = page;
    }

    public void waitForBanner() {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Dostosuj")).waitFor();
    }

    public void clickCustomize() {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Dostosuj")).click();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Wyjdź z ustawień")).waitFor();
    }

    public void enableAnalyticsOnly() {
        setSwitchChecked(ANALYTICS_SWITCH, true);
        setSwitchChecked(MARKETING_SWITCH, false);
    }

    public void acceptSelected() {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Zaakceptuj zaznaczone")).click();
    }

    public void waitConsentSaved() {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Wyjdź z ustawień"))
                .waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
    }

    private void setSwitchChecked(String selector, boolean checked) {
        Locator toggle = page.locator(selector);
        toggle.waitFor();
        boolean isChecked = "true".equals(toggle.getAttribute("aria-checked"));
        if (isChecked != checked) {
            toggle.click();
        }
    }
}
