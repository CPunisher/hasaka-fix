package com.cpunisher.hasakafix.bean;

import java.util.List;

public class Cluster<T> {
    private T pattern;
    private List<T> edits;

    public Cluster(T pattern, List<T> edits) {
        this.pattern = pattern;
        this.edits = edits;
    }

    public T getPattern() {
        return pattern;
    }

    public void setPattern(T pattern) {
        this.pattern = pattern;
    }

    public List<T> getEdits() {
        return edits;
    }

    public void setEdits(List<T> edits) {
        this.edits = edits;
    }
}
