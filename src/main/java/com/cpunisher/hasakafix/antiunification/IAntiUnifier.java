package com.cpunisher.hasakafix.antiunification;

public interface IAntiUnifier<T> {
    AUNode antiUnify(T before, T after);
}
