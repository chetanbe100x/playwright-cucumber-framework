package com.framework.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

/**
 * Utility class for logging in the framework
 */
public class LoggerUtil {
    private final Logger logger;

    /**
     * Initializes the logger for the specified class
     *
     * @param clazz Class to create logger for
     */
    public LoggerUtil(Class<?> clazz) {
        logger = LogManager.getLogger(clazz);
    }

    /**
     * Sets thread context for parallel execution logging
     *
     * @param scenarioName Name of the scenario
     */
    public static void setThreadContext(String scenarioName) {
        // Convert any potentially problematic characters in the scenario name
        String safeName = scenarioName.replaceAll("[^a-zA-Z0-9_-]", "_");
        ThreadContext.put("scenarioName", safeName);
        ThreadContext.put("threadId", String.valueOf(Thread.currentThread().getId()));
    }

    /**
     * Clears thread context
     */
    public static void clearThreadContext() {
        ThreadContext.clearAll();
    }

    /**
     * Logs an info message
     *
     * @param message Message to log
     */
    public void info(String message) {
        logger.info(message);
    }

    /**
     * Logs a debug message
     *
     * @param message Message to log
     */
    public void debug(String message) {
        logger.debug(message);
    }

    /**
     * Logs a warning message
     *
     * @param message Message to log
     */
    public void warn(String message) {
        logger.warn(message);
    }

    /**
     * Logs an error message
     *
     * @param message Message to log
     */
    public void error(String message) {
        logger.error(message);
    }

    /**
     * Logs an error message with an exception
     *
     * @param message Message to log
     * @param e Exception to log
     */
    public void error(String message, Throwable e) {
        logger.error(message, e);
    }

    /**
     * Logs a fatal message
     *
     * @param message Message to log
     */
    public void fatal(String message) {
        logger.fatal(message);
    }

    /**
     * Logs a fatal message with an exception
     *
     * @param message Message to log
     * @param e Exception to log
     */
    public void fatal(String message, Throwable e) {
        logger.fatal(message, e);
    }

    /**
     * Logs the start of a test step
     *
     * @param stepName Name of the step
     */
    public void stepStart(String stepName) {
        logger.info("STEP START: " + stepName);
    }

    /**
     * Logs the end of a test step
     *
     * @param stepName Name of the step
     */
    public void stepEnd(String stepName) {
        logger.info("STEP END: " + stepName);
    }

    /**
     * Logs the start of a test
     *
     * @param testName Name of the test
     */
    public void testStart(String testName) {
        logger.info("TEST START: " + testName);
        logger.info("======================================================");
    }

    /**
     * Logs the end of a test
     *
     * @param testName Name of the test
     */
    public void testEnd(String testName) {
        logger.info("======================================================");
        logger.info("TEST END: " + testName);
    }
}