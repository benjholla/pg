package dev.chpg.pg.global;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.Edge;

public class DifferenceEdgesEquivalenceInvariantTest {

    @Test
    public void testDifferenceEdgesEquivalence() {
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

        GlobalGraph edgeGraph = new GlobalGraph(a, b);
        edgeGraph.addEdge(e1);

        Graph diffEdgesEdge = graph.differenceEdges(e1);
        Graph diffEdgesGraph = graph.differenceEdges(edgeGraph);

        assertEquals(diffEdgesEdge.nodes().size(), diffEdgesGraph.nodes().size());
        assertEquals(diffEdgesEdge.edges().size(), diffEdgesGraph.edges().size());

        assertTrue(diffEdgesEdge.nodes().containsAll(diffEdgesGraph.nodes()));
        assertTrue(diffEdgesGraph.nodes().containsAll(diffEdgesEdge.nodes()));

        assertTrue(diffEdgesEdge.edges().containsAll(diffEdgesGraph.edges()));
        assertTrue(diffEdgesGraph.edges().containsAll(diffEdgesEdge.edges()));
    }
}
