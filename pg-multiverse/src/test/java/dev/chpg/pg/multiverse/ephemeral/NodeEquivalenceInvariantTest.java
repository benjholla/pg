package dev.chpg.pg.multiverse.ephemeral;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.Edge;
import dev.chpg.pg.multiverse.universe.Universe;

public class NodeEquivalenceInvariantTest {

    @Test
    public void testBetweenEquivalenceGraphAndSet() {
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

        Graph betweenGraphs = graph.between(fromGraph, toGraph);
        Graph betweenSets = graph.between(fromGraph.nodes(), toGraph.nodes());

        assertEquals(betweenSets.nodes().size(), betweenGraphs.nodes().size());
        assertEquals(betweenSets.edges().size(), betweenGraphs.edges().size());

        assertTrue(betweenGraphs.nodes().containsAll(betweenSets.nodes()));
        assertTrue(betweenSets.nodes().containsAll(betweenGraphs.nodes()));

        assertTrue(betweenGraphs.edges().containsAll(betweenSets.edges()));
        assertTrue(betweenSets.edges().containsAll(betweenGraphs.edges()));

        Graph betweenNodes = graph.between(b, c);

        Graph betweenNodesAsSets = graph.between(graph.singleton(b), graph.singleton(c));

        assertEquals(betweenNodesAsSets.nodes().size(), betweenNodes.nodes().size());
        assertEquals(betweenNodesAsSets.edges().size(), betweenNodes.edges().size());

        assertTrue(betweenNodes.nodes().containsAll(betweenNodesAsSets.nodes()));
        assertTrue(betweenNodesAsSets.nodes().containsAll(betweenNodes.nodes()));
    }
}
