package com.framework.utils;

import com.framework.config.Configuration;
import com.microsoft.playwright.Page;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utility class for capturing screenshots
 */
public class ScreenshotUtil {
    private static final LoggerUtil LOGGER = new LoggerUtil(ScreenshotUtil.class);

    /**
     * Captures a screenshot of the current page
     *
     * @param page Playwright Page instance
     * @param screenshotName Base name for the screenshot file
     * @return Path to the saved screenshot
     */
    public static Path captureScreenshot(Page page, String screenshotName) {
        try {
            // Create screenshots directory if it doesn't exist
            Configuration config = Configuration.getInstance();
            String screenshotsDir = config.getScreenshotsDir();
            File directory = new File(screenshotsDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Create unique filename with timestamp
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String filename = screenshotName + "_" + timestamp + ".png";
            Path screenshotPath = Paths.get(screenshotsDir, filename);

            LOGGER.info("Capturing screenshot: " + screenshotPath);

            // Take screenshot with Playwright
            page.screenshot(new Page.ScreenshotOptions()
                    .setPath(screenshotPath)
                    .setFullPage(true));

            return screenshotPath;
        } catch (Exception e) {
            LOGGER.error("Failed to capture screenshot", e);
            return null;
        }
    }

    /**
     * Captures a screenshot with the current thread id in the filename
     * to support parallel execution
     *
     * @param page Playwright Page instance
     * @param scenarioName Name of the scenario
     * @param screenshotType Type of screenshot (e.g., "failure", "step", etc.)
     * @return Path to the saved screenshot
     */
    public static Path captureThreadSafeScreenshot(Page page, String scenarioName, String screenshotType) {
        long threadId = Thread.currentThread().getId();
        String screenshotName = scenarioName + "_" + threadId + "_" + screenshotType;
        return captureScreenshot(page, screenshotName);
    }

    /**
     * Gets the last captured screenshot path for attachments
     *
     * @param screenshotsDir Screenshots directory
     * @param threadId Thread ID
     * @return Path to the most recent screenshot for the thread
     */
    public static String getLatestScreenshotPath(String screenshotsDir, long threadId) {
        File directory = new File(screenshotsDir);
        if (!directory.exists() || !directory.isDirectory()) {
            return null;
        }

        File[] files = directory.listFiles((dir, name) ->
                name.contains("_" + threadId + "_") && name.endsWith(".png"));

        if (files == null || files.length == 0) {
            return null;
        }

        // Find the most recent file
        File latestFile = files[0];
        for (int i = 1; i < files.length; i++) {
            if (files[i].lastModified() > latestFile.lastModified()) {
                latestFile = files[i];
            }
        }

        return latestFile.getAbsolutePath();
    }
}