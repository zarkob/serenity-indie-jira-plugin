package com.serenityindie.jiraplugin.model;

public class JiraComment {
    private StringBuilder commentBuilder;

    public JiraComment() {
        this.commentBuilder = new StringBuilder();
    }

    public StringBuilder getCommentBuilder() {
        return commentBuilder;
    }

    private void setCommentBuilder(StringBuilder commentBuilder) {
        this.commentBuilder = commentBuilder;
    }

    public JiraComment append(String str) {
        this.commentBuilder.append(str);
        return this;
    }

    public JiraComment appendLine(String str) {
        this.commentBuilder.append(str).append("\n");
        return this;
    }

    public String get() {
        return this.commentBuilder.toString();
    }
}
