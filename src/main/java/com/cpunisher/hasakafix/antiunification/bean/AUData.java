package com.cpunisher.hasakafix.antiunification.bean;

import java.util.List;

public record AUData(String unifier, List<Hole> holes) {
    @Override
    public String toString() {
        return this.unifier;
    }
}
