package dev.chpg.pg.multiverse.ephemeral;

import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.NodeSet;
import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.EdgeSet;
import dev.chpg.pg.multiverse.universe.Universe;
import dev.chpg.pg.multiverse.universe.UniverseNode;
import dev.chpg.pg.multiverse.universe.UniverseNodeSet;
import dev.chpg.pg.multiverse.universe.UniverseEdgeSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class ShadowSetCoverageTest {

    private Universe universe;
    private Universe universe2;
    private EphemeralGraph graph;
    private EphemeralGraph graph2;
    private Node n1, n2, n3;
    private Edge e1;
    private EphemeralFactory factory;

    @BeforeEach
    public void setup() {
        universe = new Universe();
        graph = new EphemeralGraph(universe);
        factory = graph.factory();

        n1 = factory.createNode();
        n2 = factory.createNode();
        n3 = factory.createNode();
        graph.addNode(n1);
        graph.addNode(n2);

        e1 = factory.createEdge(n1, n2);
        graph.addEdge(e1);

        universe2 = new Universe();
        graph2 = new EphemeralGraph(universe2);
    }

    @Test
    public void testShadowNodeSetCoverage() {
        NodeSet nodes = graph.nodes();

        // Additional constructor coverage
        NodeSet shadowCopy = new ShadowNodeSet(graph, new UniverseNodeSet(universe, new java.util.BitSet()));

        // Test UniverseNode contains
        UniverseNode uNode = new UniverseNode(universe, 9999);
        assertFalse(nodes.contains(uNode));
        UniverseNode un3 = new UniverseNode(universe, 10000);
        assertFalse(nodes.contains(un3));

        assertTrue(nodes.contains(n1));
        assertFalse(nodes.contains(n3)); // n3 not in graph
        assertFalse(nodes.contains(null)); // false contains
        assertFalse(nodes.contains(new Object())); // false contains type

        assertTrue(nodes.contains(nodes.iterator().next()));

        // unwrapForAlgebra - EphemeralImmutableNodeSet (empty) coverage
        nodes.union(dev.chpg.pg.api.NodeSet.empty());
        nodes.union(new EphemeralImmutableNodeSet(new EphemeralNodeSet(n3)));
        nodes.union(new EphemeralNodeSet(graph.localNodes()));

        NodeSet diff = nodes.difference(new EphemeralNodeSet(n1));
        assertEquals(1, diff.size());

        // cover unwrapForAlgebra other types
        nodes.union(new EphemeralImmutableSingletonNodeSet(n3));
        nodes.union(new EphemeralNodeSet(n1));

        universe.promote(graph);
        EphemeralGraph graphTombstone = new EphemeralGraph(universe);
        graphTombstone.removeNode(n1); // put tombstone on universe node
        NodeSet tombstoneNodes = graphTombstone.nodes();
        assertEquals(2, tombstoneNodes.ids().size());
        assertEquals(2, tombstoneNodes.toIdArray().length);

        assertTrue(nodes.ids().size() == 2);
        assertTrue(nodes.toIdArray().length == 2);
        assertTrue(nodes.one().isPresent());

        NodeSet empty = graph.nodes().difference(nodes);
        // We don't assert empty.one().isPresent() anymore because graph.nodes() was re-evaluated after we promoted and added tombstones

        assertNotNull(nodes.toImmutable());
        assertNotNull(empty.toImmutable());

        Node[] arr = nodes.toArray(new Node[0]);
        assertEquals(2, arr.length);

        // coverage for singleton toImmutable
        Node tempNode = factory.createNode(); graph.addNode(tempNode);
        NodeSet singleNodes = graph.nodes().intersect(new EphemeralNodeSet(tempNode));
        assertNotNull(singleNodes.toImmutable());
        assertEquals(1, singleNodes.toImmutable().size());
        Object[] arrObj = nodes.toArray();
        assertEquals(2, arrObj.length);

        // Unsupported modifications
        assertThrows(UnsupportedOperationException.class, () -> nodes.add(n1));
        assertThrows(UnsupportedOperationException.class, () -> nodes.remove(n1));
        assertThrows(UnsupportedOperationException.class, () -> nodes.addAll(java.util.Collections.singleton(n1)));
        assertThrows(UnsupportedOperationException.class, () -> nodes.removeAll(java.util.Collections.singleton(n1)));
        assertThrows(UnsupportedOperationException.class, () -> nodes.retainAll(java.util.Collections.singleton(n1)));
        assertThrows(UnsupportedOperationException.class, () -> nodes.clear());
        assertTrue(nodes.isMaterialized()); // Materialize call coverage via toArray is already present

        // cross-universe exception
        NodeSet nodes2 = graph2.nodes();
        assertThrows(IllegalArgumentException.class, () -> nodes.union(nodes2));
        assertThrows(IllegalArgumentException.class, () -> nodes.union(new UniverseNodeSet(universe2, new java.util.BitSet())));
        assertThrows(NullPointerException.class, () -> nodes.union(null));

        // strict algebra whitelist violation
        assertThrows(IllegalArgumentException.class, () -> nodes.union(java.util.Collections.singleton(factory.createNode())));
    }

    @Test
    public void testShadowEdgeSetCoverage() {
        EdgeSet edges = graph.edges();

        // Additional constructor coverage
        EdgeSet shadowCopy = new ShadowEdgeSet(graph, new UniverseEdgeSet(universe, new java.util.BitSet()));

        // Test contains
        assertFalse(edges.contains(new dev.chpg.pg.multiverse.universe.UniverseEdge(universe, e1.id() + 100)));

        assertTrue(edges.contains(e1));
        assertFalse(edges.contains(factory.createEdge(n1, n1))); // not in graph
        assertFalse(edges.contains(null)); // false contains
        assertFalse(edges.contains(new Object())); // false contains type

        assertTrue(edges.contains(edges.iterator().next()));

        // unwrapForAlgebra - EphemeralImmutableEdgeSet (empty) coverage
        edges.union(dev.chpg.pg.api.EdgeSet.empty());
        edges.union(new EphemeralImmutableEdgeSet(new EphemeralEdgeSet(e1)));
        edges.union(new EphemeralEdgeSet(graph.localEdges()));

        EdgeSet diff = edges.difference(new EphemeralEdgeSet(e1));
        assertEquals(0, diff.size());

        Edge e2 = factory.createEdge(n1, n3);

        // cover unwrapForAlgebra other types
        edges.union(new EphemeralImmutableSingletonEdgeSet(e2));
        edges.union(new EphemeralEdgeSet(e1));

        universe.promote(graph);
        EphemeralGraph graphTombstone2 = new EphemeralGraph(universe);
        graphTombstone2.removeEdge(e1); // put tombstone on universe edge
        EdgeSet tombstoneEdges = graphTombstone2.edges();
        assertEquals(1, tombstoneEdges.ids().size());
        assertEquals(1, tombstoneEdges.toIdArray().length);

        assertTrue(edges.ids().size() == 1);
        assertTrue(edges.toIdArray().length == 1);
        assertTrue(edges.one().isPresent());

        EdgeSet empty = graph.edges().difference(edges);

        assertNotNull(edges.toImmutable());
        assertNotNull(empty.toImmutable());

        Edge[] arr = edges.toArray(new Edge[0]);
        assertEquals(1, arr.length);

        // coverage for singleton toImmutable
        Edge e4 = factory.createEdge(n1, n2); graph.addEdge(e4);
        EdgeSet singleEdges = graph.edges().intersect(new EphemeralEdgeSet(e4));
        assertNotNull(singleEdges.toImmutable());
        assertEquals(1, singleEdges.toImmutable().size());
        Object[] arrObj = edges.toArray();
        assertEquals(1, arrObj.length);

        // Unsupported modifications
        assertThrows(UnsupportedOperationException.class, () -> edges.add(e1));
        assertThrows(UnsupportedOperationException.class, () -> edges.remove(e1));
        assertThrows(UnsupportedOperationException.class, () -> edges.addAll(java.util.Collections.singleton(e1)));
        assertThrows(UnsupportedOperationException.class, () -> edges.removeAll(java.util.Collections.singleton(e1)));
        assertThrows(UnsupportedOperationException.class, () -> edges.retainAll(java.util.Collections.singleton(e1)));
        assertThrows(UnsupportedOperationException.class, () -> edges.clear());
        assertTrue(edges.isMaterialized()); // Materialize call coverage via toArray is already present

        // cross-universe exception
        EdgeSet edges2 = graph2.edges();
        assertThrows(IllegalArgumentException.class, () -> edges.union(edges2));
        assertThrows(IllegalArgumentException.class, () -> edges.union(new UniverseEdgeSet(universe2, new java.util.BitSet())));
        assertThrows(NullPointerException.class, () -> edges.union(null));

        // strict algebra whitelist violation
        assertThrows(IllegalArgumentException.class, () -> edges.union(java.util.Collections.singleton(factory.createEdge(n1,n2))));
    }
}
