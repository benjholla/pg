package dev.chpg.pg.global;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import dev.chpg.pg.api.EdgeSet;
import dev.chpg.pg.api.AttributeValue;

public class GlobalUnmodifiableLiveEdgeSetTest {

    @Test
    public void testUnsupportedOperations() {
        Map<Integer, GlobalNode> nodes = new HashMap<>();
        Map<Integer, GlobalEdge> edges = new HashMap<>();
        Map<Integer, GlobalEdgeSet> inEdges = new HashMap<>();
        Map<Integer, GlobalEdgeSet> outEdges = new HashMap<>();
        GlobalUnmodifiableLiveEdgeSet set = new GlobalUnmodifiableLiveEdgeSet(nodes, edges, inEdges, outEdges);
        GlobalNode n1 = new GlobalNode();
        GlobalNode n2 = new GlobalNode();
        GlobalEdge e = new GlobalEdge(n1, n2);

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
        Map<Integer, GlobalNode> nodes = new HashMap<>();
        Map<Integer, GlobalEdge> edges = new HashMap<>();
        Map<Integer, GlobalEdgeSet> inEdges = new HashMap<>();
        Map<Integer, GlobalEdgeSet> outEdges = new HashMap<>();
        GlobalNode n1 = new GlobalNode();
        GlobalNode n2 = new GlobalNode();
        GlobalEdge e1 = new GlobalEdge(n1, n2);
        edges.put(e1.id(), e1);
        GlobalUnmodifiableLiveEdgeSet set = new GlobalUnmodifiableLiveEdgeSet(nodes, edges, inEdges, outEdges);

        assertTrue(set.contains(e1));
        assertFalse(set.contains(new GlobalEdge(n1, n2)));
        assertFalse(set.contains(new Object()));

        assertEquals(1, set.size());
        assertFalse(set.isEmpty());
        assertTrue(set.containsAll(Collections.singletonList(e1)));

        GlobalEdge missing = new GlobalEdge(n1, n2);
        assertFalse(set.containsAll(Arrays.asList(e1, missing)));

        assertEquals(e1, set.iterator().next());

        assertNotNull(set.toArray());
        assertNotNull(set.toArray(new GlobalEdge[0]));
        assertNotNull(set.toArray(GlobalEdge[]::new));
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
        for (GlobalEdge n : edges.values()) {
            h += n.hashCode();
        }
        assertEquals(h, set.hashCode());

        GlobalUnmodifiableLiveEdgeSet sameSet = new GlobalUnmodifiableLiveEdgeSet(nodes, edges, inEdges, outEdges);
        assertTrue(set.equals(sameSet));
        assertTrue(set.equals(set));
        assertFalse(set.equals(null));
        assertFalse(set.equals("String"));

        Map<Integer, GlobalEdge> diffEdges = new HashMap<>();
        diffEdges.put(missing.id(), missing);
        GlobalUnmodifiableLiveEdgeSet diffSet = new GlobalUnmodifiableLiveEdgeSet(nodes, diffEdges, inEdges, outEdges);
        assertFalse(set.equals(diffSet));

        Map<Integer, GlobalEdge> diffEdgesSize = new HashMap<>();
        GlobalUnmodifiableLiveEdgeSet diffSetSize = new GlobalUnmodifiableLiveEdgeSet(nodes, diffEdgesSize, inEdges, outEdges);
        assertFalse(set.equals(diffSetSize));
    }

    @Test
    public void testSetTheoreticAndFilteringMethods() {
        Map<Integer, GlobalNode> nodes = new HashMap<>();
        Map<Integer, GlobalEdge> edges = new HashMap<>();
        Map<Integer, GlobalEdgeSet> inEdges = new HashMap<>();
        Map<Integer, GlobalEdgeSet> outEdges = new HashMap<>();
        GlobalNode n1 = new GlobalNode();
        GlobalNode n2 = new GlobalNode();
        GlobalNode n3 = new GlobalNode();
        GlobalEdge e1 = new GlobalEdge(n1, n2);
        e1.attributes().put("type", "A");
        e1.attributes().put("val", 1);
        GlobalEdge e2 = new GlobalEdge(n2, n3);
        e2.attributes().put("type", "B");
        e2.attributes().put("val", 2);

        edges.put(e1.id(), e1);
        edges.put(e2.id(), e2);

        GlobalUnmodifiableLiveEdgeSet set = new GlobalUnmodifiableLiveEdgeSet(nodes, edges, inEdges, outEdges);

        assertTrue(set.one().isPresent());

        EdgeSet filteredA = set.filter("type", AttributeValue.value("A"));
        assertEquals(1, filteredA.size());
        assertTrue(filteredA.contains(e1));

        EdgeSet filteredVal = set.filter("val");
        assertEquals(2, filteredVal.size());

        EdgeSet filterNull = set.filter(null, AttributeValue.value("A"));
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

        GlobalEdge e3 = new GlobalEdge(n3, n1);
        EdgeSet union = set.union(Collections.singletonList(e3));
        assertEquals(3, union.size());
        assertTrue(union.contains(e3));
    }

    @Test
    public void testTaggedWithAny() {
        Map<Integer, GlobalEdge> map = new HashMap<>();
        Map<Integer, GlobalNode> nodes = new HashMap<>();
        Map<Integer, GlobalEdgeSet> inEdges = new HashMap<>();
        Map<Integer, GlobalEdgeSet> outEdges = new HashMap<>();

        GlobalNode n1 = new GlobalNode();
        GlobalNode n2 = new GlobalNode();

        GlobalEdge e1 = new GlobalEdge(n1, n2);
        e1.tags().add("tagA");
        e1.tags().add("tagB");

        GlobalEdge e2 = new GlobalEdge(n1, n2);
        e2.tags().add("tagB");
        e2.tags().add("tagC");

        GlobalEdge e3 = new GlobalEdge(n1, n2);
        e3.tags().add("tagC");
        e3.tags().add("tagD");

        map.put(e1.id(), e1);
        map.put(e2.id(), e2);
        map.put(e3.id(), e3);

        GlobalUnmodifiableLiveEdgeSet set = new GlobalUnmodifiableLiveEdgeSet(nodes, map, inEdges, outEdges);

        EdgeSet result1 = set.taggedWithAny("tagA");
        assertEquals(1, result1.size());
        assertTrue(result1.contains(e1));

        EdgeSet result2 = set.taggedWithAny("tagB");
        assertEquals(2, result2.size());
        assertTrue(result2.contains(e1));
        assertTrue(result2.contains(e2));

        EdgeSet result3 = set.taggedWithAny("tagA", "tagC");
        assertEquals(3, result3.size());
        assertTrue(result3.contains(e1));
        assertTrue(result3.contains(e2));
        assertTrue(result3.contains(e3));

        EdgeSet result4 = set.taggedWithAny("nonexistent");
        assertEquals(0, result4.size());

        EdgeSet result5 = set.taggedWithAny((String[]) null);
        assertEquals(0, result5.size());

        EdgeSet result6 = set.taggedWithAny(new String[0]);
        assertEquals(0, result6.size());
    }

    @Test
    public void testTaggedWithAll() {
        Map<Integer, GlobalEdge> map = new HashMap<>();
        Map<Integer, GlobalNode> nodes = new HashMap<>();
        Map<Integer, GlobalEdgeSet> inEdges = new HashMap<>();
        Map<Integer, GlobalEdgeSet> outEdges = new HashMap<>();

        GlobalNode n1 = new GlobalNode();
        GlobalNode n2 = new GlobalNode();

        GlobalEdge e1 = new GlobalEdge(n1, n2);
        e1.tags().add("tagA");
        e1.tags().add("tagB");

        GlobalEdge e2 = new GlobalEdge(n1, n2);
        e2.tags().add("tagB");
        e2.tags().add("tagC");

        GlobalEdge e3 = new GlobalEdge(n1, n2);
        e3.tags().add("tagA");
        e3.tags().add("tagB");
        e3.tags().add("tagC");

        map.put(e1.id(), e1);
        map.put(e2.id(), e2);
        map.put(e3.id(), e3);

        GlobalUnmodifiableLiveEdgeSet set = new GlobalUnmodifiableLiveEdgeSet(nodes, map, inEdges, outEdges);

        EdgeSet result1 = set.taggedWithAll("tagA");
        assertEquals(2, result1.size());
        assertTrue(result1.contains(e1));
        assertTrue(result1.contains(e3));

        EdgeSet result2 = set.taggedWithAll("tagB");
        assertEquals(3, result2.size());
        assertTrue(result2.contains(e1));
        assertTrue(result2.contains(e2));
        assertTrue(result2.contains(e3));

        EdgeSet result3 = set.taggedWithAll("tagA", "tagB");
        assertEquals(2, result3.size());
        assertTrue(result3.contains(e1));
        assertTrue(result3.contains(e3));

        EdgeSet result4 = set.taggedWithAll("tagA", "tagB", "tagC");
        assertEquals(1, result4.size());
        assertTrue(result4.contains(e3));

        EdgeSet result5 = set.taggedWithAll("nonexistent");
        assertEquals(0, result5.size());

        EdgeSet result6 = set.taggedWithAll((String[]) null);
        assertEquals(0, result6.size());

        EdgeSet result7 = set.taggedWithAll(new String[0]);
        assertEquals(0, result7.size());
    }
}
