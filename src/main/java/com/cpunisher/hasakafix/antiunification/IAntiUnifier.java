package com.cpunisher.hasakafix.antiunification;

import com.cpunisher.hasakafix.antiunification.bean.AntiUnifyData;

import java.util.List;

public interface IAntiUnifier<T> {
    List<AntiUnifyData> antiUnify(T before, T after);
    List<AntiUnifyData> antiUnify(String before, String after);
}
