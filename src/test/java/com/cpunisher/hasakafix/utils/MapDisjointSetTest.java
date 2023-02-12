package com.cpunisher.hasakafix.utils;

import com.cpunisher.hasakafix.utils.disjointset.IDisjointSet;
import com.cpunisher.hasakafix.utils.disjointset.MapDisjointSet;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class MapDisjointSetTest {

    @Test
    public void testFind() {
        IDisjointSet<Integer> disjointSet = new MapDisjointSet<>();
        Set<Integer> set = disjointSet.find(1);

        assertEquals(1, set.size());
        assertTrue(set.contains(1));
    }

    @Test
    public void testUnion() {
        IDisjointSet<Integer> disjointSet = new MapDisjointSet<>();
        assertFalse(disjointSet.isUnion(1, 2));

        disjointSet.union(1, 2);
        disjointSet.union(1, 2);
        assertTrue(disjointSet.isUnion(1, 2));
        assertEquals(1, disjointSet.findAll().size());
    }

    @Test
    public void testMultipleUnion() {
        IDisjointSet<Integer> disjointSet = new MapDisjointSet<>();
        disjointSet.union(1, 2);
        disjointSet.union(3, 4);
        disjointSet.union(1, 4);

        Set<Integer> set = disjointSet.find(1);
        assertEquals(4, set.size());
        assertEquals(1, disjointSet.findAll().size());
    }
}
