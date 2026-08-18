package dev.chpg.pg.multiverse.ephemeral;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.Edge;
import dev.chpg.pg.multiverse.universe.Universe;

public class BetweenStepEquivalenceInvariantTest {

    @Test
    public void testBetweenStepEquivalenceGraphAndSetAndNode() {
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
        Graph toGraph = graph.induce(e3);

        Graph betweenStepGraphs = graph.betweenStep(fromGraph, toGraph);
        Graph betweenStepSets = graph.betweenStep(fromGraph.nodes(), toGraph.nodes());

        assertEquals(betweenStepSets.nodes().size(), betweenStepGraphs.nodes().size());
        assertEquals(betweenStepSets.edges().size(), betweenStepGraphs.edges().size());

        assertTrue(betweenStepGraphs.nodes().containsAll(betweenStepSets.nodes()));
        assertTrue(betweenStepSets.nodes().containsAll(betweenStepGraphs.nodes()));

        assertTrue(betweenStepGraphs.edges().containsAll(betweenStepSets.edges()));
        assertTrue(betweenStepSets.edges().containsAll(betweenStepGraphs.edges()));

        Graph betweenStepNodes = graph.betweenStep(b, c);

        Graph betweenStepNodesAsSets = graph.betweenStep(graph.singleton(b), graph.singleton(c));

        assertEquals(betweenStepNodesAsSets.nodes().size(), betweenStepNodes.nodes().size());
        assertEquals(betweenStepNodesAsSets.edges().size(), betweenStepNodes.edges().size());

        assertTrue(betweenStepNodes.nodes().containsAll(betweenStepNodesAsSets.nodes()));
        assertTrue(betweenStepNodesAsSets.nodes().containsAll(betweenStepNodes.nodes()));
    }
}
