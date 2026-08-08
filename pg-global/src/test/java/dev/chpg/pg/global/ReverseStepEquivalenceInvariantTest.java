package dev.chpg.pg.global;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.Edge;

public class ReverseStepEquivalenceInvariantTest {

    @Test
    public void testReverseStepEquivalenceGraphAndSetAndNode() {
        GlobalGraph graph = new GlobalGraph();
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

        Graph reverseStepGraph = graph.reverseStep(fromGraph);
        Graph reverseStepSet = graph.reverseStep(fromGraph.nodes());

        assertEquals(reverseStepSet.nodes().size(), reverseStepGraph.nodes().size());
        assertEquals(reverseStepSet.edges().size(), reverseStepGraph.edges().size());

        assertTrue(reverseStepGraph.nodes().containsAll(reverseStepSet.nodes()));
        assertTrue(reverseStepSet.nodes().containsAll(reverseStepGraph.nodes()));

        assertTrue(reverseStepGraph.edges().containsAll(reverseStepSet.edges()));
        assertTrue(reverseStepSet.edges().containsAll(reverseStepGraph.edges()));

        Graph reverseStepNode = graph.reverseStep(b);
        Graph reverseStepNodeSet = graph.reverseStep(graph.singleton(b));

        assertEquals(reverseStepNodeSet.nodes().size(), reverseStepNode.nodes().size());
        assertEquals(reverseStepNodeSet.edges().size(), reverseStepNode.edges().size());

        assertTrue(reverseStepNode.nodes().containsAll(reverseStepNodeSet.nodes()));
        assertTrue(reverseStepNodeSet.nodes().containsAll(reverseStepNode.nodes()));
    }
}
