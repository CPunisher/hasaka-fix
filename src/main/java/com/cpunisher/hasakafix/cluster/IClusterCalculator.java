package com.cpunisher.hasakafix.cluster;

import com.cpunisher.hasakafix.antiunification.bean.AntiUnifyData;
import com.cpunisher.hasakafix.bean.Cluster;
import com.cpunisher.hasakafix.edit.editor.gumtree.GTTreeEdit;
import com.github.gumtreediff.utils.Pair;

import java.util.List;

public interface IClusterCalculator<T> {
    List<Pair<String, Double>> cost(List<Cluster<T>> clusters, AntiUnifyData antiUnifyData);
}
