package com.cpunisher.hasakafix.antiunification;

public interface IAntiUnifier<T> {
    String antiUnify(T before, T after);
}
