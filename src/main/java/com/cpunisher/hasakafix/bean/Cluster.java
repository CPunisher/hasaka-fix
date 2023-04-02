package com.cpunisher.hasakafix.bean;

import java.util.List;
import java.util.function.Consumer;

public record Cluster<T> (
        T pattern,
        List<Cluster<T>> children
) {
    public void preOrder(Consumer<Cluster<T>> visitor) {
        visitor.accept(this);
        for (var child : children) {
            child.preOrder(visitor);
        }
    }
}
