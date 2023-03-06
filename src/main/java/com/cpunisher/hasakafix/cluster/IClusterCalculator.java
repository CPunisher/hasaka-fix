package com.cpunisher.hasakafix.cluster;

import com.cpunisher.hasakafix.bean.Cluster;

import java.util.List;

public interface IClusterCalculator<T> {
    List<Cluster<T>> cluster(List<T> edits);
}