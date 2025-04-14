# Allure Reporting in Playwright-Cucumber Framework

This document explains how to use Allure reporting in the Playwright-Cucumber framework.

## Overview

Allure is a flexible, lightweight multi-language test reporting tool that shows a detailed representation of test execution results. It provides:

- Rich and interactive HTML reports
- Test categorization
- History trends
- Integration with CI/CD systems
- Attachments (screenshots, logs, etc.)
- Step-by-step test execution details

## Viewing Allure Reports

To view Allure reports, you need to have the Allure command-line tool installed:

1. Run your tests to generate Allure results
2. Double-click on `viewAllureResults.bat` in your project directory
3. This will start the Allure server and open the report in your browser

## Installing Allure Command-line Tool

To use Allure reporting, you need to install the Allure command-line tool:

1. Download from: https://github.com/allure-framework/allure2/releases
2. Extract to a directory on your computer
3. Add the bin directory to your PATH environment variable
4. Run: `allure --version` to verify installation

## Allure Features in the Framework

### Environment Information

The following environment information is captured:
- OS name and version
- Java version

### Test Results

Allure captures detailed information about test execution:
- Test status (passed, failed, skipped)
- Test duration
- Failure details

## Troubleshooting

If you encounter issues with Allure reporting:

1. Check that Allure results are being generated in `build/allure-results`
2. Verify that the Allure Cucumber plugin is included in the test runners
3. Make sure the Allure command-line tool is properly installed and in your PATH

For more information, refer to the [Allure Framework documentation](https://docs.qameta.io/allure/)
