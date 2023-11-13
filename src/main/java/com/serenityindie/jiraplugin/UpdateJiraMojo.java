package com.serenityindie.jiraplugin;

import com.serenityindie.jiraplugin.client.JiraRestClient;
import com.serenityindie.jiraplugin.converter.ConvertTestReports;
import com.serenityindie.jiraplugin.model.TestReport;
import com.serenityindie.jiraplugin.model.TestResult;
import com.serenityindie.jiraplugin.processor.JiraCommentGenerator;
import com.serenityindie.jiraplugin.processor.SerenityReportProcessor;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.util.List;

import static com.serenityindie.jiraplugin.processor.TestResultsProcessor.extractAndAddParentTestResults;

/**
 * The serenity-indie Maven Mojo that updates Jira issue comments with the results of the serenity tests.
 */
@Mojo(name = "update-jira", defaultPhase = LifecyclePhase.VERIFY)
public class UpdateJiraMojo extends AbstractMojo {
    @Parameter(property = "project")
    private MavenProject project;
    @Parameter(property = "serenity-folder-path")
    private String serenityFolderPath;
    @Parameter(property = "jira-server-instance-url")
    private String jiraServerInstanceUrl;
    @Parameter(property = "jira-project-key")
    private String jiraProjectKey;
    @Parameter(property = "plugin-execution-environment")
    private String pluginExecutionEnvironment;
    @Parameter(property = "jira-username")
    private String jiraUsername;
    @Parameter(property = "jira-password")
    private String jiraPassword;
    JiraRestClient jiraRestClient;

    public void execute() throws MojoExecutionException, MojoFailureException {
        jiraRestClient = new JiraRestClient();
        getLog().info("Hello from serenity-jira-indie-plugin !!!");
        getLog().info("serenity-folder-path: " + serenityFolderPath);
        getLog().info("plugin-execution-environment: " + pluginExecutionEnvironment);
        getLog().info("jira-server-instance-url: " + jiraServerInstanceUrl);
        getLog().info("jira-project-key: " + jiraProjectKey);
        getLog().info("jira-username: " + jiraUsername);
        getLog().info("jira-password: " + jiraPassword);

        SerenityReportProcessor serenityReportProcessor = new SerenityReportProcessor();
        List<TestReport> testReports = serenityReportProcessor.processSerenityReports(serenityFolderPath);
        List<TestResult> testResults = ConvertTestReports.toTestResults(testReports);
        List<TestResult> testResultsWithParents = extractAndAddParentTestResults(testResults);

        getLog().info("TEST RESULTS: ");
        for (TestResult testResult : testResultsWithParents) {
            getLog().info(testResult.toString());
        }

        findExistingComments(testResultsWithParents);
        postJiraComments(testResultsWithParents);

    }

    private void findExistingComments(List<TestResult> testResults) {
        for (TestResult testResult : testResults) {
            String issueId = testResult.getIssueKey();
            String existingCommentId = jiraRestClient.findExistingCommentId(jiraServerInstanceUrl, issueId, jiraUsername, jiraPassword);
            testResult.setJiraCommentId(existingCommentId);
        }
    }

    private void postJiraComments(List<TestResult> testResults) {
        JiraCommentGenerator jiraCommentGenerator = new JiraCommentGenerator(
                testResults,
                this.jiraUsername,
                this.pluginExecutionEnvironment,
                jiraServerInstanceUrl);

        for (TestResult testResult : testResults) {
            String commentForIssue = jiraCommentGenerator.getHttpBodyJsonWithJiraCommentForIssueId(testResult.getIssueKey());
            getLog().info("jiraIssueKey: " + testResult.getIssueKey());
            getLog().info("jiraCommentBody: " + commentForIssue);
            jiraRestClient.postCommentToIssue(
                    this.jiraServerInstanceUrl,
                    testResult.getIssueKey(),
                    this.jiraUsername,
                    this.jiraPassword,
                    commentForIssue,
                    testResult.getJiraCommentId());
        }
    }
}
