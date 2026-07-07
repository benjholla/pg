package io.github.benjholla.pg.universe.ephemeral;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import io.github.benjholla.pg.api.NodeSet;
import io.github.benjholla.pg.api.AttributeValue;

public class EphemeralUnmodifiableLiveNodeSetTest {

    @Test
    public void testUnsupportedOperations() {
        Map<Integer, EphemeralNode> map = new HashMap<>();
        Map<Integer, EphemeralEdge> edges = new HashMap<>();
        Map<Integer, EphemeralEdgeSet> inEdges = new HashMap<>();
        Map<Integer, EphemeralEdgeSet> outEdges = new HashMap<>();
        EphemeralUnmodifiableLiveNodeSet set = new EphemeralUnmodifiableLiveNodeSet(map, edges, inEdges, outEdges);
        EphemeralNode n = new EphemeralNode();

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
        Map<Integer, EphemeralNode> map = new HashMap<>();
        Map<Integer, EphemeralEdge> edges = new HashMap<>();
        Map<Integer, EphemeralEdgeSet> inEdges = new HashMap<>();
        Map<Integer, EphemeralEdgeSet> outEdges = new HashMap<>();
        EphemeralNode n1 = new EphemeralNode();
        map.put(n1.id(), n1);
        EphemeralUnmodifiableLiveNodeSet set = new EphemeralUnmodifiableLiveNodeSet(map, edges, inEdges, outEdges);

        assertTrue(set.contains(n1));
        assertFalse(set.contains(new EphemeralNode()));
        assertFalse(set.contains(new Object()));

        assertEquals(1, set.size());
        assertFalse(set.isEmpty());
        assertTrue(set.containsAll(Collections.singletonList(n1)));

        EphemeralNode missing = new EphemeralNode();
        assertFalse(set.containsAll(Arrays.asList(n1, missing)));

        assertEquals(n1, set.iterator().next());

        assertNotNull(set.toArray());
        assertNotNull(set.toArray(new EphemeralNode[0]));
        assertNotNull(set.toArray(EphemeralNode[]::new));
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
        for (EphemeralNode n : map.values()) {
            h += n.hashCode();
        }
        assertEquals(h, set.hashCode());

        EphemeralUnmodifiableLiveNodeSet sameSet = new EphemeralUnmodifiableLiveNodeSet(map, edges, inEdges, outEdges);
        assertTrue(set.equals(sameSet));
        assertTrue(set.equals(set));
        assertFalse(set.equals(null));
        assertFalse(set.equals("String"));

        Map<Integer, EphemeralNode> diffMap = new HashMap<>();
        diffMap.put(missing.id(), missing);
        EphemeralUnmodifiableLiveNodeSet diffSet = new EphemeralUnmodifiableLiveNodeSet(diffMap, edges, inEdges, outEdges);
        assertFalse(set.equals(diffSet));

        Map<Integer, EphemeralNode> diffMapSize = new HashMap<>();
        EphemeralUnmodifiableLiveNodeSet diffSetSize = new EphemeralUnmodifiableLiveNodeSet(diffMapSize, edges, inEdges, outEdges);
        assertFalse(set.equals(diffSetSize));
    }

    @Test
    public void testSetTheoreticAndFilteringMethods() {
        Map<Integer, EphemeralNode> map = new HashMap<>();
        Map<Integer, EphemeralEdge> edges = new HashMap<>();
        Map<Integer, EphemeralEdgeSet> inEdges = new HashMap<>();
        Map<Integer, EphemeralEdgeSet> outEdges = new HashMap<>();
        EphemeralNode n1 = new EphemeralNode();
        n1.attributes().put("type", "A");
        n1.attributes().put("val", 1);
        EphemeralNode n2 = new EphemeralNode();
        n2.attributes().put("type", "B");
        n2.attributes().put("val", 2);

        map.put(n1.id(), n1);
        map.put(n2.id(), n2);

        EphemeralUnmodifiableLiveNodeSet set = new EphemeralUnmodifiableLiveNodeSet(map, edges, inEdges, outEdges);

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

        EphemeralNode n3 = new EphemeralNode();
        NodeSet union = set.union(Collections.singletonList(n3));
        assertEquals(3, union.size());
        assertTrue(union.contains(n3));
    }
}
