Feature: Sauce Demo Shopping

  Background: User is logged in
    Given user launch "chrome" browser
    And user loads test data file from location "src/test/resources/testdata/TestData.xlsx"
    And user calls method "login" from component "com.framework.components.ApplicationCheck"

  @Smoke @Shopping
  Scenario: User can add a product to cart and checkout
    Given user calls method "addProductToCart" with parameter "Sauce Labs Backpack" from component "com.framework.components.SauceDemoShop"
    When user calls method "openCart" from component "com.framework.components.SauceDemoShop"
    Then user calls method "verifyProductInCart" with parameter "Sauce Labs Backpack" from component "com.framework.components.SauceDemoShop"
    And user calls method "proceedToCheckout" from component "com.framework.components.SauceDemoShop"
    And user calls method "fillCheckoutInfo" from component "com.framework.components.SauceDemoShop"
    And user calls method "completeOrder" from component "com.framework.components.SauceDemoShop"
    Then user calls method "verifyOrderComplete" from component "com.framework.components.SauceDemoShop"


  Scenario: User can sort products and add multiple items to cart
    Given user calls method "sortProductsBy" with parameter "Price (low to high)" from component "com.framework.components.SauceDemoShop"
    And user calls method "addProductToCart" with parameter "Sauce Labs Onesie" from component "com.framework.components.SauceDemoShop"
    And user calls method "addProductToCart" with parameter "Sauce Labs Bike Light" from component "com.framework.components.SauceDemoShop"
    When user calls method "openCart" from component "com.framework.components.SauceDemoShop"
    Then user calls method "verifyProductInCart" with parameter "Sauce Labs Onesie" from component "com.framework.components.SauceDemoShop"
    And user calls method "verifyProductInCart" with parameter "Sauce Labs Bike Light" from component "com.framework.components.SauceDemoShop"


  Scenario: User can complete purchase and return to products
    Given user calls method "addProductToCart" with parameter "Sauce Labs Fleece Jacket" from component "com.framework.components.SauceDemoShop"
    When user calls method "openCart" from component "com.framework.components.SauceDemoShop"
    And user calls method "proceedToCheckout" from component "com.framework.components.SauceDemoShop"
    And user calls method "fillCheckoutInfo" from component "com.framework.components.SauceDemoShop"
    And user calls method "completeOrder" from component "com.framework.components.SauceDemoShop"
    Then user calls method "verifyOrderComplete" from component "com.framework.components.SauceDemoShop"
    And user calls method "backToProducts" from component "com.framework.components.SauceDemoShop"
    Then user calls method "verifyProductExists" with parameter "Sauce Labs Backpack" from component "com.framework.components.SauceDemoShop"
