package dev.chpg.pg.global;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.Edge;

public class BetweenStepEquivalenceInvariantTest {

    @Test
    public void testBetweenStepEquivalence() {
        GlobalGraph graph = new GlobalGraph();
        Node a = graph.factory().createNode();
        Node b = graph.factory().createNode();
        Node c = graph.factory().createNode();
        Node d = graph.factory().createNode();
        Node e = graph.factory().createNode();

        graph.addNode(a);
        graph.addNode(b);
        graph.addNode(c);
        graph.addNode(d);
        graph.addNode(e);

        Edge e1 = graph.factory().createEdge(a, b);
        Edge e2 = graph.factory().createEdge(b, c);
        Edge e3 = graph.factory().createEdge(c, d);
        Edge e4 = graph.factory().createEdge(a, e);
        Edge e5 = graph.factory().createEdge(e, d);

        graph.addEdge(e1);
        graph.addEdge(e2);
        graph.addEdge(e3);
        graph.addEdge(e4);
        graph.addEdge(e5);

        Graph betweenStep = graph.betweenStep(a, d);
        Graph equivalent = graph.forwardStep(a).intersection(graph.reverseStep(d));

        assertEquals(equivalent.nodes().size(), betweenStep.nodes().size());
        assertEquals(equivalent.edges().size(), betweenStep.edges().size());

        for (Node n : equivalent.nodes()) {
            assertEquals(true, betweenStep.nodes().contains(n));
        }
        for (Edge ed : equivalent.edges()) {
            assertEquals(true, betweenStep.edges().contains(ed));
        }
    }

    @Test
    public void testBetweenStepEquivalenceGraphAndSetAndNode() {
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
        Graph toGraph = graph.induce(e3);

        Graph betweenStepGraphs = graph.betweenStep(fromGraph, toGraph);
        Graph betweenStepSets = graph.betweenStep(fromGraph.nodes(), toGraph.nodes());

        assertEquals(betweenStepSets.nodes().size(), betweenStepGraphs.nodes().size());
        assertEquals(betweenStepSets.edges().size(), betweenStepGraphs.edges().size());

        assertTrue(betweenStepGraphs.nodes().containsAll(betweenStepSets.nodes()));
        assertTrue(betweenStepSets.nodes().containsAll(betweenStepGraphs.nodes()));

        assertTrue(betweenStepGraphs.edges().containsAll(betweenStepSets.edges()));
        assertTrue(betweenStepSets.edges().containsAll(betweenStepGraphs.edges()));

        Graph equivalentGraphs = graph.forwardStep(fromGraph).intersection(graph.reverseStep(toGraph));
        assertEquals(equivalentGraphs.nodes().size(), betweenStepGraphs.nodes().size());
        assertEquals(equivalentGraphs.edges().size(), betweenStepGraphs.edges().size());

        Graph betweenStepNodes = graph.betweenStep(b, c);

        Graph betweenStepNodesAsSets = graph.betweenStep(graph.singleton(b), graph.singleton(c));

        assertEquals(betweenStepNodesAsSets.nodes().size(), betweenStepNodes.nodes().size());
        assertEquals(betweenStepNodesAsSets.edges().size(), betweenStepNodes.edges().size());

        assertTrue(betweenStepNodes.nodes().containsAll(betweenStepNodesAsSets.nodes()));
        assertTrue(betweenStepNodesAsSets.nodes().containsAll(betweenStepNodes.nodes()));

        Graph equivalentNodes = graph.forwardStep(b).intersection(graph.reverseStep(c));
        assertEquals(equivalentNodes.nodes().size(), betweenStepNodes.nodes().size());
        assertEquals(equivalentNodes.edges().size(), betweenStepNodes.edges().size());
    }
}
