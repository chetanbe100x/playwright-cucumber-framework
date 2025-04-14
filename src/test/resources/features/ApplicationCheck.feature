Feature: Application Check


  Scenario: Verify user can login and access admin section
    Given user launch "chrome" browser
    And user loads test data file from location "src/test/resources/testdata/TestData.xlsx"
    And user calls method "login" from component "com.framework.components.ApplicationCheck"
