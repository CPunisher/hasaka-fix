package com.cpunisher.hasakafix.bean;

import java.util.List;

public record Cluster<T> (
        T pattern,
        List<Cluster<T>> children
) { }
