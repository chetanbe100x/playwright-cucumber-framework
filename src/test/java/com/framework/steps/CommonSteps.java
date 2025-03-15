package com.framework.steps;

import com.framework.components.ApplicationCheck;
import com.framework.core.DriverManager;
import com.framework.core.WebActions;
import com.framework.utils.ExcelReader;
import com.framework.utils.LoggerUtil;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Common step definitions for Cucumber scenarios
 */
public class CommonSteps {
    private static final LoggerUtil LOGGER = new LoggerUtil(CommonSteps.class);
    private WebActions webActions;
    private final Map<String, ExcelReader> excelReaders = new HashMap<>();
    private final Map<String, Object> componentInstances = new HashMap<>();
    private Map<String, String> testData;
    private Scenario scenario;

    /**
     * Setup before each scenario
     *
     * @param scenario Current Cucumber scenario
     */
    @Before
    public void setUp(Scenario scenario) {
        this.scenario = scenario;
        LOGGER.info("Setting up scenario: " + scenario.getName());
    }

    /**
     * Teardown after each scenario
     */
    @After
    public void tearDown() {
        LOGGER.info("Tearing down scenario: " + scenario.getName());

        // Close browser
        DriverManager.closeBrowser();

        // Close all Excel readers
        excelReaders.values().forEach(ExcelReader::close);
        excelReaders.clear();

        // Clear component instances
        componentInstances.clear();
    }

    /**
     * Initializes the browser
     *
     * @param browserType Type of browser to launch
     */
    @Given("user launch {string} browser")
    public void userLaunchBrowser(String browserType) {
        LOGGER.info("Launching browser: " + browserType);
        DriverManager.initializeBrowser(browserType);
        webActions = new WebActions();
    }

    /**
     * Loads test data from an Excel file
     *
     * @param fileLocation Location of the Excel file
     */
    @And("user loads test data file from location {string}")
    public void userLoadsTestDataFile(String fileLocation) {
        LOGGER.info("Loading test data from: " + fileLocation);

        // Create a new Excel reader if not already created for this file
        if (!excelReaders.containsKey(fileLocation)) {
            excelReaders.put(fileLocation, new ExcelReader(fileLocation));
        }

        // Test data is already loaded at this point, but will be selected based on component and tag later
        LOGGER.info("Test data file loaded successfully");
    }

    /**
     * Calls a method from a specified component
     *
     * @param methodName Name of the method to call
     * @param componentPath Fully qualified path to the component class
     * @throws Exception if method invocation fails
     */
    @And("user calls method {string} from component {string}")
    public void userCallsMethodFromComponent(String methodName, String componentPath) throws Exception {
        LOGGER.info("Calling method: " + methodName + " from component: " + componentPath);

        // Extract component name from the fully qualified path
        String componentName = componentPath.substring(componentPath.lastIndexOf('.') + 1);

        // Load test data for this specific component based on active tags
        loadTestDataForComponent(componentName);

        // Get or create component instance
        Object componentInstance = getOrCreateComponentInstance(componentPath);

        // Find the method
        Method method = componentInstance.getClass().getMethod(methodName);

        // Invoke the method
        Object result = method.invoke(componentInstance);

        // Handle boolean results (for verification methods)
        if (result instanceof Boolean) {
            boolean success = (Boolean) result;
            if (!success) {
                throw new AssertionError("Verification failed in method: " + methodName);
            }
        }

        LOGGER.info("Method executed successfully: " + methodName);
    }

    /**
     * Loads test data for a specific component based on active tags
     *
     * @param componentName Name of the component
     */
    private void loadTestDataForComponent(String componentName) {
        // Get the Excel reader
        String fileLocation = getFirstExcelReaderKey();
        if (fileLocation == null) {
            LOGGER.error("No Excel reader initialized. Make sure test data file is loaded first.");
            throw new RuntimeException("No Excel reader initialized");
        }
        ExcelReader excelReader = excelReaders.get(fileLocation);

        // Load the component sheet
        excelReader.loadTestData(componentName, "TestCase");

        // Find the first matching tag from the scenario
        String tagKey = null;
        for (String tag : scenario.getSourceTagNames()) {
            // Remove @ from tag name
            String cleanTag = tag.startsWith("@") ? tag.substring(1) : tag;

            // Check if this tag exists as a key in the sheet
            try {
                Map<String, String> tagData = excelReader.getRowData(componentName, cleanTag);
                if (tagData != null) {
                    tagKey = cleanTag;
                    break;
                }
            } catch (Exception e) {
                // Tag not found in this sheet, continue to next tag
            }
        }

        if (tagKey == null) {
            LOGGER.error("No matching tag found in Excel sheet for component: " + componentName);
            throw new RuntimeException("No matching tag found in Excel sheet: " + componentName);
        }

        // Get test data for the matching tag
        testData = excelReader.getRowData(componentName, tagKey);
        LOGGER.info("Test data loaded for component [" + componentName + "] and tag [" + tagKey + "]: " + testData);
    }

    /**
     * Gets the first Excel reader key
     *
     * @return First Excel reader key or null if none exists
     */
    private String getFirstExcelReaderKey() {
        if (excelReaders.isEmpty()) {
            return null;
        }
        return excelReaders.keySet().iterator().next();
    }

    /**
     * Gets or creates a component instance
     *
     * @param componentPath Fully qualified path to the component class
     * @return Component instance
     * @throws Exception if component instantiation fails
     */
    private Object getOrCreateComponentInstance(String componentPath) throws Exception {
        if (componentInstances.containsKey(componentPath)) {
            return componentInstances.get(componentPath);
        }

        // Load the component class
        Class<?> componentClass = Class.forName(componentPath);

        // Create instance using constructor with WebActions and testData
        Object componentInstance = componentClass.getConstructor(WebActions.class, Map.class)
                .newInstance(webActions, testData);

        // Store the instance for reuse
        componentInstances.put(componentPath, componentInstance);

        return componentInstance;
    }

    /**
     * Gets the current WebActions instance
     *
     * @return WebActions instance
     */
    public WebActions getWebActions() {
        return webActions;
    }

    /**
     * Gets the current test data
     *
     * @return Test data map
     */
    public Map<String, String> getTestData() {
        return testData;
    }
}