package net.conjur.api.integration;

import cucumber.api.junit.Cucumber;
import junit.framework.TestCase;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@Cucumber.Options(format = {"html:target/cucumber-html-report", "json:target/cucumber-json-report.json"})
public class RunCukesTest {
}
