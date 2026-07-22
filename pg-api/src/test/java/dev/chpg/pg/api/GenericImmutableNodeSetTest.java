package dev.chpg.pg.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

public class GenericImmutableNodeSetTest {

    @Test
    public void testBasicProperties() {
        Node n1 = new TestDummyNode() { @Override public int id() { return 1; } };
        Node n2 = new TestDummyNode() { @Override public int id() { return 2; } };

        GenericImmutableNodeSet set = new GenericImmutableNodeSet(List.of(n1, n2));

        assertEquals(2, set.size());
        assertFalse(set.isEmpty());
        assertTrue(set.contains(n1));
        assertTrue(set.contains(n2));
        assertFalse(set.contains(new TestDummyNode() { @Override public int id() { return 3; } }));

        assertTrue(set.one().isPresent());
        assertTrue(set.one().get() == n1 || set.one().get() == n2);

        assertTrue(set.ids().contains(1));
        assertTrue(set.ids().contains(2));

        int[] arr = set.toIdArray();
        assertEquals(2, arr.length);
        assertTrue(arr[0] == 1 || arr[0] == 2);
        assertTrue(arr[1] == 1 || arr[1] == 2);

        assertTrue(set.isMaterialized());
        assertTrue(set.isSizeKnown());
    }

    @Test
    public void testEmptyProperties() {
        GenericImmutableNodeSet emptySet = new GenericImmutableNodeSet(Collections.emptyList());
        assertEquals(0, emptySet.size());
        assertTrue(emptySet.isEmpty());
        assertFalse(emptySet.one().isPresent());
        assertTrue(emptySet.ids().isEmpty());
        assertEquals(0, emptySet.toIdArray().length);
    }

    @Test
    public void testImmutability() {
        GenericImmutableNodeSet set = new GenericImmutableNodeSet(List.of(new TestDummyNode()));
        assertSame(set, set.materialize());
        assertSame(set, set.toImmutable());
        assertThrows(UnsupportedOperationException.class, () -> set.add(new TestDummyNode()));
    }

    @Test
    public void testIntersect() {
        Node n1 = new TestDummyNode() { @Override public int id() { return 1; } };
        Node n2 = new TestDummyNode() { @Override public int id() { return 2; } };
        GenericImmutableNodeSet set = new GenericImmutableNodeSet(List.of(n1, n2));

        assertThrows(NullPointerException.class, () -> set.intersect(null));

        assertTrue(set.intersect(Collections.emptyList()).isEmpty());

        NodeSet intersection = set.intersect(List.of(n1, new TestDummyNode() { @Override public int id() { return 3; } }));
        assertEquals(1, intersection.size());
        assertTrue(intersection.contains(n1));
    }

    @Test
    public void testDifference() {
        Node n1 = new TestDummyNode() { @Override public int id() { return 1; } };
        Node n2 = new TestDummyNode() { @Override public int id() { return 2; } };
        GenericImmutableNodeSet set = new GenericImmutableNodeSet(List.of(n1, n2));

        assertThrows(NullPointerException.class, () -> set.difference(null));

        assertSame(set, set.difference(Collections.emptyList()));

        NodeSet diff = set.difference(List.of(n1, new TestDummyNode() { @Override public int id() { return 3; } }));
        assertEquals(1, diff.size());
        assertTrue(diff.contains(n2));

        NodeSet emptyDiff = set.difference(List.of(n1, n2));
        assertTrue(emptyDiff.isEmpty());
    }

    @Test
    public void testUnion() {
        Node n1 = new TestDummyNode() { @Override public int id() { return 1; } };
        Node n2 = new TestDummyNode() { @Override public int id() { return 2; } };
        GenericImmutableNodeSet set = new GenericImmutableNodeSet(List.of(n1));

        assertThrows(NullPointerException.class, () -> set.union(null));

        assertSame(set, set.union(Collections.emptyList()));

        NodeSet union = set.union(List.of(n2));
        assertEquals(2, union.size());
        assertTrue(union.contains(n1));
        assertTrue(union.contains(n2));
    }
}
