package dev.chpg.pg.multiverse.ephemeral;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;

public class GraphBulkOperationsTest {

    private static final EphemeralFactory factory = new EphemeralGraph().factory();
    private Graph graph;
    private Node n1, n2, n3, n4;
    private Edge e1, e2, e3;

    @BeforeEach
    public void setUp() {
        graph = factory.createGraph();
        n1 = factory.createNode();
        n2 = factory.createNode();
        n3 = factory.createNode();
        n4 = factory.createNode();

        e1 = factory.createEdge(n1, n2);
        e2 = factory.createEdge(n2, n3);
        e3 = factory.createEdge(n3, n4);
    }

    @Test
    public void testAddAllNodes() {
        assertTrue(graph.nodes().isEmpty());
        boolean changed = graph.addAllNodes(Arrays.asList(n1, n2, n3));
        assertTrue(changed);
        assertEquals(3, graph.nodes().size());
        assertTrue(graph.nodes().contains(n1));
        assertTrue(graph.nodes().contains(n2));
        assertTrue(graph.nodes().contains(n3));

        // Adding already existing nodes should return false
        changed = graph.addAllNodes(Arrays.asList(n1, n2));
        assertFalse(changed);
        assertEquals(3, graph.nodes().size());
    }

    @Test
    public void testAddAllEdges() {
        assertTrue(graph.nodes().isEmpty());
        boolean changed = graph.addAllEdges(Arrays.asList(e1, e2));
        assertTrue(changed);
        assertEquals(3, graph.nodes().size()); // n1, n2, n3 implicitly added
        assertEquals(2, graph.edges().size());
        assertTrue(graph.edges().contains(e1));
        assertTrue(graph.edges().contains(e2));

        // Adding already existing edges should return false
        changed = graph.addAllEdges(Arrays.asList(e1));
        assertFalse(changed);
        assertEquals(2, graph.edges().size());
    }

    @Test
    public void testRemoveAllNodesSelf() {
        graph.addAllEdges(Arrays.asList(e1, e2, e3));
        assertEquals(4, graph.nodes().size());
        assertEquals(3, graph.edges().size());

        boolean changed = graph.removeAllNodes(graph.nodes());
        assertTrue(changed);
        assertEquals(0, graph.nodes().size());
    }

    @Test
    public void testRemoveAllEdgesSelf() {
        graph.addAllEdges(Arrays.asList(e1, e2, e3));
        assertEquals(4, graph.nodes().size());
        assertEquals(3, graph.edges().size());

        boolean changed = graph.removeAllEdges(graph.edges());
        assertTrue(changed);
        assertEquals(0, graph.edges().size());
    }

    public void testRemoveAllNodes() {
        graph.addAllEdges(Arrays.asList(e1, e2, e3));
        assertEquals(4, graph.nodes().size());
        assertEquals(3, graph.edges().size());

        boolean changed = graph.removeAllNodes(Arrays.asList(n2, n3));
        assertTrue(changed);

        assertEquals(2, graph.nodes().size());
        assertTrue(graph.nodes().contains(n1));
        assertTrue(graph.nodes().contains(n4));

        // Removing n2 and n3 should remove all incident edges (e1, e2, e3)
        assertEquals(0, graph.edges().size());

        // Removing already non-existent nodes should return false
        changed = graph.removeAllNodes(Arrays.asList(n2, n3));
        assertFalse(changed);
    }

    @Test
    public void testRemoveAllEdges() {
        graph.addAllEdges(Arrays.asList(e1, e2, e3));
        assertEquals(4, graph.nodes().size());
        assertEquals(3, graph.edges().size());

        boolean changed = graph.removeAllEdges(Arrays.asList(e1, e3));
        assertTrue(changed);

        assertEquals(4, graph.nodes().size()); // Nodes should remain
        assertEquals(1, graph.edges().size());
        assertTrue(graph.edges().contains(e2));
        assertFalse(graph.edges().contains(e1));
        assertFalse(graph.edges().contains(e3));

        // Removing already non-existent edges should return false
        changed = graph.removeAllEdges(Arrays.asList(e1, e3));
        assertFalse(changed);
    }
}
