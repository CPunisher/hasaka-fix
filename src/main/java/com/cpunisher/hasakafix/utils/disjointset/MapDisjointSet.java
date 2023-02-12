package com.cpunisher.hasakafix.utils.disjointset;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MapDisjointSet<E> implements IDisjointSet<E> {

    private final Map<E, Set<E>> groups = new HashMap<>();

    @Override
    public void union(E e1, E e2) {
        Set<E> set1 = computeValueOf(e1);
        Set<E> set2 = computeValueOf(e2);

        if (set1 == set2) {
            return;
        }

        Set<E> newSet = Stream.of(set1, set2)
                .flatMap(Set::stream)
                .collect(Collectors.toUnmodifiableSet());
        for (E e : newSet) {
            groups.put(e, newSet);
        }
    }

    @Override
    public Set<E> find(E e) {
        return computeValueOf(e);
    }

    @Override
    public boolean isUnion(E e1, E e2) {
        return computeValueOf(e1) == computeValueOf(e2);
    }

    @Override
    public Set<Set<E>> findAll() {
        return groups.values().stream().distinct().collect(Collectors.toSet());
    }

    private Set<E> computeValueOf(E e) {
        return groups.computeIfAbsent(e, element -> Set.of(e));
    }
}
