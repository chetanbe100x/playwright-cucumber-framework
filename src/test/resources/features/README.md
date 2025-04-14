# Sauce Demo Test Cases

This directory contains feature files for testing the Sauce Demo website (https://www.saucedemo.com/).

## Feature Files

### ApplicationCheck.feature
Basic login functionality tests.

### SauceDemoShopping.feature
Post-login shopping functionality tests:

1. **Add Product to Cart and Checkout**
   - Adds a single product to the cart
   - Verifies the product is in the cart
   - Completes the checkout process
   - Verifies order completion

2. **Sort Products and Add Multiple Items**
   - Sorts products by price (low to high)
   - Adds multiple products to the cart
   - Verifies all products are in the cart

3. **Complete Purchase and Return to Products**
   - Adds a product to the cart
   - Completes the checkout process
   - Returns to the products page
   - Verifies products are still displayed

## Test Data

Test data is stored in the Excel file:
- `src/test/resources/testdata/TestData.xlsx` - Contains both login credentials and shopping test data

## Tags

- `@Smoke` - Critical path tests
- `@Regression` - More comprehensive tests
- `@Shopping` - Tests related to shopping functionality

## Running Tests

To run all tests:
```
gradlew test
```

To run only shopping tests:
```
gradlew test -Dcucumber.filter.tags="@Shopping"
```

To run smoke tests:
```
gradlew test -Dcucumber.filter.tags="@Smoke"
```
