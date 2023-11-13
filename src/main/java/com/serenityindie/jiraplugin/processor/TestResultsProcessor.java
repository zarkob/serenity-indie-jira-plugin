package com.serenityindie.jiraplugin.processor;

import com.serenityindie.jiraplugin.model.TestResult;

import java.util.*;

public class TestResultsProcessor {

    /**
     * Creates a list of test results from the given list of test reports.
     * If record A contains the elements in additionalIssues, these are the references of the parent of the current issue A.
     * New record B should be created and added to the list of the testResults to be returned
     * IssueKey of the new record B should be the key previously found in additionalIssues list of the record A.
     * Aditionalissues of the new record B should be the list of all issueKeys of the records where B is found in additionalIssues
     * If parent record B is created then it should be removed from the list of the additionalIssues list of the record A.
     * For example, if the list of test reports is:
     * [
     * {issueKey: "DAS-2"...additionalIssues: ["DAS-1"]},
     * {issueKey: "DAS-3"...additionalIssues: ["DAS-1","DAS-1"]},
     * {issueKey: "DAS-12"...additionalIssues: ["DAS-10","DAS-10"]}
     * ]
     * then the new list of test results to be returned is:
     * [
     * {issueKey: "DAS-2"...additionalIssues: []},
     * {issueKey: "DAS-3"...additionalIssues: []},
     * {issueKey: "DAS-1"...additionalIssues: ["DAS-2","DAS-3"]},
     * {issueKey: "DAS-12"...additionalIssues: []},
     * {issueKey: "DAS-10"...additionalIssues: ["DAS-12"]}
     * ]
     *
     * @param testResultList
     * @return
     */
    public static List<TestResult> extractAndAddParentTestResults(List<TestResult> testResultList) {
        List<TestResult> newTestResults = new ArrayList<>();
        Map<String, Set<String>> parentToChildrenMap = new HashMap<>();
        Set<String> parentAditionalIssues = new HashSet<>();

        // First, create a map of parents to their children
        for (TestResult testResult : testResultList) {
            for (String parentIssue : testResult.getAdditionalIssues()) {
                parentToChildrenMap.computeIfAbsent(parentIssue, k -> new HashSet<>()).add(testResult.getIssueKey());
            }
        }

        // Now, iterate through the original test results and modify them
        for (TestResult testResult : testResultList) {
            parentAditionalIssues.addAll(new HashSet<>(testResult.getAdditionalIssues()));
            parentAditionalIssues.retainAll(parentToChildrenMap.keySet()); // Retain only those that are parents
            testResult.getAdditionalIssues().removeAll(parentAditionalIssues); // Remove parents from additionalIssues
            newTestResults.add(testResult);
        }

        // Add parent issues as new test results
        for (String parentIssue : parentAditionalIssues) {
            Set<String> childrenIssues = parentToChildrenMap.get(parentIssue);
            newTestResults.add(new TestResult(
                    "",
                    parentIssue,
                    "",
                    new ArrayList<>(childrenIssues),
                    ""
            ));
        }
        return newTestResults;
    }

    //    NOT USED BUT KEEP EVENTUALLY FOR SOME FUTURE SCENARIOS

    /**
     * Creates a list of test results from the given list of test reports.
     * New list of test results is reorganized in the manner so that each list contains only the results
     * of the primary issue and it's children.
     * For example, if the list of test reports is: [{issueKey: "DAS-1"...additionalIssues: ["DAS-1","DAS-2","DAS-3"]},
     * {issueKey: "DAS-2"...additionalIssues: ["DAS-2","DAS-3","DAS-1"]}]
     * then the new list of test results is:
     * [
     * {issueKey: "DAS-1"...additionalIssues: ["DAS-2", "DAS-3"]},
     * {issueKey: "DAS-2"...additionalIssues: ["DAS-3"]},
     * {issueKey: "DAS-3"...additionalIssues: []}
     * ]
     *
     * @param testResultList
     * @return
     */
    public static List<TestResult> reorganizeTestResults(List<TestResult> testResultList) {
        Set<String> processedIssues = new LinkedHashSet<>();
        List<TestResult> reorganizedResults = new ArrayList<>();

        for (TestResult testResult : testResultList) {
            if (!processedIssues.contains(testResult.getIssueKey())) {
                Set<String> additionalIssues = new LinkedHashSet<>(testResult.getAdditionalIssues());
                additionalIssues.remove(testResult.getIssueKey()); // Remove the primary issue key

                // Process and remove already processed issues from the additional issues
                additionalIssues.removeAll(processedIssues);
                processedIssues.add(testResult.getIssueKey());
                processedIssues.addAll(additionalIssues);
                reorganizedResults.add(new TestResult(
                        testResult.getResult(),
                        testResult.getIssueKey(),
                        testResult.getScenarioTitle(),
                        testResult.getAdditionalIssues(),
                        testResult.getJiraCommentId()
                ));
            }
        }
        return reorganizedResults;
    }

}
