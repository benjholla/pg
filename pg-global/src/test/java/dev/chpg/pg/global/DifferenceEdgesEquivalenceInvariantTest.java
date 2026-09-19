package dev.chpg.pg.global;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.Edge;

public class DifferenceEdgesEquivalenceInvariantTest {

    @Test
    public void testDifferenceEdgesEquivalenceGraphAndEdge() {
        GlobalGraph graph = new GlobalGraph();
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

        GlobalGraph diffSource = new GlobalGraph();
        diffSource.addNode(a);
        diffSource.addNode(b);
        diffSource.addEdge(e1);

        Graph diffGraph = graph.differenceEdges(diffSource);
        Graph diffEdge = graph.differenceEdges(e1);

        assertEquals(diffGraph.nodes().size(), diffEdge.nodes().size());
        assertEquals(diffGraph.edges().size(), diffEdge.edges().size());

        assertTrue(diffGraph.nodes().containsAll(diffEdge.nodes()));
        assertTrue(diffEdge.nodes().containsAll(diffGraph.nodes()));

        assertTrue(diffGraph.edges().containsAll(diffEdge.edges()));
        assertTrue(diffEdge.edges().containsAll(diffGraph.edges()));
    }
}
