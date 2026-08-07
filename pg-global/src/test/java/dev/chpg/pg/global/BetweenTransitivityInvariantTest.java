package dev.chpg.pg.global;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.Edge;

public class BetweenTransitivityInvariantTest {

    @Test
    public void testBetweenTransitivity() {
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

        // a -> b -> c

        // between(a, c) should include between(a, b) and between(b, c)
        Graph betweenAC = graph.between(a, c);
        Graph betweenAB = graph.between(a, b);
        Graph betweenBC = graph.between(b, c);

        assertTrue(betweenAC.nodes().containsAll(betweenAB.nodes()));
        assertTrue(betweenAC.edges().containsAll(betweenAB.edges()));

        assertTrue(betweenAC.nodes().containsAll(betweenBC.nodes()));
        assertTrue(betweenAC.edges().containsAll(betweenBC.edges()));
    }
}
