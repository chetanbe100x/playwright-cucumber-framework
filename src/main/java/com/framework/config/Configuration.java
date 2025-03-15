package com.framework.config;

import com.framework.utils.LoggerUtil;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Singleton class to manage framework configuration
 */
public class Configuration {
    private static final LoggerUtil LOGGER = new LoggerUtil(Configuration.class);
    private static volatile Configuration instance;
    private final Properties properties = new Properties();

    private Configuration() {
        loadProperties();
    }

    /**
     * Gets the singleton instance of Configuration
     *
     * @return Configuration instance
     */
    public static Configuration getInstance() {
        if (instance == null) {
            synchronized (Configuration.class) {
                if (instance == null) {
                    instance = new Configuration();
                }
            }
        }
        return instance;
    }

    /**
     * Loads properties from config.properties file
     */
    private void loadProperties() {
        try {
            String configPath = System.getProperty("config.file", "src/main/resources/config.properties");
            LOGGER.info("Loading configuration from: " + configPath);
            properties.load(new FileInputStream(configPath));
        } catch (IOException e) {
            LOGGER.error("Failed to load configuration properties", e);
            throw new RuntimeException("Failed to load configuration properties", e);
        }
    }

    /**
     * Gets a property value
     *
     * @param key Property key
     * @param defaultValue Default value if property is not found
     * @return Property value
     */
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * Gets a property value as an integer
     *
     * @param key Property key
     * @param defaultValue Default value if property is not found
     * @return Property value as an integer
     */
    public int getIntProperty(String key, int defaultValue) {
        String value = properties.getProperty(key);
        return (value != null) ? Integer.parseInt(value) : defaultValue;
    }

    /**
     * Gets a property value as a boolean
     *
     * @param key Property key
     * @param defaultValue Default value if property is not found
     * @return Property value as a boolean
     */
    public boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        return (value != null) ? Boolean.parseBoolean(value) : defaultValue;
    }

    /**
     * Gets the base URL for the application
     *
     * @return Base URL
     */
    public String getBaseUrl() {
        return getProperty("base.url", "http://localhost");
    }

    /**
     * Checks if browser should run in headless mode
     *
     * @return true if headless, false otherwise
     */
    public boolean isHeadless() {
        return getBooleanProperty("browser.headless", true);
    }

    /**
     * Gets the slow motion delay for Playwright actions
     *
     * @return Slow motion delay in milliseconds
     */
    public int getSlowMotion() {
        return getIntProperty("browser.slowmo", 0);
    }

    /**
     * Gets the timeout for browser operations
     *
     * @return Timeout in milliseconds
     */
    public int getTimeout() {
        return getIntProperty("browser.timeout", 30000);
    }

    /**
     * Gets the timeout for element operations
     *
     * @return Element timeout in milliseconds
     */
    public int getElementTimeout() {
        return getIntProperty("element.timeout", 10000);
    }

    /**
     * Gets the viewport width
     *
     * @return Viewport width in pixels
     */
    public int getViewportWidth() {
        return getIntProperty("viewport.width", 1280);
    }

    /**
     * Gets the viewport height
     *
     * @return Viewport height in pixels
     */
    public int getViewportHeight() {
        return getIntProperty("viewport.height", 720);
    }

    /**
     * Checks if tracing is enabled
     *
     * @return true if tracing is enabled, false otherwise
     */
    public boolean isTracing() {
        return getBooleanProperty("tracing.enabled", false);
    }

    /**
     * Checks if video recording is enabled
     *
     * @return true if video recording is enabled, false otherwise
     */
    public boolean isRecordVideo() {
        return getBooleanProperty("video.recording", false);
    }

    /**
     * Checks if DevTools should be enabled
     *
     * @return true if DevTools should be enabled, false otherwise
     */
    public boolean isDevTools() {
        return getBooleanProperty("browser.devtools", false);
    }

    /**
     * Gets the screenshots directory path
     *
     * @return Screenshots directory path
     */
    public String getScreenshotsDir() {
        return getProperty("screenshots.dir", "screenshots");
    }

    /**
     * Gets the test data directory path
     *
     * @return Test data directory path
     */
    public String getTestDataDir() {
        return getProperty("testdata.dir", "src/test/resources/testdata");
    }

    /**
     * Gets the identifiers directory path
     *
     * @return Identifiers directory path
     */
    public String getIdentifiersDir() {
        return getProperty("identifiers.dir", "src/main/resources/identifiers");
    }

    /**
     * Gets custom browser arguments
     *
     * @return List of browser arguments
     */
    public List<String> getBrowserArgs() {
        String args = getProperty("browser.args", "");
        return args.isEmpty() ? List.of() : Arrays.asList(args.split(","));
    }
}