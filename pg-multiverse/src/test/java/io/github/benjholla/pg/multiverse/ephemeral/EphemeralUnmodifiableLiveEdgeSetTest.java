package io.github.benjholla.pg.multiverse.ephemeral;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import io.github.benjholla.pg.api.EdgeSet;
import io.github.benjholla.pg.api.AttributeValue;

public class EphemeralUnmodifiableLiveEdgeSetTest {

    @Test
    public void testUnsupportedOperations() {
        EphemeralGraph graph = new EphemeralGraph();
        Map<Integer, EphemeralNode> nodes = new HashMap<>();
        Map<Integer, EphemeralEdge> edges = new HashMap<>();
        Map<Integer, EphemeralEdgeSet> inEdges = new HashMap<>();
        Map<Integer, EphemeralEdgeSet> outEdges = new HashMap<>();
        EphemeralUnmodifiableLiveEdgeSet set = new EphemeralUnmodifiableLiveEdgeSet(nodes, edges, inEdges, outEdges);
        EphemeralNode n1 = graph.createNode();
        EphemeralNode n2 = graph.createNode();
        EphemeralEdge e = graph.createEdge(n1, n2);

        assertThrows(UnsupportedOperationException.class, () -> set.add(e));
        assertThrows(UnsupportedOperationException.class, () -> set.remove(e));
        assertThrows(UnsupportedOperationException.class, () -> set.clear());
        assertThrows(UnsupportedOperationException.class, () -> set.addAll(Collections.singletonList(e)));
        assertThrows(UnsupportedOperationException.class, () -> set.removeAll(Collections.singletonList(e)));
        assertThrows(UnsupportedOperationException.class, () -> set.retainAll(Collections.singletonList(e)));
        assertThrows(UnsupportedOperationException.class, () -> set.removeIf(x -> true));

        edges.put(e.id(), e);
        assertThrows(UnsupportedOperationException.class, () -> {
            var it = set.iterator();
            it.next();
            it.remove();
        });
    }

    @Test
    public void testDelegatedMethods() {
        EphemeralGraph graph = new EphemeralGraph();
        Map<Integer, EphemeralNode> nodes = new HashMap<>();
        Map<Integer, EphemeralEdge> edges = new HashMap<>();
        Map<Integer, EphemeralEdgeSet> inEdges = new HashMap<>();
        Map<Integer, EphemeralEdgeSet> outEdges = new HashMap<>();
        EphemeralNode n1 = graph.createNode();
        EphemeralNode n2 = graph.createNode();
        EphemeralEdge e1 = graph.createEdge(n1, n2);
        edges.put(e1.id(), e1);
        EphemeralUnmodifiableLiveEdgeSet set = new EphemeralUnmodifiableLiveEdgeSet(nodes, edges, inEdges, outEdges);

        assertTrue(set.contains(e1));
        assertFalse(set.contains(graph.createEdge(n1, n2)));
        assertFalse(set.contains(new Object()));

        assertEquals(1, set.size());
        assertFalse(set.isEmpty());
        assertTrue(set.containsAll(Collections.singletonList(e1)));

        EphemeralEdge missing = graph.createEdge(n1, n2);
        assertFalse(set.containsAll(Arrays.asList(e1, missing)));

        assertEquals(e1, set.iterator().next());

        assertNotNull(set.toArray());
        assertNotNull(set.toArray(new EphemeralEdge[0]));
        assertNotNull(set.toArray(EphemeralEdge[]::new));
        assertNotNull(set.spliterator());
        assertNotNull(set.stream());
        assertNotNull(set.parallelStream());
        assertNotNull(set.toString());

        assertTrue(set.ids().contains(e1.id()));
        assertArrayEquals(new int[]{e1.id()}, set.toIdArray());

        AtomicInteger count = new AtomicInteger();
        set.forEach(n -> count.incrementAndGet());
        assertEquals(1, count.get());

        int h = 0;
        for (EphemeralEdge n : edges.values()) {
            h += n.hashCode();
        }
        assertEquals(h, set.hashCode());

        EphemeralUnmodifiableLiveEdgeSet sameSet = new EphemeralUnmodifiableLiveEdgeSet(nodes, edges, inEdges, outEdges);
        assertTrue(set.equals(sameSet));
        assertTrue(set.equals(set));
        assertFalse(set.equals(null));
        assertFalse(set.equals("String"));

        Map<Integer, EphemeralEdge> diffEdges = new HashMap<>();
        diffEdges.put(missing.id(), missing);
        EphemeralUnmodifiableLiveEdgeSet diffSet = new EphemeralUnmodifiableLiveEdgeSet(nodes, diffEdges, inEdges, outEdges);
        assertFalse(set.equals(diffSet));

        Map<Integer, EphemeralEdge> diffEdgesSize = new HashMap<>();
        EphemeralUnmodifiableLiveEdgeSet diffSetSize = new EphemeralUnmodifiableLiveEdgeSet(nodes, diffEdgesSize, inEdges, outEdges);
        assertFalse(set.equals(diffSetSize));
    }

    @Test
    public void testSetTheoreticAndFilteringMethods() {
        EphemeralGraph graph = new EphemeralGraph();
        Map<Integer, EphemeralNode> nodes = new HashMap<>();
        Map<Integer, EphemeralEdge> edges = new HashMap<>();
        Map<Integer, EphemeralEdgeSet> inEdges = new HashMap<>();
        Map<Integer, EphemeralEdgeSet> outEdges = new HashMap<>();
        EphemeralNode n1 = graph.createNode();
        EphemeralNode n2 = graph.createNode();
        EphemeralNode n3 = graph.createNode();
        EphemeralEdge e1 = graph.createEdge(n1, n2);
        e1.attributes().put("type", "A");
        e1.attributes().put("val", 1);
        EphemeralEdge e2 = graph.createEdge(n2, n3);
        e2.attributes().put("type", "B");
        e2.attributes().put("val", 2);

        edges.put(e1.id(), e1);
        edges.put(e2.id(), e2);

        EphemeralUnmodifiableLiveEdgeSet set = new EphemeralUnmodifiableLiveEdgeSet(nodes, edges, inEdges, outEdges);

        assertTrue(set.one().isPresent());

        EdgeSet filteredA = set.filter("type", new AttributeValue.StringVal("A"));
        assertEquals(1, filteredA.size());
        assertTrue(filteredA.contains(e1));

        EdgeSet filteredVal = set.filter("val");
        assertEquals(2, filteredVal.size());

        EdgeSet filterNull = set.filter(null, new AttributeValue.StringVal("A"));
        assertEquals(0, filterNull.size());

        EdgeSet intersectEmpty = set.intersect(null);
        assertEquals(0, intersectEmpty.size());

        EdgeSet intersect = set.intersect(Collections.singletonList(e1));
        assertEquals(1, intersect.size());
        assertTrue(intersect.contains(e1));

        EdgeSet diffEmpty = set.difference(null);
        assertEquals(2, diffEmpty.size());

        EdgeSet diff = set.difference(Collections.singletonList(e1));
        assertEquals(1, diff.size());
        assertTrue(diff.contains(e2));

        EdgeSet unionEmpty = set.union(null);
        assertEquals(2, unionEmpty.size());

        EphemeralEdge e3 = graph.createEdge(n3, n1);
        EdgeSet union = set.union(Collections.singletonList(e3));
        assertEquals(3, union.size());
        assertTrue(union.contains(e3));
    }
}
