package com.serenityindie.jiraplugin.processor;

import com.serenityindie.jiraplugin.model.JiraComment;
import com.serenityindie.jiraplugin.model.TestResult;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.serenityindie.jiraplugin.util.StringUtil.escapeSpecialCharacters;

public class JiraCommentGenerator {
    private final List<TestResult> testResults;
    private final String jiraUrl;
    private final String username;
    private final String environment;
    private JiraComment jiraComment;
    private static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static DateTimeFormatter hourAndMinuteFormatter = DateTimeFormatter.ofPattern("H:m");
    public static final String COMMENT_IDENTIFIER = "***SerenityIndieJiraPlugin Comment***";

    public JiraCommentGenerator(List<TestResult> testResults, String username, String environment, String jiraUrl) {
        this.jiraComment = new JiraComment();
        this.jiraUrl = jiraUrl;
        this.testResults = testResults;
        this.username = username;
        this.environment = environment;
    }

    private void generateComment(String issueKey) {
        this.jiraComment.getCommentBuilder().setLength(0);
        this.jiraComment.append(COMMENT_IDENTIFIER)
                .append("\\n\\nThis comment has been automatically generated by [~")
                .append(escapeSpecialCharacters(this.username))
                .append("] following the `mvn verify` execution in the *")
                .append(escapeSpecialCharacters(this.environment))
                .append("* environment on ")
                .append(OffsetDateTime.now().format(dateFormatter))
                .append(" at ")
                .append(OffsetDateTime.now().format(hourAndMinuteFormatter))
                .append(".\\n\\nTest Execution Results:\\n");

        TestResult testResultMatchingIssueKey = this.testResults.stream().filter(
                        testResult ->
                                testResult.getIssueKey().trim().equalsIgnoreCase(issueKey))
                .findFirst()
                .orElse(null);

        if (testResultMatchingIssueKey != null) {
            if (null != testResultMatchingIssueKey.getAdditionalIssues() && !testResultMatchingIssueKey.getAdditionalIssues().isEmpty()) {
                this.appendResultsForSubissues(
                        testResultMatchingIssueKey.getAdditionalIssues(),
                        this.testResults);

            } else {
                this.appendResultsForIssueItself(issueKey, testResults);
            }

        }
//        this.jiraComment.append("\\nWhere:\\n- *Success*: The test passed.\\n- *Fail*: The test failed.\\n");
    }

    private void appendResultsForSubissues(List<String> subIssues, List<TestResult> testResults) {
        for (String subIssue : subIssues) {
            for (TestResult testResult : testResults) {
                if (testResult.getIssueKey().trim().equalsIgnoreCase(subIssue)) {
                    appendResults(testResult);
                    break;
                }
            }
        }
    }

    private void appendResultsForIssueItself(String issueId, List<TestResult> testResults) {
        for (TestResult testResult : testResults) {
            if (testResult.getIssueKey().trim().equalsIgnoreCase(issueId)) {
                appendResults(testResult);
                break;
            }
        }
    }

    private void appendResults(TestResult testResult) {
        this.jiraComment.append("- **")
                .append(escapeSpecialCharacters(testResult.getResult()))
                .append("** - [")
                .append(escapeSpecialCharacters(testResult.getIssueKey()))
                .append(String.format("%s%s%s%s%s", "|", this.jiraUrl, "/browse/", testResult.getIssueKey(), "]"))
                .append(" - ")
                .append(escapeSpecialCharacters(testResult.getScenarioTitle()))
                .append("\\n");
    }

    public String getHttpBodyJsonWithJiraCommentForIssueId(String issueKey) {
        this.generateComment(issueKey);
        return new StringBuilder("{")
                .append("\"body\" : \"")
                .append(this.jiraComment.get())
                .append("\"")
                .append("}")
                .toString();

    }
}
