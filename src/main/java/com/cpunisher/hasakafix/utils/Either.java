package com.cpunisher.hasakafix.utils;

import java.util.function.Consumer;
import java.util.function.Function;

public class Either<A, B> {
    private final A first;
    private final B second;

    public Either(A first, B second) {
        this.first = first;
        this.second = second;
    }

    public static <A, B> Either<A, B> first(A first) {
        return new Either<>(first, null);
    }

    public static <A, B> Either<A, B> second(B second) {
        return new Either<>(null, second);
    }

    public void ifFirst(Consumer<A> consumer) {
        if (first != null) {
            consumer.accept(first);
        }
    }

    public void ifSecond(Consumer<B> consumer) {
        if (second != null) {
            consumer.accept(second);
        }
    }

    public boolean isFirst() {
        return first != null;
    }

    public boolean isSecond() {
        return second != null;
    }

    public <T> T fold(Function<A, T> ifFirst, Function<B, T> ifSecond) {
        if (this.first != null) return ifFirst.apply(this.first);
        if (this.second != null) return ifSecond.apply(this.second);
        throw new IllegalStateException("Either first and second is null");
    }

    public A getFirst() {
        return first;
    }

    public B getSecond() {
        return second;
    }

    @Override
    public String toString() {
        return fold(Object::toString, Object::toString);
    }
}
