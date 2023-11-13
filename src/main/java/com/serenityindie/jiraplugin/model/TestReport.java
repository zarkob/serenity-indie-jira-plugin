package com.serenityindie.jiraplugin.model;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

@Data
public class TestReport {
    private String name;
    private String id;
    private String scenarioId;
    private String methodName;
    private List<TestStep> testSteps;
    private UserStory userStory;
    private String title;
    private String description;
    private String backgroundTitle;
    private String backgroundDescription;
    private List<String> additionalIssues;
    private List<Tag> tags;
    private OffsetDateTime startTime;
    private int duration;
    private String projectKey;
    private boolean isManualTestingUpToDate;
    private boolean manual;
    private String testSource;
    private int order;
    private String result;
    private List<String> issues;

    @Data
    public static class TestStep {
        private int number;
        private String description;
        private int duration;
        private OffsetDateTime startTime;
        private String result;
        private boolean precondition;
        private int level;
    }

    @Data
    public static class UserStory {
        private String id;
        private String storyName;
        private String displayName;
        private String path;
        private List<PathElement> pathElements;
        private String narrative;
        private String type;
    }

    @Data
    public static class PathElement {
        private String name;
        private String description;
    }

    @Data
    public static class Tag {
        private String name;
        private String type;
        private String displayName;
    }
}
