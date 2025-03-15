package com.framework.core;

import com.framework.config.Configuration;
import com.framework.utils.LoggerUtil;
import com.microsoft.playwright.*;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class BrowserFactory {
    private static final LoggerUtil LOGGER = new LoggerUtil(BrowserFactory.class);
    private static Playwright playwright;

    /**
     * Initializes and returns a Browser instance based on the specified browser type
     *
     * @param browserType Type of browser (chrome, firefox, edge, safari)
     * @return Configured Browser instance
     */
    public static Browser initializeBrowser(String browserType) {
        LOGGER.info("Initializing browser: " + browserType);

        if (playwright == null) {
            playwright = Playwright.create();
        }

        Browser browser;
        Configuration config = Configuration.getInstance();

        switch (browserType.toLowerCase()) {
            case "chrome":
                browser = launchChrome(config);
                break;
            case "firefox":
                browser = launchFirefox(config);
                break;
            case "edge":
                browser = launchEdge(config);
                break;
            case "safari":
                browser = launchSafari(config);
                break;
            case "chromium":
            default:
                browser = launchChromium(config);
        }

        LOGGER.info("Browser initialized successfully");
        return browser;
    }

    private static Browser launchChrome(Configuration config) {
        Map<String, Object> args = new HashMap<>();
        args.put("headless", config.isHeadless());

        return playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setChannel("chrome")
                .setHeadless(config.isHeadless())
                .setSlowMo(config.getSlowMotion())
                .setTimeout(config.getTimeout())
                .setArgs(config.getBrowserArgs())
                .setDevtools(config.isDevTools()));
    }

    private static Browser launchFirefox(Configuration config) {
        return playwright.firefox().launch(new BrowserType.LaunchOptions()
                .setHeadless(config.isHeadless())
                .setSlowMo(config.getSlowMotion())
                .setTimeout(config.getTimeout()));
    }

    private static Browser launchEdge(Configuration config) {
        return playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setChannel("msedge")
                .setHeadless(config.isHeadless())
                .setSlowMo(config.getSlowMotion())
                .setTimeout(config.getTimeout()));
    }

    private static Browser launchSafari(Configuration config) {
        return playwright.webkit().launch(new BrowserType.LaunchOptions()
                .setHeadless(config.isHeadless())
                .setSlowMo(config.getSlowMotion())
                .setTimeout(config.getTimeout()));
    }

    private static Browser launchChromium(Configuration config) {
        return playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(config.isHeadless())
                .setSlowMo(config.getSlowMotion())
                .setTimeout(config.getTimeout()));
    }

    /**
     * Creates a new browser context with tracing enabled if specified in configuration
     *
     * @param browser Browser instance
     * @return BrowserContext with configured options
     */
    public static BrowserContext createBrowserContext(Browser browser) {
        Configuration config = Configuration.getInstance();

        BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                .setViewportSize(config.getViewportWidth(), config.getViewportHeight())
                .setRecordVideoDir(config.isRecordVideo() ? Paths.get("videos/") : null));

        if (config.isTracing()) {
            context.tracing().start(new Tracing.StartOptions()
                    .setScreenshots(true)
                    .setSnapshots(true));
        }

        return context;
    }

    /**
     * Closes the Playwright instance and all associated browsers
     */
    public static void closePlaywright() {
        if (playwright != null) {
            LOGGER.info("Closing Playwright instance");
            playwright.close();
            playwright = null;
        }
    }
}