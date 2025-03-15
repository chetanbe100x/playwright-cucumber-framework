package com.framework.runners;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

/**
 * Test runner for Regression tests
 */
@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features",
        glue = {"com.framework.steps"},
        plugin = {
                "pretty",
                "html:target/cucumber-reports/cucumber-pretty-regression.html",
                "json:target/cucumber-reports/Test1Runner.json",
                "rerun:target/cucumber-reports/rerun-regression.txt",
                "com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter:"
        },
        monochrome = true,
        tags = "@Regression",
        dryRun = false
)
public class Test1Runner {
    // This class is intentionally empty. It's just a holder for annotations.
}