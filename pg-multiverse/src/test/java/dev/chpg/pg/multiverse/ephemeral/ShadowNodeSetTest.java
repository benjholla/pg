package dev.chpg.pg.multiverse.ephemeral;

import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.NodeSet;
import dev.chpg.pg.multiverse.universe.Universe;
import dev.chpg.pg.multiverse.universe.UniverseNode;
import dev.chpg.pg.multiverse.universe.UniverseNodeSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.BitSet;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ShadowNodeSetTest {

    private Universe universe;
    private EphemeralGraph graph;
    private UniverseNode uNode1;
    private UniverseNode uNode2;
    private EphemeralNode eNode1;

    @BeforeEach
    public void setup() {
        universe = new Universe();
        EphemeralGraph initialGraph = new EphemeralGraph(universe);

        EphemeralNode n1 = (EphemeralNode) initialGraph.factory().createNode();
        EphemeralNode n2 = (EphemeralNode) initialGraph.factory().createNode();
        initialGraph.addNode(n1);
        initialGraph.addNode(n2);

        universe.promote(initialGraph);

        Iterator<Node> iter = universe.asGraph().nodes().iterator();
        uNode1 = (UniverseNode) iter.next();
        uNode2 = (UniverseNode) iter.next();

        graph = new EphemeralGraph(universe);

        eNode1 = (EphemeralNode) graph.factory().createNode();
        graph.addNode(eNode1);
    }

    @Test
    public void testConstructorsAndUnwrapForAlgebra() {
        NodeSet universeNodes = universe.asGraph().nodes();
        ShadowNodeSet shadowSet = new ShadowNodeSet(graph, universeNodes);
        assertEquals(2, shadowSet.size());

        Set<Node> localAdds = new HashSet<>();
        localAdds.add(eNode1);
        ShadowNodeSet compositeSet = new ShadowNodeSet(graph, universeNodes, localAdds);
        assertEquals(3, compositeSet.size());

        Universe universe2 = new Universe();
        EphemeralGraph graph2 = new EphemeralGraph(universe2);
        ShadowNodeSet shadowSet2 = new ShadowNodeSet(graph2, universe2.asGraph().nodes());

        // Test cross-universe contamination
        assertThrows(IllegalArgumentException.class, () -> shadowSet.union(shadowSet2));
        assertThrows(IllegalArgumentException.class, () -> shadowSet.union(new UniverseNodeSet(universe2, new BitSet())));
        assertThrows(NullPointerException.class, () -> shadowSet.union(null));
        NodeSet ephemSet = new EphemeralNodeSet(Collections.emptyList());
        shadowSet.union(ephemSet);

        // Empty set fallback
        NodeSet emptyResult = shadowSet.union(Collections.emptySet());
        assertEquals(2, emptyResult.size());

        // Foreign object rejection
        assertThrows(IllegalArgumentException.class, () -> shadowSet.union(Set.of((Node) eNode1)));
    }

    @Test
    public void testUnion() {
        ShadowNodeSet set1 = new ShadowNodeSet(graph, new UniverseNodeSet(universe, bitSet(uNode1.id())));
        ShadowNodeSet set2 = new ShadowNodeSet(graph, new UniverseNodeSet(universe, bitSet(uNode2.id())), new HashSet<>(Collections.singleton(eNode1)));

        NodeSet unionSet = set1.union(set2);
        assertEquals(3, unionSet.size());
        assertTrue(unionSet.contains(uNode1));
        assertTrue(unionSet.contains(uNode2));
        assertTrue(unionSet.contains(eNode1));
    }

    @Test
    public void testDifference() {
        ShadowNodeSet set1 = new ShadowNodeSet(graph, universe.asGraph().nodes(), new HashSet<>(Collections.singleton(eNode1)));
        ShadowNodeSet set2 = new ShadowNodeSet(graph, new UniverseNodeSet(universe, bitSet(uNode2.id())), new HashSet<>(Collections.singleton(eNode1)));

        NodeSet diffSet = set1.difference(set2);
        assertEquals(1, diffSet.size());
        assertTrue(diffSet.contains(uNode1));
        assertFalse(diffSet.contains(uNode2));
        assertFalse(diffSet.contains(eNode1));
    }

    @Test
    public void testIntersect() {
        ShadowNodeSet set1 = new ShadowNodeSet(graph, universe.asGraph().nodes(), new HashSet<>(Collections.singleton(eNode1)));
        ShadowNodeSet set2 = new ShadowNodeSet(graph, new UniverseNodeSet(universe, bitSet(uNode2.id())), new HashSet<>(Collections.singleton(eNode1)));

        NodeSet intersectSet = set1.intersect(set2);
        assertEquals(2, intersectSet.size());
        assertFalse(intersectSet.contains(uNode1));
        assertTrue(intersectSet.contains(uNode2));
        assertTrue(intersectSet.contains(eNode1));
    }

    @Test
    public void testIteratorAndTombstones() {
        ShadowNodeSet set = new ShadowNodeSet(graph, universe.asGraph().nodes(), new HashSet<>(Collections.singleton(eNode1)));

        graph.removeNode(uNode1); // Add tombstone

        Set<Node> iterated = new HashSet<>();
        Iterator<Node> iter = set.iterator();
        while (iter.hasNext()) {
            iterated.add(iter.next());
        }

        assertEquals(2, iterated.size());
        assertTrue(iterated.contains(eNode1));
        assertTrue(iterated.stream().anyMatch(n -> n.id() == uNode2.id()));

        assertThrows(NoSuchElementException.class, iter::next);
    }

    @Test
    public void testSize() {
        ShadowNodeSet set = new ShadowNodeSet(graph, universe.asGraph().nodes(), new HashSet<>(Collections.singleton(eNode1)));
        assertEquals(3, set.size());

        graph.removeNode(uNode1);
        assertEquals(2, set.size());
    }

    @Test
    public void testContainsAndContainsAll() {
        ShadowNodeSet set = new ShadowNodeSet(graph, universe.asGraph().nodes(), new HashSet<>(Collections.singleton(eNode1)));

        assertTrue(set.contains(uNode1));
        assertTrue(set.contains(uNode2));
        assertTrue(set.contains(eNode1));
        assertFalse(set.contains(null));
        assertFalse(set.contains(new Object()));

        assertTrue(set.containsAll(Arrays.asList(uNode1, eNode1)));

        graph.removeNode(uNode1);
        assertFalse(set.contains(uNode1));

        ShadowNode sNode = new ShadowNode(graph, uNode2);
        assertTrue(set.contains(sNode));
    }

    @Test
    public void testIsEmptyMaterializedSizeKnown() {
        ShadowNodeSet set = new ShadowNodeSet(graph, universe.asGraph().nodes());
        assertFalse(set.isEmpty());
        assertTrue(set.isMaterialized());
        assertTrue(set.isSizeKnown());

        ShadowNodeSet emptySet = new ShadowNodeSet(graph, new UniverseNodeSet(universe, new BitSet()));
        assertTrue(emptySet.isEmpty());
    }

    @Test
    public void testIdsAndToIdArray() {
        ShadowNodeSet set = new ShadowNodeSet(graph, universe.asGraph().nodes(), new HashSet<>(Collections.singleton(eNode1)));

        Set<Integer> ids = set.ids();
        assertEquals(3, ids.size());
        assertTrue(ids.contains(uNode1.id()));
        assertTrue(ids.contains(uNode2.id()));
        assertTrue(ids.contains(eNode1.id()));

        int[] idArray = set.toIdArray();
        assertEquals(3, idArray.length);
    }

    @Test
    public void testOneAndToImmutable() {
        ShadowNodeSet set = new ShadowNodeSet(graph, universe.asGraph().nodes());
        Optional<Node> one = set.one();
        assertTrue(one.isPresent());

        NodeSet immutable = set.toImmutable();
        assertEquals(2, immutable.size());

        ShadowNodeSet emptySet = new ShadowNodeSet(graph, new UniverseNodeSet(universe, new BitSet()));
        assertFalse(emptySet.one().isPresent());
        assertTrue(emptySet.toImmutable().isEmpty());

        ShadowNodeSet singletonSet = new ShadowNodeSet(graph, new UniverseNodeSet(universe, bitSet(uNode1.id())));
        assertEquals(1, singletonSet.toImmutable().size());
    }

    @Test
    public void testUnsupportedMutators() {
        ShadowNodeSet set = new ShadowNodeSet(graph, universe.asGraph().nodes());

        assertThrows(UnsupportedOperationException.class, () -> set.add(eNode1));
        assertThrows(UnsupportedOperationException.class, () -> set.remove(uNode1));
        assertThrows(UnsupportedOperationException.class, () -> set.addAll(Collections.singleton(eNode1)));
        assertThrows(UnsupportedOperationException.class, () -> set.removeAll(Collections.singleton(uNode1)));
        assertThrows(UnsupportedOperationException.class, () -> set.retainAll(Collections.singleton(uNode2)));
        assertThrows(UnsupportedOperationException.class, set::clear);
    }

    @Test
    public void testToArray() {
        ShadowNodeSet set = new ShadowNodeSet(graph, universe.asGraph().nodes(), new HashSet<>(Collections.singleton(eNode1)));
        Object[] arr = set.toArray();
        assertEquals(3, arr.length);

        Node[] typedArr = set.toArray(new Node[0]);
        assertEquals(3, typedArr.length);
    }

    private BitSet bitSet(int... bits) {
        BitSet bs = new BitSet();
        for (int b : bits) bs.set(b);
        return bs;
    }
}
