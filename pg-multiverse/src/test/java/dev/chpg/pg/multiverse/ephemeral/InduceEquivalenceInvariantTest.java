package dev.chpg.pg.multiverse.ephemeral;

import dev.chpg.pg.multiverse.universe.Universe;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.Edge;

public class InduceEquivalenceInvariantTest {

    @Test
    public void testInduceEquivalenceGraphAndSetAndEdge() {
        Universe universe = new Universe();
        EphemeralGraph graph = new EphemeralGraph(universe);

        Node a = graph.factory().createNode();
        Node b = graph.factory().createNode();
        Node c = graph.factory().createNode();

        graph.addNode(a);
        graph.addNode(b);
        graph.addNode(c);

        Edge e1 = graph.factory().createEdge(a, b);
        Edge e2 = graph.factory().createEdge(b, c);

        graph.addEdge(e1);
        graph.addEdge(e2);

        Graph induceSource = graph.factory().createGraph();
        induceSource.addNode(a);
        induceSource.addNode(b);
        induceSource.addEdge(e1);

        Graph induceGraph = graph.induce(induceSource);
        Graph induceSet = graph.induce(induceSource.edges());
        Graph induceEdge = graph.induce(e1);

        assertEquals(induceGraph.nodes().size(), induceSet.nodes().size());
        assertEquals(induceGraph.edges().size(), induceSet.edges().size());

        assertTrue(induceGraph.nodes().containsAll(induceSet.nodes()));
        assertTrue(induceSet.nodes().containsAll(induceGraph.nodes()));

        assertTrue(induceGraph.edges().containsAll(induceSet.edges()));
        assertTrue(induceSet.edges().containsAll(induceGraph.edges()));

        assertEquals(induceEdge.nodes().size(), induceSet.nodes().size());
        assertEquals(induceEdge.edges().size(), induceSet.edges().size());

        assertTrue(induceEdge.nodes().containsAll(induceSet.nodes()));
        assertTrue(induceSet.nodes().containsAll(induceEdge.nodes()));

        assertTrue(induceEdge.edges().containsAll(induceSet.edges()));
        assertTrue(induceSet.edges().containsAll(induceEdge.edges()));
    }
}
