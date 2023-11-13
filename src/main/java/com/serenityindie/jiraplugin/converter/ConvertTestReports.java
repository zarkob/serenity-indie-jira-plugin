package com.serenityindie.jiraplugin.converter;

import com.serenityindie.jiraplugin.model.TestReport;
import com.serenityindie.jiraplugin.model.TestResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConvertTestReports {
    public static List<TestResult> toTestResults(List<TestReport> testReports) {
        Map<String, TestResult> resultsMap = new HashMap<>();

        for (TestReport report : testReports) {
            // Skip if issues list is null or empty
            if (report.getIssues() == null || report.getIssues().isEmpty()) {
                continue;
            }

            // Extract the first issue as the issueKey
            String issueKey = report.getIssues().get(0);

            // Check if the issueKey is already in the map
            TestResult result = resultsMap.get(issueKey);
            if (result == null) {
                // Create a new TestResult if it doesn't exist
                result = new TestResult();
                result.setIssueKey(issueKey);
                result.setResult(report.getResult());
                result.setScenarioTitle(report.getTitle());
                result.setAdditionalIssues(new ArrayList<>(report.getIssues().subList(1, report.getIssues().size())));
                resultsMap.put(issueKey, result);
            } else {
                // Update the existing TestResult with additional issues if needed
                List<String> additionalIssues = result.getAdditionalIssues();
                additionalIssues.addAll(report.getIssues().subList(1, report.getIssues().size()));
                result.setAdditionalIssues(additionalIssues);
            }
        }

        // Return a list of unique TestResult objects
        return new ArrayList<>(resultsMap.values());
    }
}
