package dev.chpg.pg.global;

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Validates edge cases for set-theoretic graph algebra with Node and Edge operands.
 */
public class NodeEdgeAlgebraEdgeCaseTest {

    @Test
    public void testDifferenceNodeSelf() {
        GlobalFactory factory = new GlobalGraph().factory();
        Node n1 = factory.createNode();
        Graph g = factory.createGraph(n1);
        Graph diff = g.difference(n1);
        assertTrue(diff.nodes().isEmpty());
    }

    @Test
    public void testDifferenceEdgeSelf() {
        GlobalFactory factory = new GlobalGraph().factory();
        Node n1 = factory.createNode();
        Node n2 = factory.createNode();
        Edge e1 = factory.createEdge(n1, n2);
        Graph g = factory.createGraph(n1, n2);
        g.addEdge(e1);

        Graph diff = g.difference(e1);
        // subtracting e1 subtracts n1 and n2
        assertTrue(diff.nodes().isEmpty());
        assertTrue(diff.edges().isEmpty());
    }
}
