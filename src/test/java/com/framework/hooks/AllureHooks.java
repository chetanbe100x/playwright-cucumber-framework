package com.framework.hooks;

import io.cucumber.java.After;
import io.cucumber.java.AfterStep;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.qameta.allure.Allure;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Hooks for Allure reporting
 */
public class AllureHooks {
    private static boolean environmentAdded = false;

    /**
     * Setup before each scenario
     *
     * @param scenario Current Cucumber scenario
     */
    @Before(order = 1)
    public void setupAllure(Scenario scenario) {
        System.out.println("Setting up Allure for scenario: " + scenario.getName());

        // Add environment info only once
        if (!environmentAdded) {
            addEnvironmentInfo();
            environmentAdded = true;
        }
    }

    /**
     * After each step, check if it failed and capture screenshot
     *
     * @param scenario Current Cucumber scenario
     */
    @AfterStep
    public void afterStep(Scenario scenario) {
        if (scenario.isFailed()) {
            captureAndAttachScreenshot(scenario);
        }
    }

    /**
     * After each scenario, capture screenshot if failed
     *
     * @param scenario Current Cucumber scenario
     */
    @After(order = 1)
    public void afterScenario(Scenario scenario) {
        if (scenario.isFailed()) {
            captureAndAttachScreenshot(scenario);
        }
    }

    /**
     * Captures screenshot and attaches it to Allure report
     *
     * @param scenario Current Cucumber scenario
     */
    private void captureAndAttachScreenshot(Scenario scenario) {
        try {
            // Create a simple text file as a placeholder for screenshot
            String message = "Screenshot placeholder for: " + scenario.getName();
            byte[] messageBytes = message.getBytes();

            // Attach to Allure report
            try (InputStream is = new ByteArrayInputStream(messageBytes)) {
                Allure.addAttachment(
                    "Screenshot on Failure",
                    "text/plain",
                    is,
                    "txt"
                );
            }

            // Also attach to Cucumber report
            scenario.attach(messageBytes, "text/plain", "Screenshot on Failure");

            System.out.println("Screenshot placeholder attached to reports");
        } catch (IOException e) {
            System.err.println("Failed to attach screenshot placeholder: " + e.getMessage());
        }
    }

    /**
     * Adds environment information to Allure report
     */
    private void addEnvironmentInfo() {
        try {
            System.out.println("Adding environment information to Allure report");

            Properties props = new Properties();

            // System properties
            props.setProperty("OS", System.getProperty("os.name"));
            props.setProperty("OS Version", System.getProperty("os.version"));
            props.setProperty("Java Version", System.getProperty("java.version"));

            // Create directory if it doesn't exist
            File allureResultsDir = new File("build/allure-results");
            if (!allureResultsDir.exists()) {
                allureResultsDir.mkdirs();
            }

            // Save environment properties to a file
            File envPropsFile = new File(allureResultsDir, "environment.properties");
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(envPropsFile)) {
                props.store(fos, "Allure Environment Information");
            }

            System.out.println("Environment information added to Allure report");
        } catch (Exception e) {
            System.err.println("Failed to add environment information to Allure report: " + e.getMessage());
        }
    }
}
