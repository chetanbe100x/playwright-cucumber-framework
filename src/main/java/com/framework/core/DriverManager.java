package com.framework.core;

import com.framework.utils.LoggerUtil;
import com.microsoft.playwright.*;

/**
 * Manages Playwright browser instances in a thread-safe manner to support parallel execution
 */
public class DriverManager {
    private static final LoggerUtil LOGGER = new LoggerUtil(DriverManager.class);
    private static final ThreadLocal<Browser> browserThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<BrowserContext> contextThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<Page> pageThreadLocal = new ThreadLocal<>();

    /**
     * Initializes a browser for the current thread
     *
     * @param browserType Type of browser to initialize
     */
    public static void initializeBrowser(String browserType) {
        LOGGER.info("Initializing browser for thread: " + Thread.currentThread().getId());
        Browser browser = BrowserFactory.initializeBrowser(browserType);
        browserThreadLocal.set(browser);

        BrowserContext context = BrowserFactory.createBrowserContext(browser);
        contextThreadLocal.set(context);

        Page page = context.newPage();
        pageThreadLocal.set(page);
        LOGGER.info("Browser initialization complete for thread: " + Thread.currentThread().getId());
    }

    /**
     * Gets the browser instance associated with the current thread
     *
     * @return Browser instance
     */
    public static Browser getBrowser() {
        return browserThreadLocal.get();
    }

    /**
     * Gets the browser context associated with the current thread
     *
     * @return BrowserContext instance
     */
    public static BrowserContext getContext() {
        return contextThreadLocal.get();
    }

    /**
     * Gets the page instance associated with the current thread
     *
     * @return Page instance
     */
    public static Page getPage() {
        return pageThreadLocal.get();
    }

    /**
     * Creates a new page in the current browser context
     *
     * @return Newly created Page instance
     */
    public static Page newPage() {
        Page page = contextThreadLocal.get().newPage();
        pageThreadLocal.set(page);
        return page;
    }

    /**
     * Closes and cleans up browser resources for the current thread
     */
    public static void closeBrowser() {
        LOGGER.info("Closing browser for thread: " + Thread.currentThread().getId());

        if (pageThreadLocal.get() != null) {
            pageThreadLocal.get().close();
            pageThreadLocal.remove();
        }

        if (contextThreadLocal.get() != null) {
            contextThreadLocal.get().close();
            contextThreadLocal.remove();
        }

        if (browserThreadLocal.get() != null) {
            browserThreadLocal.get().close();
            browserThreadLocal.remove();
        }

        LOGGER.info("Browser closed for thread: " + Thread.currentThread().getId());
    }
}