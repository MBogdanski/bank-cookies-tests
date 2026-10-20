package pl.ing.cookies.fixtures;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
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
        page = context.newPage();
        page.setDefaultTimeout(30_000);
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
            // best-effort artifact for CI
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
