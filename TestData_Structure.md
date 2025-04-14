# Test Data Structure for Playwright-Cucumber Framework

## Overview

This document describes the test data structure used in the Playwright-Cucumber framework. The framework uses Excel files to store test data, which is loaded dynamically based on the test case being executed.

## Excel File Structure

The test data is stored in Excel files located in the `src/test/resources/testdata/` directory. The main test data file is `TestData.xlsx`.

### Sheet: ApplicationCheck

This sheet contains login credentials and other application-specific data:

| TestCase    | username       | password       |
|-------------|----------------|----------------|
| Smoke       | standard_user  | secret_sauce   |
| Regression  | standard_user  | secret_sauce   |

### How Data is Loaded

1. The framework identifies which test is running based on the Cucumber tags (@Smoke, @Regression, etc.)
2. It then loads the corresponding row from the Excel sheet based on the tag name
3. The data is made available to the test steps through the `testData` map

## Adding New Test Data

### For New Test Cases

To add data for a new test case:

1. Add a new row to the appropriate sheet
2. Set the TestCase column value to match your Cucumber tag (without the @ symbol)
3. Add the required data in the other columns

Example for adding a new test case for invalid login:

| TestCase     | username        | password       | expected_error                                       |
|--------------|-----------------|----------------|------------------------------------------------------|
| Smoke        | standard_user   | secret_sauce   |                                                      |
| Regression   | standard_user   | secret_sauce   |                                                      |
| InvalidLogin | locked_out_user | secret_sauce   | Epic sadface: Sorry, this user has been locked out.  |

### For New Features

To add data for a new feature:

1. Create a new sheet in the Excel file named after your component/feature
2. Add columns for TestCase and any data fields needed by your tests
3. Add rows for each test case type (Smoke, Regression, etc.)

## Accessing Test Data in Code

In your component classes, you can access the test data as follows:

```java
String username = testData.get("username");
String password = testData.get("password");
```

## Best Practices

1. Keep the TestCase column as the first column in each sheet
2. Use consistent naming for test cases across different sheets
3. Add comments or documentation for complex data fields
4. Consider adding validation data (expected results) in the test data sheet

## SauceDemoShop Example

If you want to add shopping functionality tests, you could create a new sheet like this:

| TestCase    | firstName | lastName | postalCode | productName            |
|-------------|-----------|----------|------------|------------------------|
| Smoke       | John      | Doe      | 12345      | Sauce Labs Backpack    |
| Regression  | Jane      | Smith    | 54321      | Sauce Labs Fleece Jacket |
