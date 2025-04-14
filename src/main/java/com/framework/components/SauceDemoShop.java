package com.framework.components;

import com.framework.core.WebActions;
import com.framework.utils.JsonReader;
import com.framework.utils.LoggerUtil;

import java.util.Map;

/**
 * Component for Sauce Demo shopping functionality
 */
public class SauceDemoShop {
    private static final LoggerUtil LOGGER = new LoggerUtil(SauceDemoShop.class);
    private static final String COMPONENT_NAME = "SauceDemoShop";

    private final WebActions webActions;
    private final Map<String, String> testData;

    /**
     * Initializes the component with web actions and test data
     *
     * @param webActions WebActions instance
     * @param testData Test data for the component
     */
    public SauceDemoShop(WebActions webActions, Map<String, String> testData) {
        this.webActions = webActions;
        this.testData = testData;
    }

    /**
     * Sorts products by the specified option
     *
     * @param sortOption Sort option to select
     * @return SauceDemoShop instance for method chaining
     */
    public SauceDemoShop sortProductsBy(String sortOption) {
        LOGGER.info("Sorting products by: " + sortOption);

        String sortDropdown = JsonReader.getLocator(COMPONENT_NAME, "sortDropdown");
        webActions.click(sortDropdown);
        webActions.selectByText(sortDropdown, sortOption);

        LOGGER.info("Products sorted successfully");
        return this;
    }

    /**
     * Adds a specific product to the cart
     *
     * @param productName Name of the product to add
     * @return SauceDemoShop instance for method chaining
     */
    public SauceDemoShop addProductToCart(String productName) {
        LOGGER.info("Adding product to cart: " + productName);

        // Dynamic locator for the add to cart button of a specific product
        String addToCartButton = "//div[text()='" + productName + "']/ancestor::div[@class='inventory_item']//button[contains(@id, 'add-to-cart')]";
        webActions.click(addToCartButton);

        // Verify cart badge is updated
        String cartBadge = JsonReader.getLocator(COMPONENT_NAME, "cartBadge");
        webActions.waitForElementVisible(cartBadge);

        LOGGER.info("Product added to cart successfully");
        return this;
    }

    /**
     * Opens the shopping cart
     *
     * @return SauceDemoShop instance for method chaining
     */
    public SauceDemoShop openCart() {
        LOGGER.info("Opening shopping cart");

        String cartLink = JsonReader.getLocator(COMPONENT_NAME, "cartLink");
        webActions.click(cartLink);

        // Verify cart page is loaded
        String cartPageTitle = JsonReader.getLocator(COMPONENT_NAME, "cartPageTitle");
        webActions.waitForElementVisible(cartPageTitle);

        LOGGER.info("Shopping cart opened successfully");
        return this;
    }

    /**
     * Proceeds to checkout
     *
     * @return SauceDemoShop instance for method chaining
     */
    public SauceDemoShop proceedToCheckout() {
        LOGGER.info("Proceeding to checkout");

        String checkoutButton = JsonReader.getLocator(COMPONENT_NAME, "checkoutButton");
        webActions.click(checkoutButton);

        // Verify checkout page is loaded
        String checkoutPageTitle = JsonReader.getLocator(COMPONENT_NAME, "checkoutPageTitle");
        webActions.waitForElementVisible(checkoutPageTitle);

        LOGGER.info("Proceeded to checkout successfully");
        return this;
    }

    /**
     * Fills checkout information
     *
     * @return SauceDemoShop instance for method chaining
     */
    public SauceDemoShop fillCheckoutInfo() {
        LOGGER.info("Filling checkout information");

        String firstNameField = JsonReader.getLocator(COMPONENT_NAME, "firstNameField");
        String lastNameField = JsonReader.getLocator(COMPONENT_NAME, "lastNameField");
        String postalCodeField = JsonReader.getLocator(COMPONENT_NAME, "postalCodeField");
        String continueButton = JsonReader.getLocator(COMPONENT_NAME, "continueButton");

        webActions.type(firstNameField, testData.get("firstName"));
        webActions.type(lastNameField, testData.get("lastName"));
        webActions.type(postalCodeField, testData.get("postalCode"));
        webActions.click(continueButton);

        // Verify checkout overview page is loaded
        String checkoutOverviewTitle = JsonReader.getLocator(COMPONENT_NAME, "checkoutOverviewTitle");
        webActions.waitForElementVisible(checkoutOverviewTitle);

        LOGGER.info("Checkout information filled successfully");
        return this;
    }

    /**
     * Completes the order
     *
     * @return SauceDemoShop instance for method chaining
     */
    public SauceDemoShop completeOrder() {
        LOGGER.info("Completing order");

        String finishButton = JsonReader.getLocator(COMPONENT_NAME, "finishButton");
        webActions.click(finishButton);

        // Verify order completion page is loaded
        String orderCompleteTitle = JsonReader.getLocator(COMPONENT_NAME, "orderCompleteTitle");
        webActions.waitForElementVisible(orderCompleteTitle);

        LOGGER.info("Order completed successfully");
        return this;
    }

    /**
     * Verifies product exists in inventory
     *
     * @param productName Name of the product to verify
     * @return true if product exists, false otherwise
     */
    public boolean verifyProductExists(String productName) {
        LOGGER.info("Verifying product exists: " + productName);

        String productLocator = "//div[text()='" + productName + "']";
        boolean exists = webActions.isElementVisible(productLocator);

        if (exists) {
            LOGGER.info("Product exists: " + productName);
        } else {
            LOGGER.error("Product does not exist: " + productName);
        }

        return exists;
    }

    /**
     * Verifies product is in cart
     *
     * @param productName Name of the product to verify
     * @return true if product is in cart, false otherwise
     */
    public boolean verifyProductInCart(String productName) {
        LOGGER.info("Verifying product is in cart: " + productName);

        String productInCartLocator = "//div[@class='inventory_item_name' and text()='" + productName + "']";
        boolean inCart = webActions.isElementVisible(productInCartLocator);

        if (inCart) {
            LOGGER.info("Product is in cart: " + productName);
        } else {
            LOGGER.error("Product is not in cart: " + productName);
        }

        return inCart;
    }

    /**
     * Verifies order completion
     *
     * @return true if order is complete, false otherwise
     */
    public boolean verifyOrderComplete() {
        LOGGER.info("Verifying order completion");

        String thankYouMessage = JsonReader.getLocator(COMPONENT_NAME, "thankYouMessage");
        boolean isComplete = webActions.isElementVisible(thankYouMessage);

        if (isComplete) {
            LOGGER.info("Order completion verified");
        } else {
            LOGGER.error("Order completion verification failed");
        }

        return isComplete;
    }

    /**
     * Returns to products page
     *
     * @return SauceDemoShop instance for method chaining
     */
    public SauceDemoShop backToProducts() {
        LOGGER.info("Returning to products page");

        String backToProductsButton = JsonReader.getLocator(COMPONENT_NAME, "backToProductsButton");
        webActions.click(backToProductsButton);

        // Verify products page is loaded
        String productsTitle = JsonReader.getLocator(COMPONENT_NAME, "productsTitle");
        webActions.waitForElementVisible(productsTitle);

        LOGGER.info("Returned to products page successfully");
        return this;
    }
}
