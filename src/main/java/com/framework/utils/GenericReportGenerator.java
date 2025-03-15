package com.framework.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Fixed report generator that handles duplicate steps issue
 */
public class GenericReportGenerator {

    private static final String[] JSON_SEARCH_DIRS = {
            "build/cucumber-reports/",
            "reports/",
            "target/cucumber-reports/",
            "build/reports/"
    };

    private static final String OUTPUT_REPORT = "reports/SparkReport/ExtentSpark.html";

    public static void main(String[] args) {
        generateReport();
    }

    /**
     * Generates an Extent report by processing Cucumber JSON files
     * with special handling for duplicate steps
     */
    public static void generateReport() {
        try {
            System.out.println("Starting fixed report generation...");

            // Ensure reports directory exists
            new File("reports/SparkReport").mkdirs();

            // Initialize ExtentReports
            ExtentReports extent = new ExtentReports();

            // Create reporter
            ExtentSparkReporter spark = new ExtentSparkReporter(OUTPUT_REPORT);
            spark.config().setDocumentTitle("Test Execution Report");
            spark.config().setReportName("Cucumber Test Results");
            spark.config().setTheme(com.aventstack.extentreports.reporter.configuration.Theme.DARK);

            extent.attachReporter(spark);

            // Add system info
            extent.setSystemInfo("OS", System.getProperty("os.name"));
            extent.setSystemInfo("Java Version", System.getProperty("java.version"));
            extent.setSystemInfo("User", System.getProperty("user.name"));
            extent.setSystemInfo("Generated", new java.util.Date().toString());

            // Find all JSON files
            System.out.println("Searching for Cucumber JSON files...");
            java.util.List<Path> jsonFiles = findCucumberJsonFiles();
            System.out.println("Found " + jsonFiles.size() + " JSON files");

            if (jsonFiles.isEmpty()) {
                System.out.println("No JSON files found. Creating example report...");
                createExampleReport(extent);
            } else {
                processCucumberJsonFiles(jsonFiles, extent);
            }

            // Write the report
            extent.flush();

            System.out.println("Report generated successfully at: " + OUTPUT_REPORT);

        } catch (Exception e) {
            System.err.println("Error generating report: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Finds all Cucumber JSON files in the search directories
     */
    private static java.util.List<Path> findCucumberJsonFiles() throws IOException {
        java.util.List<Path> jsonFiles = new java.util.ArrayList<>();

        for (String searchDir : JSON_SEARCH_DIRS) {
            Path dirPath = Paths.get(searchDir);
            if (Files.exists(dirPath)) {
                try (Stream<Path> paths = Files.walk(dirPath, 3)) {
                    java.util.List<Path> files = paths
                            .filter(Files::isRegularFile)
                            .filter(path -> path.toString().endsWith(".json"))
                            .collect(Collectors.toList());

                    System.out.println("Found " + files.size() + " JSON files in " + searchDir);

                    // Check each file to see if it's a Cucumber JSON
                    for (Path file : files) {
                        try {
                            if (isCucumberJson(file)) {
                                System.out.println("  - Adding Cucumber JSON: " + file);
                                jsonFiles.add(file);
                            }
                        } catch (Exception e) {
                            System.out.println("  - Skipping non-Cucumber JSON: " + file);
                        }
                    }
                }
            } else {
                System.out.println("Directory not found: " + searchDir);
            }
        }

        return jsonFiles;
    }

    /**
     * Checks if a file is a Cucumber JSON format
     */
    private static boolean isCucumberJson(Path file) throws IOException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(file.toFile());

            // Cucumber JSON files are arrays with elements having certain properties
            return json.isArray() && json.size() > 0 &&
                    (json.get(0).has("elements") || json.get(0).has("name") && json.get(0).has("uri"));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Processes Cucumber JSON files to generate a report with special handling for duplicates
     */
    private static void processCucumberJsonFiles(java.util.List<Path> jsonFiles, ExtentReports extent) throws IOException {
        // Map to store feature tests by name to avoid duplicates
        Map<String, ExtentTest> featureMap = new HashMap<>();
        Map<String, Set<String>> scenarioStepMap = new HashMap<>();

        ObjectMapper mapper = new ObjectMapper();
        int totalFeatures = 0;
        int totalScenarios = 0;

        for (Path jsonFile : jsonFiles) {
            System.out.println("Processing file: " + jsonFile);

            try {
                JsonNode jsonReport = mapper.readTree(jsonFile.toFile());

                // Process each feature in the JSON
                for (JsonNode feature : jsonReport) {
                    String featureName = feature.path("name").asText("Unnamed Feature");

                    // Get or create feature test
                    ExtentTest featureTest;
                    if (featureMap.containsKey(featureName)) {
                        featureTest = featureMap.get(featureName);
                        System.out.println("  - Using existing feature: " + featureName);
                    } else {
                        featureTest = extent.createTest(featureName);
                        featureMap.put(featureName, featureTest);
                        System.out.println("  - Created new feature: " + featureName);
                        totalFeatures++;
                    }

                    // Process each element (scenario) in the feature
                    JsonNode elements = feature.path("elements");
                    if (elements.isArray()) {
                        for (JsonNode element : elements) {
                            // Skip background elements
                            String elementType = element.path("type").asText("");
                            if ("background".equals(elementType)) {
                                continue;
                            }

                            String scenarioName = element.path("name").asText("Unnamed Scenario");
                            String scenarioId = element.path("id").asText("");

                            // Get tags
                            String tagString = "";
                            JsonNode tags = element.path("tags");
                            if (tags.isArray()) {
                                StringBuilder sb = new StringBuilder();
                                for (JsonNode tag : tags) {
                                    String tagName = tag.path("name").asText("");
                                    if (!tagName.isEmpty()) {
                                        sb.append(tagName).append(" ");
                                    }
                                }
                                tagString = sb.toString().trim();
                            }

                            // Create a unique key for the scenario
                            String scenarioKey = featureName + "::" + scenarioName + "::" + tagString;

                            // Create a node for the scenario if it doesn't exist with these tags
                            ExtentTest scenarioTest;
                            if (!scenarioStepMap.containsKey(scenarioKey)) {
                                scenarioTest = featureTest.createNode(scenarioName + (!tagString.isEmpty() ? " " + tagString : ""));
                                System.out.println("    - Created new scenario: " + scenarioName + " with tags: " + tagString);

                                // Add tags as categories
                                if (tags.isArray()) {
                                    for (JsonNode tag : tags) {
                                        String tagName = tag.path("name").asText("").replace("@", "");
                                        if (!tagName.isEmpty()) {
                                            scenarioTest.assignCategory(tagName);
                                        }
                                    }
                                }

                                scenarioStepMap.put(scenarioKey, new HashSet<>());
                                totalScenarios++;

                                // Process steps only the first time we see this scenario
                                processSteps(element, scenarioTest, scenarioStepMap.get(scenarioKey));
                            } else {
                                System.out.println("    - Skipping duplicate scenario: " + scenarioName + " with tags: " + tagString);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Error processing JSON file: " + jsonFile);
                e.printStackTrace();
            }
        }

        System.out.println("Total features processed: " + totalFeatures);
        System.out.println("Total scenarios processed: " + totalScenarios);
    }

    /**
     * Process steps for a scenario with duplicate prevention
     */
    private static void processSteps(JsonNode element, ExtentTest scenarioTest, Set<String> processedSteps) {
        JsonNode steps = element.path("steps");
        if (steps.isArray()) {
            for (JsonNode step : steps) {
                String keyword = step.path("keyword").asText("");
                String stepName = step.path("name").asText("");
                String fullStepName = keyword + stepName;

                // Skip if we've already processed this step for this scenario
                if (processedSteps.contains(fullStepName)) {
                    continue;
                }

                // Mark step as processed
                processedSteps.add(fullStepName);

                // Get step status
                JsonNode result = step.path("result");
                String status = result.path("status").asText("").toLowerCase();

                // Add step to report
                switch (status) {
                    case "passed":
                        scenarioTest.log(Status.PASS, fullStepName);
                        break;
                    case "failed":
                        String errorMessage = result.path("error_message").asText("");
                        scenarioTest.log(Status.FAIL, fullStepName + "\n" + errorMessage);

                        // Check for embeddings (screenshots)
                        JsonNode embeddings = step.path("embeddings");
                        if (embeddings.isArray() && embeddings.size() > 0) {
                            for (JsonNode embedding : embeddings) {
                                String data = embedding.path("data").asText("");
                                String mimeType = embedding.path("mime_type").asText("");

                                if (!data.isEmpty() && mimeType.contains("image")) {
                                    scenarioTest.fail("Screenshot",
                                            com.aventstack.extentreports.MediaEntityBuilder
                                                    .createScreenCaptureFromBase64String(data)
                                                    .build());
                                }
                            }
                        }
                        break;
                    case "skipped":
                        scenarioTest.log(Status.SKIP, fullStepName);
                        break;
                    case "pending":
                        scenarioTest.log(Status.WARNING, fullStepName + " (PENDING)");
                        break;
                    default:
                        scenarioTest.log(Status.INFO, fullStepName);
                        break;
                }
            }
        }
    }

    /**
     * Creates an example report when no JSON files are found
     */
    private static void createExampleReport(ExtentReports extent) {
        // Create feature
        ExtentTest featureTest = extent.createTest("Application Check");

        // First scenario
        ExtentTest scenario1 = featureTest.createNode("Verify user can login and access admin section @Smoke");
        scenario1.assignCategory("Smoke");
        scenario1.log(Status.PASS, "Given user launch \"chrome\" browser");
        scenario1.log(Status.PASS, "And user loads test data file from location \"src/test/resources/testdata/TestData.xlsx\"");
        scenario1.log(Status.PASS, "And user calls method \"login\" from component \"com.framework.components.ApplicationCheck\"");

        // Second scenario
        ExtentTest scenario2 = featureTest.createNode("Verify user can login and access admin section @Regression");
        scenario2.assignCategory("Regression");
        scenario2.log(Status.PASS, "Given user launch \"chrome\" browser");
        scenario2.log(Status.PASS, "And user loads test data file from location \"src/test/resources/testdata/TestData.xlsx\"");
        scenario2.log(Status.PASS, "And user calls method \"login\" from component \"com.framework.components.ApplicationCheck\"");

        System.out.println("Created example report with 1 feature and 2 scenarios");
    }
}