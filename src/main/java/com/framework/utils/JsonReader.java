package com.framework.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.framework.config.Configuration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class to read web element identifiers from JSON files
 */
public class JsonReader {
    private static final LoggerUtil LOGGER = new LoggerUtil(JsonReader.class);
    private static final Map<String, JsonNode> IDENTIFIER_CACHE = new HashMap<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Gets a web element locator from a JSON file
     *
     * @param componentName Name of the component (JSON file name without extension)
     * @param locatorName Name of the locator within the JSON file
     * @return Locator value
     */
    public static String getLocator(String componentName, String locatorName) {
        JsonNode identifiers = getIdentifiers(componentName);

        if (!identifiers.has(locatorName)) {
            LOGGER.error("Locator not found: " + locatorName + " in " + componentName);
            throw new RuntimeException("Locator not found: " + locatorName + " in " + componentName);
        }

        JsonNode locatorNode = identifiers.get(locatorName);
        if (locatorNode.isObject()) {
            // Handle complex locator with type and value
            String locatorType = locatorNode.get("type").asText();
            String locatorValue = locatorNode.get("value").asText();
            return locatorType + "=" + locatorValue;
        } else {
            // Simple locator string
            return locatorNode.asText();
        }
    }

    /**
     * Gets all identifiers for a component from its JSON file
     *
     * @param componentName Name of the component (JSON file name without extension)
     * @return JSON node containing all identifiers
     */
    public static JsonNode getIdentifiers(String componentName) {
        // Check cache first
        if (IDENTIFIER_CACHE.containsKey(componentName)) {
            return IDENTIFIER_CACHE.get(componentName);
        }

        // Load from file
        try {
            Configuration config = Configuration.getInstance();
            String identifiersDir = config.getIdentifiersDir();
            String filePath = identifiersDir + File.separator + componentName + ".json";

            LOGGER.info("Loading identifiers from file: " + filePath);
            File file = new File(filePath);
            if (!file.exists()) {
                LOGGER.error("Identifiers file not found: " + filePath);
                throw new RuntimeException("Identifiers file not found: " + filePath);
            }

            JsonNode identifiers = objectMapper.readTree(file);
            IDENTIFIER_CACHE.put(componentName, identifiers);

            return identifiers;
        } catch (IOException e) {
            LOGGER.error("Failed to load identifiers from file: " + componentName, e);
            throw new RuntimeException("Failed to load identifiers from file: " + componentName, e);
        }
    }

    /**
     * Clears the identifier cache
     */
    public static void clearCache() {
        IDENTIFIER_CACHE.clear();
    }

    /**
     * Gets all locators for a component
     *
     * @param componentName Name of the component
     * @return Map of locator names to values
     */
    public static Map<String, String> getAllLocators(String componentName) {
        JsonNode identifiers = getIdentifiers(componentName);
        Map<String, String> locators = new HashMap<>();

        identifiers.fields().forEachRemaining(entry -> {
            String locatorName = entry.getKey();
            JsonNode locatorNode = entry.getValue();

            if (locatorNode.isObject()) {
                // Handle complex locator with type and value
                String locatorType = locatorNode.get("type").asText();
                String locatorValue = locatorNode.get("value").asText();
                locators.put(locatorName, locatorType + "=" + locatorValue);
            } else {
                // Simple locator string
                locators.put(locatorName, locatorNode.asText());
            }
        });

        return locators;
    }
}