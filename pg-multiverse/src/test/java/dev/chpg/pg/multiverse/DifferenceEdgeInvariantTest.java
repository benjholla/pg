package dev.chpg.pg.multiverse;

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.multiverse.ephemeral.EphemeralGraph;
import dev.chpg.pg.multiverse.ephemeral.EphemeralFactory;
import dev.chpg.pg.multiverse.universe.Universe;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DifferenceEdgeInvariantTest {

    @Test
    public void testDifferenceEdgeInvariantEphemeral() {
        Universe u = new Universe();
        EphemeralFactory factory = new EphemeralGraph(u).factory();
        Node n1 = factory.createNode();
        Node n2 = factory.createNode();
        Node n3 = factory.createNode();
        Edge e = factory.createEdge(n1, n2);

        // Case 1: graph has neither node
        Graph g0 = factory.createGraph();
        Graph diff0 = g0.difference(e);
        assertEquals(0, diff0.nodes().size());
        assertEquals(0, diff0.edges().size());

        // Case 2: graph has one node of the edge
        Graph g1 = factory.createGraph();
        g1.addNode(n1);
        Graph diff1 = g1.difference(e);
        // difference(Edge) treats an edge as a subgraph containing the edge and its terminals.
        // therefore subtracting it removes the edge AND its terminal nodes.
        assertFalse(diff1.nodes().contains(n1));
        assertEquals(0, diff1.nodes().size());
        assertEquals(0, diff1.edges().size());

        // Case 3: graph has completely separate node
        Graph g2 = factory.createGraph();
        g2.addNode(n3);
        Graph diff2 = g2.difference(e);
        assertTrue(diff2.nodes().contains(n3));
        assertEquals(1, diff2.nodes().size());
        assertEquals(0, diff2.edges().size());

        // Case 4: graph has both nodes and the edge
        Graph g3 = factory.createGraph();
        g3.addNode(n1);
        g3.addNode(n2);
        g3.addEdge(e);
        Graph diff3 = g3.difference(e);
        assertFalse(diff3.nodes().contains(n1));
        assertFalse(diff3.nodes().contains(n2));
        assertFalse(diff3.edges().contains(e));
        assertEquals(0, diff3.nodes().size());
        assertEquals(0, diff3.edges().size());

        // Case 5: differenceEdges should ONLY remove the edge, leaving nodes intact
        Graph diffEdges3 = g3.differenceEdges(e);
        assertTrue(diffEdges3.nodes().contains(n1));
        assertTrue(diffEdges3.nodes().contains(n2));
        assertEquals(2, diffEdges3.nodes().size());
        assertFalse(diffEdges3.edges().contains(e));
        assertEquals(0, diffEdges3.edges().size());
    }
}
