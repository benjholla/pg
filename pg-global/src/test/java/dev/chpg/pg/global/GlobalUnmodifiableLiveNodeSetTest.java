package dev.chpg.pg.global;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import dev.chpg.pg.api.NodeSet;
import dev.chpg.pg.api.AttributeValue;

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

        NodeSet filteredA = set.withAttribute("type", AttributeValue.value("A"));
        assertEquals(1, filteredA.size());
        assertTrue(filteredA.contains(n1));

        NodeSet filteredVal = set.withAttribute("val");
        assertEquals(2, filteredVal.size());

        NodeSet filterNull = set.withAttribute(null, AttributeValue.value("A"));
        assertEquals(0, filterNull.size());

        assertThrows(NullPointerException.class, () -> set.intersect(null));

        NodeSet intersect = set.intersect(Collections.singletonList(n1));
        assertEquals(1, intersect.size());
        assertTrue(intersect.contains(n1));

        assertThrows(NullPointerException.class, () -> set.difference(null));

        NodeSet diff = set.difference(Collections.singletonList(n1));
        assertEquals(1, diff.size());
        assertTrue(diff.contains(n2));

        assertThrows(NullPointerException.class, () -> set.union(null));

        GlobalNode n3 = new GlobalNode();
        NodeSet union = set.union(Collections.singletonList(n3));
        assertEquals(3, union.size());
        assertTrue(union.contains(n3));
    }

    @Test
    public void testwithAnyTag() {
        Map<Integer, GlobalNode> map = new HashMap<>();
        Map<Integer, GlobalEdge> edges = new HashMap<>();
        Map<Integer, GlobalEdgeSet> inEdges = new HashMap<>();
        Map<Integer, GlobalEdgeSet> outEdges = new HashMap<>();

        GlobalNode n1 = new GlobalNode();
        n1.tags().add("tagA");
        n1.tags().add("tagB");

        GlobalNode n2 = new GlobalNode();
        n2.tags().add("tagB");
        n2.tags().add("tagC");

        GlobalNode n3 = new GlobalNode();
        n3.tags().add("tagC");
        n3.tags().add("tagD");

        map.put(n1.id(), n1);
        map.put(n2.id(), n2);
        map.put(n3.id(), n3);

        GlobalUnmodifiableLiveNodeSet set = new GlobalUnmodifiableLiveNodeSet(map, edges, inEdges, outEdges);

        NodeSet result1 = set.withAnyTag("tagA");
        assertEquals(1, result1.size());
        assertTrue(result1.contains(n1));

        NodeSet result2 = set.withAnyTag("tagB");
        assertEquals(2, result2.size());
        assertTrue(result2.contains(n1));
        assertTrue(result2.contains(n2));

        NodeSet result3 = set.withAnyTag("tagA", "tagC");
        assertEquals(3, result3.size());
        assertTrue(result3.contains(n1));
        assertTrue(result3.contains(n2));
        assertTrue(result3.contains(n3));

        NodeSet result4 = set.withAnyTag("nonexistent");
        assertEquals(0, result4.size());

        NodeSet result5 = set.withAnyTag((String[]) null);
        assertEquals(0, result5.size());

        NodeSet result6 = set.withAnyTag(new String[0]);
        assertEquals(0, result6.size());
    }

    @Test
    public void testwithAllTags() {
        Map<Integer, GlobalNode> map = new HashMap<>();
        Map<Integer, GlobalEdge> edges = new HashMap<>();
        Map<Integer, GlobalEdgeSet> inEdges = new HashMap<>();
        Map<Integer, GlobalEdgeSet> outEdges = new HashMap<>();

        GlobalNode n1 = new GlobalNode();
        n1.tags().add("tagA");
        n1.tags().add("tagB");

        GlobalNode n2 = new GlobalNode();
        n2.tags().add("tagB");
        n2.tags().add("tagC");

        GlobalNode n3 = new GlobalNode();
        n3.tags().add("tagA");
        n3.tags().add("tagB");
        n3.tags().add("tagC");

        map.put(n1.id(), n1);
        map.put(n2.id(), n2);
        map.put(n3.id(), n3);

        GlobalUnmodifiableLiveNodeSet set = new GlobalUnmodifiableLiveNodeSet(map, edges, inEdges, outEdges);

        NodeSet result1 = set.withAllTags("tagA");
        assertEquals(2, result1.size());
        assertTrue(result1.contains(n1));
        assertTrue(result1.contains(n3));

        NodeSet result2 = set.withAllTags("tagB");
        assertEquals(3, result2.size());
        assertTrue(result2.contains(n1));
        assertTrue(result2.contains(n2));
        assertTrue(result2.contains(n3));

        NodeSet result3 = set.withAllTags("tagA", "tagB");
        assertEquals(2, result3.size());
        assertTrue(result3.contains(n1));
        assertTrue(result3.contains(n3));

        NodeSet result4 = set.withAllTags("tagA", "tagB", "tagC");
        assertEquals(1, result4.size());
        assertTrue(result4.contains(n3));

        NodeSet result5 = set.withAllTags("nonexistent");
        assertEquals(0, result5.size());

        NodeSet result6 = set.withAllTags((String[]) null);
        assertEquals(0, result6.size());

        NodeSet result7 = set.withAllTags(new String[0]);
        assertEquals(0, result7.size());
    }
}
