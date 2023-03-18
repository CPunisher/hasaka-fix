package com.cpunisher.hasakafix.antiunification;

import com.cpunisher.hasakafix.antiunification.bean.AntiUnifyData;

import java.util.List;

public interface IAntiUnifier<T> {
    List<AntiUnifyData<T>> antiUnify(T left, T right);
}
