package pl.ing.cookies.fixtures;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitUntilState;
import com.microsoft.playwright.PlaywrightException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.extension.TestWatcher;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public abstract class BrowserFixture {

    @RegisterExtension
    private final TestWatcher screenshotOnFailure = new TestWatcher() {
        @Override
        public void testFailed(ExtensionContext extensionContext, Throwable cause) {
            captureScreenshot(extensionContext);
        }
    };

    private static Playwright playwright;
    private static Browser browser;

    protected BrowserContext context;
    protected Page page;

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        String browserName = resolveBrowserName();
        boolean headed = Boolean.parseBoolean(System.getProperty("headed", "false"));

        BrowserType browserType = switch (browserName) {
            case "firefox" -> playwright.firefox();
            case "webkit" -> playwright.webkit();
            default -> playwright.chromium();
        };

        browser = browserType.launch(new BrowserType.LaunchOptions().setHeadless(!headed));
    }

    @BeforeEach
    void createContext() {
        context = browser.newContext(new Browser.NewContextOptions()
                .setLocale("pl-PL")
                .setViewportSize(1280, 720));
        if (isCi()) {
            context.route("**/*", route -> {
                String url = route.request().url();
                if (url.contains("hcaptcha.com")
                        || url.contains("hcaptcha.net")
                        || url.contains("google-analytics.com")
                        || url.contains("googletagmanager.com")
                        || url.contains("doubleclick.net")) {
                    route.abort();
                } else {
                    route.resume();
                }
            });
        }
        page = context.newPage();
        page.setDefaultNavigationTimeout(45_000);
        page.setDefaultTimeout(isCi() ? 20_000 : 30_000);
    }

    protected void openIngHome() {
        page.navigate("https://www.ing.pl/",
                new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
        assertIngHomeReachable();
    }

    private void assertIngHomeReachable() {
        String url = page.url();
        // Normal ing.pl HTML always embeds /_Incapsula_Resource as a script — only the main frame URL matters.
        if (url.contains("_Incapsula_Resource")) {
            throw new AssertionError("""
                    ing.pl blocked this client (Imperva/Incapsula). \
                    The cookie banner will not load — common for GitHub Actions datacenter IPs. \
                    Run tests locally or from a non-datacenter network.""");
        }

        Locator bannerAction = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Odrzuć wszystkie"))
                .or(page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Dostosuj")))
                .or(page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Zaakceptuj wszystkie")));

        try {
            bannerAction.first().waitFor(new Locator.WaitForOptions().setTimeout(isCi() ? 15_000 : 30_000));
        } catch (PlaywrightException e) {
            String hint = isCi()
                    ? " On CI this often means Imperva blocked the runner IP (check for _Incapsula_Resource in pw:api navigate logs)."
                    : "";
            throw new AssertionError("Cookie consent banner did not appear on www.ing.pl." + hint, e);
        }
    }

    private static boolean isCi() {
        return "true".equalsIgnoreCase(System.getenv("CI"));
    }

    @AfterEach
    void closeContext(TestInfo testInfo) {
        if (context != null) {
            context.close();
        }
    }

    @AfterAll
    static void closeBrowser() {
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

    private void captureScreenshot(ExtensionContext extensionContext) {
        if (page == null || page.isClosed()) {
            return;
        }
        try {
            Path dir = Path.of("target", "screenshots");
            Files.createDirectories(dir);
            String name = extensionContext.getDisplayName().replaceAll("[^a-zA-Z0-9_-]", "_");
            page.screenshot(new Page.ScreenshotOptions().setPath(dir.resolve(name + ".png")));
        } catch (Exception ignored) {
        }
    }

    private static String resolveBrowserName() {
        String fromProperty = System.getProperty("browser");
        if (fromProperty != null && !fromProperty.isBlank()) {
            return fromProperty.trim().toLowerCase(Locale.ROOT);
        }
        String fromEnv = System.getenv("BROWSER");
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv.trim().toLowerCase(Locale.ROOT);
        }
        return "chromium";
    }
}
