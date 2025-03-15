package com.framework.core;

import com.framework.config.Configuration;
import com.framework.utils.LoggerUtil;
import com.framework.utils.ScreenshotUtil;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.*;
import com.microsoft.playwright.options.LoadState;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Provides generic methods to interact with web elements using Playwright
 * with automatic frame handling capabilities
 */
public class WebActions {
    private static final LoggerUtil LOGGER = new LoggerUtil(WebActions.class);
    private final Configuration config = Configuration.getInstance();
    private static final int MAX_FRAME_DEPTH = 5; // Maximum depth for searching frames

    /**
     * Gets the current page instance from DriverManager
     *
     * @return Current page instance
     */
    public Page getPage() {
        return DriverManager.getPage();
    }

    /**
     * Navigates to the specified URL
     *
     * @param url URL to navigate to
     */
    public void navigateTo(String url) {
        try {
            LOGGER.info("Navigating to URL: " + url);
            getPage().navigate(url);
            waitForPageLoad();
        } catch (Exception e) {
            LOGGER.error("Failed to navigate to URL: " + url, e);
            captureScreenshot("navigation_error");
            throw e;
        }
    }

    /**
     * Waits for page to load completely
     */
    public void waitForPageLoad() {
        getPage().waitForLoadState(LoadState.NETWORKIDLE);
    }

    /**
     * Result class for element search across frames
     */
    private static class ElementSearchResult {
        final ElementHandle element;
        final Frame frame;

        ElementSearchResult(ElementHandle element, Frame frame) {
            this.element = element;
            this.frame = frame;
        }
    }

    /**
     * Recursively finds an element in any frame of the page
     *
     * @param locator Element locator
     * @return ElementSearchResult containing the element and its frame, or null if not found
     */
    private ElementSearchResult findElementAcrossFrames(String locator) {
        LOGGER.debug("Searching for element across frames: " + locator);

        // First try in the main frame
        Page page = getPage();
        try {
            ElementHandle element = page.waitForSelector(locator,
                    new Page.WaitForSelectorOptions()
                            .setTimeout(config.getElementTimeout() / 3)); // Use shorter timeout for quick check

            if (element != null) {
                LOGGER.debug("Element found in main frame: " + locator);
                return new ElementSearchResult(element, page.mainFrame());
            }
        } catch (Exception e) {
            // Element not found in main frame, will search in frames
            LOGGER.debug("Element not found in main frame, searching in frames: " + locator);
        }

        // Search in frames recursively
        AtomicReference<ElementSearchResult> result = new AtomicReference<>();
        searchFramesRecursively(page.mainFrame(), locator, 0, result);

        if (result.get() != null) {
            LOGGER.debug("Element found in frame: " + locator);
            return result.get();
        }

        LOGGER.error("Element not found in any frame: " + locator);
        captureScreenshot("element_not_found");
        throw new PlaywrightException("Element not found in any frame: " + locator);
    }

    /**
     * Recursively searches through frames for an element
     *
     * @param frame Current frame to search
     * @param locator Element locator
     * @param depth Current recursion depth
     * @param result AtomicReference to store the result
     */
    private void searchFramesRecursively(Frame frame, String locator, int depth, AtomicReference<ElementSearchResult> result) {
        if (depth > MAX_FRAME_DEPTH || result.get() != null) {
            return; // Stop recursion if max depth reached or element found
        }

        try {
            // Try to find the element in the current frame
            ElementHandle element = frame.waitForSelector(locator,
                    new Frame.WaitForSelectorOptions()
                            .setTimeout(config.getElementTimeout() / 3)
                            .setState(WaitForSelectorState.ATTACHED));

            if (element != null) {
                result.set(new ElementSearchResult(element, frame));
                return;
            }
        } catch (Exception e) {
            // Element not found in current frame, continue with child frames
        }

        // Get all child frames and search in them
        List<Frame> childFrames = frame.childFrames();
        for (Frame childFrame : childFrames) {
            searchFramesRecursively(childFrame, locator, depth + 1, result);
            if (result.get() != null) {
                return; // Element found in one of the child frames
            }
        }
    }

    /**
     * Performs an action on an element found across any frame
     *
     * @param locator Element locator
     * @param action Action to perform on the element
     * @param actionName Name of the action for logging
     */
    private void performActionAcrossFrames(String locator, Consumer<ElementHandle> action, String actionName) {
        LOGGER.info(actionName + " on element: " + locator);
        try {
            ElementSearchResult result = findElementAcrossFrames(locator);
            action.accept(result.element);
        } catch (Exception e) {
            LOGGER.error("Failed to " + actionName + " element: " + locator, e);
            captureScreenshot(actionName.toLowerCase().replace(" ", "_") + "_error");
            throw e;
        }
    }

    /**
     * Clicks on an element with the given locator, automatically finding it in any frame
     *
     * @param locator Element locator
     */
    public void click(String locator) {
        performActionAcrossFrames(locator,
                element -> element.click(new ElementHandle.ClickOptions().setTimeout(config.getElementTimeout())),
                "Clicking");
    }

    /**
     * Types text into an input field, automatically finding it in any frame
     *
     * @param locator Element locator
     * @param text Text to type
     */
    public void type(String locator, String text) {
        performActionAcrossFrames(locator,
                element -> {
                    element.click();
                    element.fill(text);
                },
                "Typing text into");
    }

    /**
     * Clears an input field, automatically finding it in any frame
     *
     * @param locator Element locator
     */
    public void clear(String locator) {
        performActionAcrossFrames(locator,
                element -> element.fill(""),
                "Clearing");
    }

    /**
     * Selects an option from dropdown by value, automatically finding it in any frame
     *
     * @param locator Dropdown element locator
     * @param value Value to select
     */
    public void selectByValue(String locator, String value) {
        performActionAcrossFrames(locator,
                element -> element.selectOption(new SelectOption().setValue(value)),
                "Selecting value from dropdown");
    }

    /**
     * Selects an option from dropdown by visible text, automatically finding it in any frame
     *
     * @param locator Dropdown element locator
     * @param text Text to select
     */
    public void selectByText(String locator, String text) {
        performActionAcrossFrames(locator,
                element -> element.selectOption(new SelectOption().setLabel(text)),
                "Selecting text from dropdown");
    }

    /**
     * Selects an option from dropdown by index, automatically finding it in any frame
     *
     * @param locator Dropdown element locator
     * @param index Index to select
     */
    public void selectByIndex(String locator, int index) {
        performActionAcrossFrames(locator,
                element -> element.selectOption(new SelectOption().setIndex(index)),
                "Selecting index from dropdown");
    }

    /**
     * Gets text from an element, automatically finding it in any frame
     *
     * @param locator Element locator
     * @return Element text
     */
    public String getText(String locator) {
        AtomicReference<String> text = new AtomicReference<>("");
        performActionAcrossFrames(locator,
                element -> text.set(element.textContent()),
                "Getting text from");
        return text.get();
    }

    /**
     * Gets the value of an attribute from an element, automatically finding it in any frame
     *
     * @param locator Element locator
     * @param attributeName Name of the attribute
     * @return Attribute value
     */
    public String getAttribute(String locator, String attributeName) {
        AtomicReference<String> attribute = new AtomicReference<>("");
        performActionAcrossFrames(locator,
                element -> attribute.set(element.getAttribute(attributeName)),
                "Getting attribute " + attributeName + " from");
        return attribute.get();
    }

    /**
     * Checks if an element is visible, automatically finding it in any frame
     *
     * @param locator Element locator
     * @return true if element is visible, false otherwise
     */
    public boolean isVisible(String locator) {
        try {
            LOGGER.info("Checking if element is visible: " + locator);
            ElementSearchResult result = findElementAcrossFrames(locator);
            return result.element.isVisible();
        } catch (Exception e) {
            LOGGER.debug("Element not visible: " + locator);
            return false;
        }
    }

    /**
     * Checks if an element exists, automatically searching in any frame
     *
     * @param locator Element locator
     * @return true if element exists, false otherwise
     */
    public boolean isExisting(String locator) {
        try {
            LOGGER.info("Checking if element exists: " + locator);
            ElementSearchResult result = findElementAcrossFrames(locator);
            return result.element != null;
        } catch (Exception e) {
            LOGGER.debug("Element does not exist: " + locator);
            return false;
        }
    }

    /**
     * Waits for an element to be visible, automatically finding it in any frame
     *
     * @param locator Element locator
     */
    public void waitForElementVisible(String locator) {
        LOGGER.info("Waiting for element to be visible: " + locator);
        try {
            ElementSearchResult result = findElementAcrossFrames(locator);
            result.element.waitForElementState(ElementState.VISIBLE,
                    new ElementHandle.WaitForElementStateOptions()
                            .setTimeout(config.getElementTimeout()));
        } catch (Exception e) {
            LOGGER.error("Element did not become visible: " + locator, e);
            captureScreenshot("wait_visibility_error");
            throw e;
        }
    }

    /**
     * Waits for an element to be invisible, automatically finding it in any frame
     *
     * @param locator Element locator
     */
    public void waitForElementInvisible(String locator) {
        LOGGER.info("Waiting for element to be invisible: " + locator);
        try {
            Page page = getPage();
            page.waitForSelector(locator,
                    new Page.WaitForSelectorOptions()
                            .setState(WaitForSelectorState.HIDDEN)
                            .setTimeout(config.getElementTimeout()));
        } catch (Exception e) {
            LOGGER.error("Element did not become invisible: " + locator, e);
            captureScreenshot("wait_invisibility_error");
            throw e;
        }
    }

    /**
     * Uploads a file to an input element, automatically finding it in any frame
     *
     * @param locator File input element locator
     * @param filePath Path to the file to upload
     */
    public void uploadFile(String locator, String filePath) {
        performActionAcrossFrames(locator,
                element -> element.setInputFiles(Paths.get(filePath)),
                "Uploading file to");
    }

    /**
     * Hovers over an element, automatically finding it in any frame
     *
     * @param locator Element locator
     */
    public void hover(String locator) {
        performActionAcrossFrames(locator,
                element -> element.hover(),
                "Hovering over");
    }

    /**
     * Double-clicks on an element, automatically finding it in any frame
     *
     * @param locator Element locator
     */
    public void doubleClick(String locator) {
        performActionAcrossFrames(locator,
                element -> element.dblclick(),
                "Double-clicking on");
    }

    /**
     * Right-clicks on an element, automatically finding it in any frame
     *
     * @param locator Element locator
     */
    public void rightClick(String locator) {
        performActionAcrossFrames(locator,
                element -> element.click(new ElementHandle.ClickOptions().setButton(MouseButton.RIGHT)),
                "Right-clicking on");
    }

    /**
     * Checks a checkbox, automatically finding it in any frame
     *
     * @param locator Checkbox element locator
     * @param check true to check, false to uncheck
     */
    public void setCheckbox(String locator, boolean check) {
        performActionAcrossFrames(locator,
                element -> {
                    if (Boolean.parseBoolean(element.getAttribute("checked")) != check) {
                        element.click();
                    }
                },
                check ? "Checking" : "Unchecking");
    }

    /**
     * Drags and drops an element, automatically finding elements in any frame
     *
     * @param sourceLocator Source element locator
     * @param targetLocator Target element locator
     */
    public void dragAndDrop(String sourceLocator, String targetLocator) {
        LOGGER.info("Performing drag and drop from " + sourceLocator + " to " + targetLocator);
        try {
            ElementSearchResult sourceResult = findElementAcrossFrames(sourceLocator);
            ElementSearchResult targetResult = findElementAcrossFrames(targetLocator);

            if (sourceResult.frame.equals(targetResult.frame)) {
                // Source and target are in the same frame
                BoundingBox sourceBox = sourceResult.element.boundingBox();
                BoundingBox targetBox = targetResult.element.boundingBox();

                if (sourceBox != null && targetBox != null) {
                    // Perform drag and drop using mouse actions
                    sourceResult.element.scrollIntoViewIfNeeded();
                    targetResult.element.scrollIntoViewIfNeeded();

                    // Calculate center points
                    double sourceX = sourceBox.x + sourceBox.width / 2;
                    double sourceY = sourceBox.y + sourceBox.height / 2;
                    double targetX = targetBox.x + targetBox.width / 2;
                    double targetY = targetBox.y + targetBox.height / 2;

                    // Perform drag and drop
                    Page page = getPage();
                    page.mouse().move(sourceX, sourceY);
                    page.mouse().down();
                    page.mouse().move(targetX, targetY);
                    page.mouse().up();
                } else {
                    LOGGER.error("Unable to get bounding box for elements");
                    throw new PlaywrightException("Unable to get bounding box for elements");
                }
            } else {
                // Source and target are in different frames
                LOGGER.error("Drag and drop across different frames is not supported");
                throw new UnsupportedOperationException("Drag and drop across different frames is not supported");
            }
        } catch (Exception e) {
            LOGGER.error("Failed to perform drag and drop", e);
            captureScreenshot("drag_drop_error");
            throw e;
        }
    }

    /**
     * Handles JavaScript alerts
     *
     * @param accept true to accept, false to dismiss
     * @param promptText Text to enter in prompt (null if not a prompt)
     */
    public void handleAlert(boolean accept, String promptText) {
        LOGGER.info(accept ? "Accepting alert" : "Dismissing alert");
        Page page = getPage();

        if (promptText != null) {
            page.onDialog(dialog -> {
                dialog.accept(promptText);
            });
        } else {
            page.onDialog(dialog -> {
                if (accept) {
                    dialog.accept();
                } else {
                    dialog.dismiss();
                }
            });
        }
    }

    /**
     * Captures a screenshot and saves it with the given name
     *
     * @param screenshotName Name for the screenshot file
     * @return Path to the saved screenshot
     */
    public Path captureScreenshot(String screenshotName) {
        return ScreenshotUtil.captureScreenshot(getPage(), screenshotName);
    }

    /**
     * Gets all cookies from the current page
     *
     * @return List of cookies
     */
    public List<Cookie> getCookies() {
        return DriverManager.getContext().cookies();
    }

    /**
     * Sets cookies for the current context
     *
     * @param cookies List of cookies to set
     */
    public void setCookies(List<Cookie> cookies) {
        DriverManager.getContext().addCookies(cookies);
    }

    /**
     * Executes JavaScript in the browser
     *
     * @param script JavaScript code to execute
     * @param args Arguments to pass to the script
     * @return Result of the script execution
     */
    public Object executeScript(String script, Object... args) {
        return getPage().evaluate(script, args);
    }

    /**
     * Executes JavaScript in a specific frame
     *
     * @param script JavaScript code to execute
     * @param args Arguments to pass to the script
     * @return Result of the script execution
     */
    public Object executeScriptInFrame(String frameSelector, String script, Object... args) {
        try {
            LOGGER.info("Executing script in frame: " + frameSelector);

            // Create a JavaScript function that finds the frame and executes the script in its context
            String wrappedScript = "() => {\n" +
                    "  const frame = document.querySelector('" + frameSelector + "');\n" +
                    "  if (!frame || !frame.contentWindow) {\n" +
                    "    throw new Error('Frame not found or not accessible');\n" +
                    "  }\n" +
                    "  return frame.contentWindow.eval(`" + script.replace("`", "\\`") + "`);\n" +
                    "}";

            // Execute the wrapper script in the main page context
            return getPage().evaluate(wrappedScript);
        } catch (Exception e) {
            LOGGER.error("Failed to execute script in frame: " + frameSelector, e);
            captureScreenshot("frame_script_error");
            throw e;
        }
    }

    /**
     * Gets all element handles matching a selector across all frames
     *
     * @param selector Element selector
     * @return List of ElementSearchResult containing elements and their frames
     */
    public List<ElementSearchResult> getAllElements(String selector) {
        LOGGER.info("Getting all elements matching: " + selector);
        List<ElementSearchResult> elements = new ArrayList<>();

        // Check main frame
        try {
            List<ElementHandle> mainFrameElements = getPage().querySelectorAll(selector);
            for (ElementHandle element : mainFrameElements) {
                elements.add(new ElementSearchResult(element, getPage().mainFrame()));
            }
        } catch (Exception e) {
            // No elements in main frame
        }

        // Check all frames
        collectElementsFromFrames(getPage().mainFrame(), selector, elements, 0);

        return elements;
    }

    /**
     * Recursively collects elements from frames
     *
     * @param frame Current frame
     * @param selector Element selector
     * @param elements List to collect elements
     * @param depth Current recursion depth
     */
    private void collectElementsFromFrames(Frame frame, String selector, List<ElementSearchResult> elements, int depth) {
        if (depth > MAX_FRAME_DEPTH) {
            return;
        }

        try {
            List<ElementHandle> frameElements = frame.querySelectorAll(selector);
            for (ElementHandle element : frameElements) {
                elements.add(new ElementSearchResult(element, frame));
            }
        } catch (Exception e) {
            // No elements in this frame
        }

        List<Frame> childFrames = frame.childFrames();
        for (Frame childFrame : childFrames) {
            collectElementsFromFrames(childFrame, selector, elements, depth + 1);
        }
    }

    /**
     * Gets the count of elements matching a selector across all frames
     *
     * @param selector Element selector
     * @return Count of matching elements
     */
    public int getElementCount(String selector) {
        return getAllElements(selector).size();
    }

    /**
     * Scrolls to an element, automatically finding it in any frame
     *
     * @param locator Element locator
     */
    public void scrollToElement(String locator) {
        performActionAcrossFrames(locator,
                element -> element.scrollIntoViewIfNeeded(),
                "Scrolling to");
    }
}