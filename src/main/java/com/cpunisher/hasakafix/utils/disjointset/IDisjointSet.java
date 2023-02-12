package com.cpunisher.hasakafix.utils.disjointset;

import java.util.Set;

public interface IDisjointSet<E> {

    public void union(E e1, E e2);

    public Set<E> find(E e);

    public boolean isUnion(E e1, E e2);

    public Set<Set<E>> findAll();
}
