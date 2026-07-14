package dev.chpg.pg.multiverse.ephemeral;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import dev.chpg.pg.api.NodeSet;
import dev.chpg.pg.api.AttributeValue;

public class EphemeralUnmodifiableLiveNodeSetTest {

    @Test
    public void testUnsupportedOperations() {
        EphemeralGraph graph = new EphemeralGraph();
        Map<Integer, EphemeralNode> map = new HashMap<>();
        Map<Integer, EphemeralEdge> edges = new HashMap<>();
        Map<Integer, EphemeralEdgeSet> inEdges = new HashMap<>();
        Map<Integer, EphemeralEdgeSet> outEdges = new HashMap<>();
        EphemeralUnmodifiableLiveNodeSet set = new EphemeralUnmodifiableLiveNodeSet(map, edges, inEdges, outEdges);
        EphemeralNode n = graph.createNode();

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
        EphemeralGraph graph = new EphemeralGraph();
        Map<Integer, EphemeralNode> map = new HashMap<>();
        Map<Integer, EphemeralEdge> edges = new HashMap<>();
        Map<Integer, EphemeralEdgeSet> inEdges = new HashMap<>();
        Map<Integer, EphemeralEdgeSet> outEdges = new HashMap<>();
        EphemeralNode n1 = graph.createNode();
        map.put(n1.id(), n1);
        EphemeralUnmodifiableLiveNodeSet set = new EphemeralUnmodifiableLiveNodeSet(map, edges, inEdges, outEdges);

        assertTrue(set.contains(n1));
        assertFalse(set.contains(graph.createNode()));
        assertFalse(set.contains(new Object()));

        assertEquals(1, set.size());
        assertFalse(set.isEmpty());
        assertTrue(set.containsAll(Collections.singletonList(n1)));

        EphemeralNode missing = graph.createNode();
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
        EphemeralGraph graph = new EphemeralGraph();
        Map<Integer, EphemeralNode> map = new HashMap<>();
        Map<Integer, EphemeralEdge> edges = new HashMap<>();
        Map<Integer, EphemeralEdgeSet> inEdges = new HashMap<>();
        Map<Integer, EphemeralEdgeSet> outEdges = new HashMap<>();
        EphemeralNode n1 = graph.createNode();
        n1.attributes().put("type", "A");
        n1.attributes().put("val", 1);
        EphemeralNode n2 = graph.createNode();
        n2.attributes().put("type", "B");
        n2.attributes().put("val", 2);

        map.put(n1.id(), n1);
        map.put(n2.id(), n2);

        EphemeralUnmodifiableLiveNodeSet set = new EphemeralUnmodifiableLiveNodeSet(map, edges, inEdges, outEdges);

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

        EphemeralNode n3 = graph.createNode();
        NodeSet union = set.union(Collections.singletonList(n3));
        assertEquals(3, union.size());
        assertTrue(union.contains(n3));
    }

    @Test
    public void testwithAnyTag() {
        Map<Integer, EphemeralNode> map = new HashMap<>();
        Map<Integer, EphemeralEdge> edges = new HashMap<>();
        Map<Integer, EphemeralEdgeSet> inEdges = new HashMap<>();
        Map<Integer, EphemeralEdgeSet> outEdges = new HashMap<>();

        EphemeralNode n1 = new EphemeralNode(-1);
        n1.tags().add("tagA");
        n1.tags().add("tagB");

        EphemeralNode n2 = new EphemeralNode(-2);
        n2.tags().add("tagB");
        n2.tags().add("tagC");

        EphemeralNode n3 = new EphemeralNode(-3);
        n3.tags().add("tagC");
        n3.tags().add("tagD");

        map.put(n1.id(), n1);
        map.put(n2.id(), n2);
        map.put(n3.id(), n3);

        EphemeralUnmodifiableLiveNodeSet set = new EphemeralUnmodifiableLiveNodeSet(map, edges, inEdges, outEdges);

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
        Map<Integer, EphemeralNode> map = new HashMap<>();
        Map<Integer, EphemeralEdge> edges = new HashMap<>();
        Map<Integer, EphemeralEdgeSet> inEdges = new HashMap<>();
        Map<Integer, EphemeralEdgeSet> outEdges = new HashMap<>();

        EphemeralNode n1 = new EphemeralNode(-1);
        n1.tags().add("tagA");
        n1.tags().add("tagB");

        EphemeralNode n2 = new EphemeralNode(-2);
        n2.tags().add("tagB");
        n2.tags().add("tagC");

        EphemeralNode n3 = new EphemeralNode(-3);
        n3.tags().add("tagA");
        n3.tags().add("tagB");
        n3.tags().add("tagC");

        map.put(n1.id(), n1);
        map.put(n2.id(), n2);
        map.put(n3.id(), n3);

        EphemeralUnmodifiableLiveNodeSet set = new EphemeralUnmodifiableLiveNodeSet(map, edges, inEdges, outEdges);

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


    @Test
    public void testToImmutable() {
        Map<Integer, dev.chpg.pg.multiverse.ephemeral.EphemeralNode> map = new HashMap<>();
        Map<Integer, dev.chpg.pg.multiverse.ephemeral.EphemeralEdge> edges = new HashMap<>();
        Map<Integer, dev.chpg.pg.multiverse.ephemeral.EphemeralEdgeSet> inEdges = new HashMap<>();
        Map<Integer, dev.chpg.pg.multiverse.ephemeral.EphemeralEdgeSet> outEdges = new HashMap<>();
        dev.chpg.pg.multiverse.ephemeral.EphemeralUnmodifiableLiveNodeSet set = new dev.chpg.pg.multiverse.ephemeral.EphemeralUnmodifiableLiveNodeSet(map, edges, inEdges, outEdges);

        // Empty
        NodeSet immutableEmpty = set.toImmutable();
        assertTrue(immutableEmpty.isEmpty());
        assertTrue(immutableEmpty.isMaterialized());

        // Singleton
        dev.chpg.pg.multiverse.ephemeral.EphemeralGraph g = new dev.chpg.pg.multiverse.ephemeral.EphemeralGraph();
        dev.chpg.pg.multiverse.ephemeral.EphemeralNode n1 = (dev.chpg.pg.multiverse.ephemeral.EphemeralNode) g.factory().createNode();
        map.put(n1.id(), n1);
        NodeSet immutableSingleton = set.toImmutable();
        assertEquals(1, immutableSingleton.size());
        assertTrue(immutableSingleton.contains(n1));
        assertTrue(immutableSingleton.isMaterialized());

        // Multiple
        dev.chpg.pg.multiverse.ephemeral.EphemeralNode n2 = (dev.chpg.pg.multiverse.ephemeral.EphemeralNode) g.factory().createNode();
        map.put(n2.id(), n2);
        NodeSet immutableMultiple = set.toImmutable();
        assertEquals(2, immutableMultiple.size());
        assertTrue(immutableMultiple.contains(n1));
        assertTrue(immutableMultiple.contains(n2));
        assertTrue(immutableMultiple.isMaterialized());
    }

}
