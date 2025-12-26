package at.hillstrom.energy.specs

import org.junit.platform.suite.api.ConfigurationParameter
import org.junit.platform.suite.api.IncludeEngines
import org.junit.platform.suite.api.SelectClasspathResource
import org.junit.platform.suite.api.Suite

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("at/hillstrom/energy/specs")
@ConfigurationParameter(key = "cucumber.glue", value = "at.hillstrom.energy.specs")
@ConfigurationParameter(key = "cucumber.plugin", value = "pretty, html:build/reports/cucumber/report.html")
class RunCucumberTest
