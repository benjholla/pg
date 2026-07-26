package dev.chpg.pg.multiverse.universe;

import dev.chpg.pg.api.EdgeSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UniverseImmutableSingletonEdgeSetTest {

    private Universe universe;
    private UniverseEdge edge1;
    private UniverseEdge edge2;
    private UniverseImmutableSingletonEdgeSet singleton;

    @BeforeEach
    void setUp() {
        universe = new Universe();
        edge1 = new UniverseEdge(universe, 1);
        edge2 = new UniverseEdge(universe, 2);
        singleton = new UniverseImmutableSingletonEdgeSet(edge1);
    }

    @Test
    void testBasicProperties() {
        assertEquals(1, singleton.size());
        assertTrue(singleton.isMaterialized());
        assertTrue(singleton.isSizeKnown());
        assertTrue(singleton.contains(edge1));
        assertFalse(singleton.contains(edge2));
        assertEquals(edge1, singleton.one().orElse(null));
        assertArrayEquals(new int[]{1}, singleton.toIdArray());
        assertEquals(Collections.singleton(1), singleton.ids());
    }

    @Test
    void testUniverseView() {
        assertEquals(universe, singleton.universe());
    }

    @Test
    void testUnsupportedMutations() {
        assertThrows(UnsupportedOperationException.class, () -> singleton.add(edge2));
        assertThrows(UnsupportedOperationException.class, () -> singleton.remove(edge1));
        assertThrows(UnsupportedOperationException.class, () -> singleton.clear());
    }

    @Test
    void testSetOperationsIntersect() {
        EdgeSet intersectSelf = singleton.intersect(Collections.singletonList(edge1));
        assertEquals(1, intersectSelf.size());
        assertTrue(intersectSelf.contains(edge1));

        EdgeSet intersectEmpty = singleton.intersect(Collections.singletonList(edge2));
        assertEquals(0, intersectEmpty.size());
    }

    @Test
    void testSetOperationsDifference() {
        EdgeSet diffSelf = singleton.difference(Collections.singletonList(edge1));
        assertEquals(0, diffSelf.size());

        EdgeSet diffOther = singleton.difference(Collections.singletonList(edge2));
        assertEquals(1, diffOther.size());
        assertTrue(diffOther.contains(edge1));
    }

    @Test
    void testSetOperationsUnion() {
        // Union with empty
        EdgeSet unionEmpty = singleton.union(Collections.emptyList());
        assertEquals(1, unionEmpty.size());
        assertTrue(unionEmpty instanceof UniverseImmutableSingletonEdgeSet);

        // Union with same element
        EdgeSet unionSame = singleton.union(Collections.singletonList(edge1));
        assertEquals(1, unionSame.size());
        assertTrue(unionSame instanceof UniverseImmutableSingletonEdgeSet);

        // Union with different element in same universe
        EdgeSet unionDiff = singleton.union(Collections.singletonList(edge2));
        assertEquals(2, unionDiff.size());
        assertTrue(unionDiff.contains(edge1));
        assertTrue(unionDiff.contains(edge2));
        assertTrue(unionDiff instanceof UniverseEdgeSet);

        // Union with foreign edge
        Universe otherUniverse = new Universe();
        UniverseEdge foreignEdge = new UniverseEdge(otherUniverse, 3);
        assertThrows(IllegalArgumentException.class, () -> singleton.union(Collections.singletonList(foreignEdge)));
    }
}
