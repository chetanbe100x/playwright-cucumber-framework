package com.framework.components;

import com.framework.config.Configuration;
import com.framework.core.WebActions;
import com.framework.utils.JsonReader;
import com.framework.utils.LoggerUtil;

import java.util.Map;

/**
 * Sample component for application checks
 */
public class ApplicationCheck {
    private static final LoggerUtil LOGGER = new LoggerUtil(ApplicationCheck.class);
    private static final String COMPONENT_NAME = "ApplicationCheck";

    private final WebActions webActions;
    private final Map<String, String> testData;

    /**
     * Initializes the component with web actions and test data
     *
     * @param webActions WebActions instance
     * @param testData Test data for the component
     */
    public ApplicationCheck(WebActions webActions, Map<String, String> testData) {
        this.webActions = webActions;
        this.testData = testData;
    }

    /**
     * Logs in to the application
     *
     * @return ApplicationCheck instance for method chaining
     */
    public ApplicationCheck login() {
        LOGGER.info("Logging in to the application");

        String username = testData.get("username");
        String password = testData.get("password");

        // Get base URL from config.properties
        String baseUrl = Configuration.getInstance().getBaseUrl();
        LOGGER.info("Using base URL from config.properties: " + baseUrl);

        // Navigate to login page
        webActions.navigateTo(baseUrl);

        // Get locators from JSON
        String usernameField = JsonReader.getLocator(COMPONENT_NAME, "usernameField");
        String passwordField = JsonReader.getLocator(COMPONENT_NAME, "passwordField");
        String loginButton = JsonReader.getLocator(COMPONENT_NAME, "loginButton");

        // Perform login
        webActions.type(usernameField, username);
        webActions.type(passwordField, password);
        webActions.click(loginButton);

        // Wait for dashboard to load
        String dashboardElement = JsonReader.getLocator(COMPONENT_NAME, "dashboardElement");
        webActions.waitForElementVisible(dashboardElement);

        LOGGER.info("Successfully logged in to the application");
        return this;
    }

    /**
     * Navigates to a specific section in the application
     *
     * @param sectionName Name of the section to navigate to
     * @return ApplicationCheck instance for method chaining
     */
    public ApplicationCheck navigateToSection(String sectionName) {
        LOGGER.info("Navigating to section: " + sectionName);

        // Get locator from JSON (using dynamic locator name)
        String sectionLocator = JsonReader.getLocator(COMPONENT_NAME, sectionName + "Link");

        // Click on the section link
        webActions.click(sectionLocator);

        // Wait for section to load
        String sectionLoadElement = JsonReader.getLocator(COMPONENT_NAME, sectionName + "LoadIndicator");
        webActions.waitForElementVisible(sectionLoadElement);

        LOGGER.info("Successfully navigated to section: " + sectionName);
        return this;
    }

    /**
     * Submits a form in the application
     *
     * @return ApplicationCheck instance for method chaining
     */
    public ApplicationCheck submitForm() {
        LOGGER.info("Submitting form");

        // Get locators from JSON
        String formField1 = JsonReader.getLocator(COMPONENT_NAME, "formField1");
        String formField2 = JsonReader.getLocator(COMPONENT_NAME, "formField2");
        String formDropdown = JsonReader.getLocator(COMPONENT_NAME, "formDropdown");
        String submitButton = JsonReader.getLocator(COMPONENT_NAME, "submitButton");

        // Fill form fields with test data
        webActions.type(formField1, testData.get("field1"));
        webActions.type(formField2, testData.get("field2"));
        webActions.selectByText(formDropdown, testData.get("dropdownValue"));

        // Submit form
        webActions.click(submitButton);

        // Wait for confirmation
        String confirmationElement = JsonReader.getLocator(COMPONENT_NAME, "confirmationMessage");
        webActions.waitForElementVisible(confirmationElement);

        LOGGER.info("Form submitted successfully");
        return this;
    }

    /**
     * Verifies data in the application
     *
     * @return true if verification passes, false otherwise
     */
    public boolean verifyData() {
        LOGGER.info("Verifying data");

        // Get locators from JSON
        String dataTable = JsonReader.getLocator(COMPONENT_NAME, "dataTable");
        String dataRow = JsonReader.getLocator(COMPONENT_NAME, "dataRow");
        String dataCell = JsonReader.getLocator(COMPONENT_NAME, "dataCell");

        // Wait for data to load
        webActions.waitForElementVisible(dataTable);

        // Verify expected data is present
        String expectedData = testData.get("expectedData");
        String actualData = webActions.getText(dataCell);

        boolean result = actualData.contains(expectedData);

        if (result) {
            LOGGER.info("Data verification passed");
        } else {
            LOGGER.error("Data verification failed. Expected: " + expectedData + ", Actual: " + actualData);
        }

        return result;
    }

    /**
     * Logs out from the application
     */
    public void logout() {
        LOGGER.info("Logging out from the application");

        // Get locator from JSON
        String userMenuButton = JsonReader.getLocator(COMPONENT_NAME, "userMenuButton");
        String logoutOption = JsonReader.getLocator(COMPONENT_NAME, "logoutOption");

        // Perform logout
        webActions.click(userMenuButton);
        webActions.click(logoutOption);

        // Wait for login page
        String loginPage = JsonReader.getLocator(COMPONENT_NAME, "loginPage");
        webActions.waitForElementVisible(loginPage);

        LOGGER.info("Successfully logged out from the application");
    }
}