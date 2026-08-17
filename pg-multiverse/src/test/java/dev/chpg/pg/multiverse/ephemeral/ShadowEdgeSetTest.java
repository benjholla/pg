package dev.chpg.pg.multiverse.ephemeral;

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.EdgeSet;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.multiverse.universe.Universe;
import dev.chpg.pg.multiverse.universe.UniverseEdge;
import dev.chpg.pg.multiverse.universe.UniverseEdgeSet;
import dev.chpg.pg.multiverse.universe.UniverseNode;
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

public class ShadowEdgeSetTest {

    private Universe universe;
    private EphemeralGraph graph;
    private UniverseEdge uEdge1;
    private UniverseEdge uEdge2;
    private EphemeralEdge eEdge1;

    @BeforeEach
    public void setup() {
        universe = new Universe();
        EphemeralGraph initialGraph = new EphemeralGraph(universe);

        EphemeralNode n1 = (EphemeralNode) initialGraph.factory().createNode();
        EphemeralNode n2 = (EphemeralNode) initialGraph.factory().createNode();
        initialGraph.addNode(n1);
        initialGraph.addNode(n2);

        EphemeralEdge e1 = (EphemeralEdge) initialGraph.factory().createEdge(n1, n2);
        EphemeralEdge e2 = (EphemeralEdge) initialGraph.factory().createEdge(n2, n1);
        initialGraph.addEdge(e1);
        initialGraph.addEdge(e2);

        universe.promote(initialGraph);

        Iterator<Edge> iter = universe.asGraph().edges().iterator();
        uEdge1 = (UniverseEdge) iter.next();
        uEdge2 = (UniverseEdge) iter.next();

        graph = new EphemeralGraph(universe);

        EphemeralNode eNode1 = (EphemeralNode) graph.factory().createNode();
        EphemeralNode eNode2 = (EphemeralNode) graph.factory().createNode();
        graph.addNode(eNode1);
        graph.addNode(eNode2);

        eEdge1 = (EphemeralEdge) graph.factory().createEdge(eNode1, eNode2);
        graph.addEdge(eEdge1);
    }

    @Test
    public void testConstructorsAndUnwrapForAlgebra() {
        EdgeSet universeEdges = universe.asGraph().edges();
        ShadowEdgeSet shadowSet = new ShadowEdgeSet(graph, universeEdges);
        assertEquals(2, shadowSet.size());

        Set<Edge> localAdds = new HashSet<>();
        localAdds.add(eEdge1);
        ShadowEdgeSet compositeSet = new ShadowEdgeSet(graph, universeEdges, localAdds);
        assertEquals(3, compositeSet.size());

        Universe universe2 = new Universe();
        EphemeralGraph graph2 = new EphemeralGraph(universe2);
        ShadowEdgeSet shadowSet2 = new ShadowEdgeSet(graph2, universe2.asGraph().edges());

        // Test cross-universe contamination
        assertThrows(IllegalArgumentException.class, () -> shadowSet.union(shadowSet2));
        assertThrows(IllegalArgumentException.class, () -> shadowSet.union(new UniverseEdgeSet(universe2, new BitSet())));
        assertThrows(NullPointerException.class, () -> shadowSet.union(null));
        EdgeSet ephemSet = new EphemeralEdgeSet(Collections.emptyList());
        shadowSet.union(ephemSet);

        // Empty set fallback
        EdgeSet emptyResult = shadowSet.union(Collections.emptySet());
        assertEquals(2, emptyResult.size());

        // Foreign object rejection
        assertThrows(IllegalArgumentException.class, () -> shadowSet.union(Set.of((Edge) eEdge1)));
    }

    @Test
    public void testUnion() {
        ShadowEdgeSet set1 = new ShadowEdgeSet(graph, new UniverseEdgeSet(universe, bitSet(uEdge1.id())));
        ShadowEdgeSet set2 = new ShadowEdgeSet(graph, new UniverseEdgeSet(universe, bitSet(uEdge2.id())), new HashSet<>(Collections.singleton(eEdge1)));

        EdgeSet unionSet = set1.union(set2);
        assertEquals(3, unionSet.size());
        assertTrue(unionSet.contains(uEdge1));
        assertTrue(unionSet.contains(uEdge2));
        assertTrue(unionSet.contains(eEdge1));
    }

    @Test
    public void testDifference() {
        ShadowEdgeSet set1 = new ShadowEdgeSet(graph, universe.asGraph().edges(), new HashSet<>(Collections.singleton(eEdge1)));
        ShadowEdgeSet set2 = new ShadowEdgeSet(graph, new UniverseEdgeSet(universe, bitSet(uEdge2.id())), new HashSet<>(Collections.singleton(eEdge1)));

        EdgeSet diffSet = set1.difference(set2);
        assertEquals(1, diffSet.size());
        assertTrue(diffSet.contains(uEdge1));
        assertFalse(diffSet.contains(uEdge2));
        assertFalse(diffSet.contains(eEdge1));
    }

    @Test
    public void testIntersect() {
        ShadowEdgeSet set1 = new ShadowEdgeSet(graph, universe.asGraph().edges(), new HashSet<>(Collections.singleton(eEdge1)));
        ShadowEdgeSet set2 = new ShadowEdgeSet(graph, new UniverseEdgeSet(universe, bitSet(uEdge2.id())), new HashSet<>(Collections.singleton(eEdge1)));

        EdgeSet intersectSet = set1.intersect(set2);
        assertEquals(2, intersectSet.size());
        assertFalse(intersectSet.contains(uEdge1));
        assertTrue(intersectSet.contains(uEdge2));
        assertTrue(intersectSet.contains(eEdge1));
    }

    @Test
    public void testIteratorAndTombstones() {
        ShadowEdgeSet set = new ShadowEdgeSet(graph, universe.asGraph().edges(), new HashSet<>(Collections.singleton(eEdge1)));

        graph.removeEdge(uEdge1); // Add tombstone

        Set<Edge> iterated = new HashSet<>();
        Iterator<Edge> iter = set.iterator();
        while (iter.hasNext()) {
            iterated.add(iter.next());
        }

        assertEquals(2, iterated.size());
        assertTrue(iterated.contains(eEdge1));
        assertTrue(iterated.stream().anyMatch(e -> e.id() == uEdge2.id()));

        assertThrows(NoSuchElementException.class, iter::next);
    }

    @Test
    public void testSize() {
        ShadowEdgeSet set = new ShadowEdgeSet(graph, universe.asGraph().edges(), new HashSet<>(Collections.singleton(eEdge1)));
        assertEquals(3, set.size());

        graph.removeEdge(uEdge1);
        assertEquals(2, set.size());
    }

    @Test
    public void testContainsAndContainsAll() {
        ShadowEdgeSet set = new ShadowEdgeSet(graph, universe.asGraph().edges(), new HashSet<>(Collections.singleton(eEdge1)));

        assertTrue(set.contains(uEdge1));
        assertTrue(set.contains(uEdge2));
        assertTrue(set.contains(eEdge1));
        assertFalse(set.contains(null));
        assertFalse(set.contains(new Object()));

        assertTrue(set.containsAll(Arrays.asList(uEdge1, eEdge1)));

        graph.removeEdge(uEdge1);
        assertFalse(set.contains(uEdge1));

        ShadowEdge sEdge = new ShadowEdge(graph, uEdge2);
        assertTrue(set.contains(sEdge));
    }

    @Test
    public void testIsEmptyMaterializedSizeKnown() {
        ShadowEdgeSet set = new ShadowEdgeSet(graph, universe.asGraph().edges());
        assertFalse(set.isEmpty());
        assertTrue(set.isMaterialized());
        assertTrue(set.isSizeKnown());

        ShadowEdgeSet emptySet = new ShadowEdgeSet(graph, new UniverseEdgeSet(universe, new BitSet()));
        assertTrue(emptySet.isEmpty());
    }

    @Test
    public void testIdsAndToIdArray() {
        ShadowEdgeSet set = new ShadowEdgeSet(graph, universe.asGraph().edges(), new HashSet<>(Collections.singleton(eEdge1)));

        Set<Integer> ids = set.ids();
        assertEquals(3, ids.size());
        assertTrue(ids.contains(uEdge1.id()));
        assertTrue(ids.contains(uEdge2.id()));
        assertTrue(ids.contains(eEdge1.id()));

        int[] idArray = set.toIdArray();
        assertEquals(3, idArray.length);
    }

    @Test
    public void testOneAndToImmutable() {
        ShadowEdgeSet set = new ShadowEdgeSet(graph, universe.asGraph().edges());
        Optional<Edge> one = set.one();
        assertTrue(one.isPresent());

        EdgeSet immutable = set.toImmutable();
        assertEquals(2, immutable.size());

        ShadowEdgeSet emptySet = new ShadowEdgeSet(graph, new UniverseEdgeSet(universe, new BitSet()));
        assertFalse(emptySet.one().isPresent());
        assertTrue(emptySet.toImmutable().isEmpty());

        ShadowEdgeSet singletonSet = new ShadowEdgeSet(graph, new UniverseEdgeSet(universe, bitSet(uEdge1.id())));
        assertEquals(1, singletonSet.toImmutable().size());
    }

    @Test
    public void testUnsupportedMutators() {
        ShadowEdgeSet set = new ShadowEdgeSet(graph, universe.asGraph().edges());

        assertThrows(UnsupportedOperationException.class, () -> set.add(eEdge1));
        assertThrows(UnsupportedOperationException.class, () -> set.remove(uEdge1));
        assertThrows(UnsupportedOperationException.class, () -> set.addAll(Collections.singleton(eEdge1)));
        assertThrows(UnsupportedOperationException.class, () -> set.removeAll(Collections.singleton(uEdge1)));
        assertThrows(UnsupportedOperationException.class, () -> set.retainAll(Collections.singleton(uEdge2)));
        assertThrows(UnsupportedOperationException.class, set::clear);
    }

    @Test
    public void testToArray() {
        ShadowEdgeSet set = new ShadowEdgeSet(graph, universe.asGraph().edges(), new HashSet<>(Collections.singleton(eEdge1)));
        Object[] arr = set.toArray();
        assertEquals(3, arr.length);

        Edge[] typedArr = set.toArray(new Edge[0]);
        assertEquals(3, typedArr.length);
    }

    private BitSet bitSet(int... bits) {
        BitSet bs = new BitSet();
        for (int b : bits) bs.set(b);
        return bs;
    }
}
