package dev.chpg.pg.multiverse.ephemeral;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.Edge;
import dev.chpg.pg.multiverse.universe.Universe;

public class ForwardEquivalenceInvariantTest {

    @Test
    public void testForwardEquivalenceGraphAndSetAndNode() {
        Universe universe = new Universe();
        EphemeralGraph graph = new EphemeralGraph(universe);

        Node a = graph.factory().createNode();
        Node b = graph.factory().createNode();
        Node c = graph.factory().createNode();
        Node d = graph.factory().createNode();

        graph.addNode(a);
        graph.addNode(b);
        graph.addNode(c);
        graph.addNode(d);

        Edge e1 = graph.factory().createEdge(a, b);
        Edge e2 = graph.factory().createEdge(b, c);
        Edge e3 = graph.factory().createEdge(c, d);

        graph.addEdge(e1);
        graph.addEdge(e2);
        graph.addEdge(e3);

        Graph fromGraph = graph.induce(e1);

        Graph forwardGraph = graph.forward(fromGraph);
        Graph forwardSet = graph.forward(fromGraph.nodes());

        assertEquals(forwardSet.nodes().size(), forwardGraph.nodes().size());
        assertEquals(forwardSet.edges().size(), forwardGraph.edges().size());

        assertTrue(forwardGraph.nodes().containsAll(forwardSet.nodes()));
        assertTrue(forwardSet.nodes().containsAll(forwardGraph.nodes()));

        assertTrue(forwardGraph.edges().containsAll(forwardSet.edges()));
        assertTrue(forwardSet.edges().containsAll(forwardGraph.edges()));

        Graph forwardNode = graph.forward(b);
        Graph forwardNodeSet = graph.forward(graph.singleton(b));

        assertEquals(forwardNodeSet.nodes().size(), forwardNode.nodes().size());
        assertEquals(forwardNodeSet.edges().size(), forwardNode.edges().size());

        assertTrue(forwardNode.nodes().containsAll(forwardNodeSet.nodes()));
        assertTrue(forwardNodeSet.nodes().containsAll(forwardNode.nodes()));
    }
}
