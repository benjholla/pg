package dev.chpg.pg.multiverse.universe;

import dev.chpg.pg.api.NodeSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UniverseImmutableSingletonNodeSetTest {

    private Universe universe;
    private UniverseNode node1;
    private UniverseNode node2;
    private UniverseImmutableSingletonNodeSet singleton;

    @BeforeEach
    void setUp() {
        universe = new Universe();
        node1 = new UniverseNode(universe, 1);
        node2 = new UniverseNode(universe, 2);
        singleton = new UniverseImmutableSingletonNodeSet(node1);
    }

    @Test
    void testBasicProperties() {
        assertEquals(1, singleton.size());
        assertTrue(singleton.isMaterialized());
        assertTrue(singleton.isSizeKnown());
        assertTrue(singleton.contains(node1));
        assertFalse(singleton.contains(node2));
        assertEquals(node1, singleton.one().orElse(null));
        assertArrayEquals(new int[]{1}, singleton.toIdArray());
        assertEquals(Collections.singleton(1), singleton.ids());
    }

    @Test
    void testUniverseView() {
        assertEquals(universe, singleton.universe());
    }

    @Test
    void testUnsupportedMutations() {
        assertThrows(UnsupportedOperationException.class, () -> singleton.add(node2));
        assertThrows(UnsupportedOperationException.class, () -> singleton.remove(node1));
        assertThrows(UnsupportedOperationException.class, () -> singleton.clear());
    }

    @Test
    void testSetOperationsIntersect() {
        NodeSet intersectSelf = singleton.intersect(Collections.singletonList(node1));
        assertEquals(1, intersectSelf.size());
        assertTrue(intersectSelf.contains(node1));

        NodeSet intersectEmpty = singleton.intersect(Collections.singletonList(node2));
        assertEquals(0, intersectEmpty.size());
    }

    @Test
    void testSetOperationsDifference() {
        NodeSet diffSelf = singleton.difference(Collections.singletonList(node1));
        assertEquals(0, diffSelf.size());

        NodeSet diffOther = singleton.difference(Collections.singletonList(node2));
        assertEquals(1, diffOther.size());
        assertTrue(diffOther.contains(node1));
    }

    @Test
    void testSetOperationsUnion() {
        // Union with empty
        NodeSet unionEmpty = singleton.union(Collections.emptyList());
        assertEquals(1, unionEmpty.size());
        assertTrue(unionEmpty instanceof UniverseImmutableSingletonNodeSet);

        // Union with same element
        NodeSet unionSame = singleton.union(Collections.singletonList(node1));
        assertEquals(1, unionSame.size());
        assertTrue(unionSame instanceof UniverseImmutableSingletonNodeSet);

        // Union with different element in same universe
        NodeSet unionDiff = singleton.union(Collections.singletonList(node2));
        assertEquals(2, unionDiff.size());
        assertTrue(unionDiff.contains(node1));
        assertTrue(unionDiff.contains(node2));
        assertTrue(unionDiff instanceof UniverseNodeSet);

        // Union with foreign node
        Universe otherUniverse = new Universe();
        UniverseNode foreignNode = new UniverseNode(otherUniverse, 3);
        assertThrows(IllegalArgumentException.class, () -> singleton.union(Collections.singletonList(foreignNode)));
    }
}
