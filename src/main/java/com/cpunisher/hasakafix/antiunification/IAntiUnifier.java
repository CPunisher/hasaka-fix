package com.cpunisher.hasakafix.antiunification;

import com.cpunisher.hasakafix.antiunification.bean.AntiUnifyData;

import java.util.List;

public interface IAntiUnifier {
    List<AntiUnifyData> antiUnify(String before, String after);
}
