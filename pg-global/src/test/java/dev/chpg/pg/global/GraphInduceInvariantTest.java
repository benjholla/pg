package dev.chpg.pg.global;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;

/**
 * Validates invariant properties of the induce operations.
 */
public class GraphInduceInvariantTest {

    private GlobalGraph graph;
    private Node a, b, c;
    private Edge ab, bc, ca;

    @BeforeEach
    public void setUp() {
        graph = new GlobalGraph();
        a = new GlobalNode();
        b = new GlobalNode();
        c = new GlobalNode();

        ab = new GlobalEdge(a, b);
        bc = new GlobalEdge(b, c);
        ca = new GlobalEdge(c, a);

        graph.addNode(a);
        graph.addNode(b);
    }

    @Test
    public void testInduceEdgesWithValidNodes() {
        // 'graph' contains only 'a' and 'b'.
        Graph induced = graph.induce(new GlobalEdgeSet(ab, bc, ca));

        // Should only contain edges where both nodes are in the graph.
        assertEquals(2, induced.nodes().size(), "Inducing should retain the graph's nodes");
        assertTrue(induced.nodes().contains(a));
        assertTrue(induced.nodes().contains(b));

        assertEquals(1, induced.edges().size(), "Only 'ab' should be induced since 'c' is not in the graph");
        assertTrue(induced.edges().contains(ab));
        assertFalse(induced.edges().contains(bc));
        assertFalse(induced.edges().contains(ca));
    }

    @Test
    public void testInduceFromOtherGraph() {
        Graph otherGraph = new GlobalGraph(new GlobalNodeSet(a, b, c));
        otherGraph.addEdge(ab);
        otherGraph.addEdge(bc);
        otherGraph.addEdge(ca);

        Graph induced = graph.induce(otherGraph);

        assertEquals(2, induced.nodes().size());
        assertTrue(induced.nodes().contains(a));
        assertTrue(induced.nodes().contains(b));

        assertEquals(1, induced.edges().size());
        assertTrue(induced.edges().contains(ab));
    }
}
