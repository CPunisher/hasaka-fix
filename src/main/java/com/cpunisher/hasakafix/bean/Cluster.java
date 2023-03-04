package com.cpunisher.hasakafix.bean;

import java.util.List;

public class Cluster<T> {
    private String beforeTemplate;
    private String afterTemplate;
    private List<T> edits;

    public Cluster(String beforeTemplate, String afterTemplate, List<T> edits) {
        this.beforeTemplate = beforeTemplate;
        this.afterTemplate = afterTemplate;
        this.edits = edits;
    }

    public String getBeforeTemplate() {
        return beforeTemplate;
    }

    public void setBeforeTemplate(String beforeTemplate) {
        this.beforeTemplate = beforeTemplate;
    }

    public String getAfterTemplate() {
        return afterTemplate;
    }

    public void setAfterTemplate(String afterTemplate) {
        this.afterTemplate = afterTemplate;
    }

    public List<T> getEdits() {
        return edits;
    }

    public void setEdits(List<T> edits) {
        this.edits = edits;
    }
}
