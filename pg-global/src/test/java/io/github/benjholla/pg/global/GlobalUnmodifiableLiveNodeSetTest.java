package io.github.benjholla.pg.global;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import io.github.benjholla.pg.api.NodeSet;
import io.github.benjholla.pg.api.AttributeValue;

public class GlobalUnmodifiableLiveNodeSetTest {

    @Test
    public void testUnsupportedOperations() {
        Map<Integer, GlobalNode> map = new HashMap<>();
        Map<Integer, GlobalEdge> edges = new HashMap<>();
        Map<Integer, GlobalEdgeSet> inEdges = new HashMap<>();
        Map<Integer, GlobalEdgeSet> outEdges = new HashMap<>();
        GlobalUnmodifiableLiveNodeSet set = new GlobalUnmodifiableLiveNodeSet(map, edges, inEdges, outEdges);
        GlobalNode n = new GlobalNode();

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
        Map<Integer, GlobalNode> map = new HashMap<>();
        Map<Integer, GlobalEdge> edges = new HashMap<>();
        Map<Integer, GlobalEdgeSet> inEdges = new HashMap<>();
        Map<Integer, GlobalEdgeSet> outEdges = new HashMap<>();
        GlobalNode n1 = new GlobalNode();
        map.put(n1.id(), n1);
        GlobalUnmodifiableLiveNodeSet set = new GlobalUnmodifiableLiveNodeSet(map, edges, inEdges, outEdges);

        assertTrue(set.contains(n1));
        assertFalse(set.contains(new GlobalNode()));
        assertFalse(set.contains(new Object()));

        assertEquals(1, set.size());
        assertFalse(set.isEmpty());
        assertTrue(set.containsAll(Collections.singletonList(n1)));

        GlobalNode missing = new GlobalNode();
        assertFalse(set.containsAll(Arrays.asList(n1, missing)));

        assertEquals(n1, set.iterator().next());

        assertNotNull(set.toArray());
        assertNotNull(set.toArray(new GlobalNode[0]));
        assertNotNull(set.toArray(GlobalNode[]::new));
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
        for (GlobalNode n : map.values()) {
            h += n.hashCode();
        }
        assertEquals(h, set.hashCode());

        GlobalUnmodifiableLiveNodeSet sameSet = new GlobalUnmodifiableLiveNodeSet(map, edges, inEdges, outEdges);
        assertTrue(set.equals(sameSet));
        assertTrue(set.equals(set));
        assertFalse(set.equals(null));
        assertFalse(set.equals("String"));

        Map<Integer, GlobalNode> diffMap = new HashMap<>();
        diffMap.put(missing.id(), missing);
        GlobalUnmodifiableLiveNodeSet diffSet = new GlobalUnmodifiableLiveNodeSet(diffMap, edges, inEdges, outEdges);
        assertFalse(set.equals(diffSet));

        Map<Integer, GlobalNode> diffMapSize = new HashMap<>();
        GlobalUnmodifiableLiveNodeSet diffSetSize = new GlobalUnmodifiableLiveNodeSet(diffMapSize, edges, inEdges, outEdges);
        assertFalse(set.equals(diffSetSize));
    }

    @Test
    public void testSetTheoreticAndFilteringMethods() {
        Map<Integer, GlobalNode> map = new HashMap<>();
        Map<Integer, GlobalEdge> edges = new HashMap<>();
        Map<Integer, GlobalEdgeSet> inEdges = new HashMap<>();
        Map<Integer, GlobalEdgeSet> outEdges = new HashMap<>();
        GlobalNode n1 = new GlobalNode();
        n1.attributes().put("type", "A");
        n1.attributes().put("val", 1);
        GlobalNode n2 = new GlobalNode();
        n2.attributes().put("type", "B");
        n2.attributes().put("val", 2);

        map.put(n1.id(), n1);
        map.put(n2.id(), n2);

        GlobalUnmodifiableLiveNodeSet set = new GlobalUnmodifiableLiveNodeSet(map, edges, inEdges, outEdges);

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

        GlobalNode n3 = new GlobalNode();
        NodeSet union = set.union(Collections.singletonList(n3));
        assertEquals(3, union.size());
        assertTrue(union.contains(n3));
    }
}
