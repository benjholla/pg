package io.github.benjholla.pg.heavy;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import io.github.benjholla.pg.api.NodeSet;
import io.github.benjholla.pg.api.AttributeValue;

public class HeavyUnmodifiableLiveNodeSetTest {

    @Test
    public void testUnsupportedOperations() {
        Map<Integer, HeavyNode> map = new HashMap<>();
        Map<Integer, HeavyEdge> edges = new HashMap<>();
        Map<Integer, HeavyEdgeSet> inEdges = new HashMap<>();
        Map<Integer, HeavyEdgeSet> outEdges = new HashMap<>();
        HeavyUnmodifiableLiveNodeSet set = new HeavyUnmodifiableLiveNodeSet(map, edges, inEdges, outEdges);
        HeavyNode n = new HeavyNode();

        assertThrows(UnsupportedOperationException.class, () -> set.add(n));
        assertThrows(UnsupportedOperationException.class, () -> set.remove(n));
        assertThrows(UnsupportedOperationException.class, () -> set.clear());
        assertThrows(UnsupportedOperationException.class, () -> set.addAll(Collections.singletonList(n)));
        assertThrows(UnsupportedOperationException.class, () -> set.removeAll(Collections.singletonList(n)));
        assertThrows(UnsupportedOperationException.class, () -> set.retainAll(Collections.singletonList(n)));
        assertThrows(UnsupportedOperationException.class, () -> set.removeIf(x -> true));

        map.put(n.id(), n);
        assertThrows(UnsupportedOperationException.class, () -> {
            var it = set.iterator();
            it.next();
            it.remove();
        });
    }

    @Test
    public void testDelegatedMethods() {
        Map<Integer, HeavyNode> map = new HashMap<>();
        Map<Integer, HeavyEdge> edges = new HashMap<>();
        Map<Integer, HeavyEdgeSet> inEdges = new HashMap<>();
        Map<Integer, HeavyEdgeSet> outEdges = new HashMap<>();
        HeavyNode n1 = new HeavyNode();
        map.put(n1.id(), n1);
        HeavyUnmodifiableLiveNodeSet set = new HeavyUnmodifiableLiveNodeSet(map, edges, inEdges, outEdges);

        assertTrue(set.contains(n1));
        assertFalse(set.contains(new HeavyNode()));
        assertFalse(set.contains(new Object()));

        assertEquals(1, set.size());
        assertFalse(set.isEmpty());
        assertTrue(set.containsAll(Collections.singletonList(n1)));

        HeavyNode missing = new HeavyNode();
        assertFalse(set.containsAll(Arrays.asList(n1, missing)));

        assertEquals(n1, set.iterator().next());

        assertNotNull(set.toArray());
        assertNotNull(set.toArray(new HeavyNode[0]));
        assertNotNull(set.toArray(HeavyNode[]::new));
        assertNotNull(set.spliterator());
        assertNotNull(set.stream());
        assertNotNull(set.parallelStream());
        assertNotNull(set.toString());

        assertTrue(set.ids().contains(n1.id()));
        assertArrayEquals(new int[]{n1.id()}, set.toIdArray());

        AtomicInteger count = new AtomicInteger();
        set.forEach(n -> count.incrementAndGet());
        assertEquals(1, count.get());

        int h = 0;
        for (HeavyNode n : map.values()) {
            h += n.hashCode();
        }
        assertEquals(h, set.hashCode());

        HeavyUnmodifiableLiveNodeSet sameSet = new HeavyUnmodifiableLiveNodeSet(map, edges, inEdges, outEdges);
        assertTrue(set.equals(sameSet));
        assertTrue(set.equals(set));
        assertFalse(set.equals(null));
        assertFalse(set.equals("String"));

        Map<Integer, HeavyNode> diffMap = new HashMap<>();
        diffMap.put(missing.id(), missing);
        HeavyUnmodifiableLiveNodeSet diffSet = new HeavyUnmodifiableLiveNodeSet(diffMap, edges, inEdges, outEdges);
        assertFalse(set.equals(diffSet));

        Map<Integer, HeavyNode> diffMapSize = new HashMap<>();
        HeavyUnmodifiableLiveNodeSet diffSetSize = new HeavyUnmodifiableLiveNodeSet(diffMapSize, edges, inEdges, outEdges);
        assertFalse(set.equals(diffSetSize));
    }

    @Test
    public void testSetTheoreticAndFilteringMethods() {
        Map<Integer, HeavyNode> map = new HashMap<>();
        Map<Integer, HeavyEdge> edges = new HashMap<>();
        Map<Integer, HeavyEdgeSet> inEdges = new HashMap<>();
        Map<Integer, HeavyEdgeSet> outEdges = new HashMap<>();
        HeavyNode n1 = new HeavyNode();
        n1.attributes().put("type", "A");
        n1.attributes().put("val", 1);
        HeavyNode n2 = new HeavyNode();
        n2.attributes().put("type", "B");
        n2.attributes().put("val", 2);

        map.put(n1.id(), n1);
        map.put(n2.id(), n2);

        HeavyUnmodifiableLiveNodeSet set = new HeavyUnmodifiableLiveNodeSet(map, edges, inEdges, outEdges);

        assertTrue(set.one().isPresent());

        NodeSet filteredA = set.filter("type", new AttributeValue.StringVal("A"));
        assertEquals(1, filteredA.size());
        assertTrue(filteredA.contains(n1));

        NodeSet filteredVal = set.filter("val");
        assertEquals(2, filteredVal.size());

        NodeSet filterNull = set.filter(null, new AttributeValue.StringVal("A"));
        assertEquals(0, filterNull.size());

        NodeSet intersectEmpty = set.intersect(null);
        assertEquals(0, intersectEmpty.size());

        NodeSet intersect = set.intersect(Collections.singletonList(n1));
        assertEquals(1, intersect.size());
        assertTrue(intersect.contains(n1));

        NodeSet diffEmpty = set.difference(null);
        assertEquals(2, diffEmpty.size());

        NodeSet diff = set.difference(Collections.singletonList(n1));
        assertEquals(1, diff.size());
        assertTrue(diff.contains(n2));

        NodeSet unionEmpty = set.union(null);
        assertEquals(2, unionEmpty.size());

        HeavyNode n3 = new HeavyNode();
        NodeSet union = set.union(Collections.singletonList(n3));
        assertEquals(3, union.size());
        assertTrue(union.contains(n3));
    }
}
