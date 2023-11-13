package com.serenityindie.jiraplugin.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Zarko Bucic
 * Reduced test result extracted from TestReport in order to be used for the Jira comment
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestResult {
    String result;
    String issueKey;
    String scenarioTitle;
    List<String> additionalIssues;
    String jiraCommentId;
}
