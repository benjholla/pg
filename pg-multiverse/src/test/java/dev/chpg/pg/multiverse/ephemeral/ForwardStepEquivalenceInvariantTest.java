package dev.chpg.pg.multiverse.ephemeral;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.Edge;
import dev.chpg.pg.multiverse.universe.Universe;

public class ForwardStepEquivalenceInvariantTest {

    @Test
    public void testForwardStepEquivalenceGraphAndSetAndNode() {
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

        Graph forwardStepGraph = graph.forwardStep(fromGraph);
        Graph forwardStepSet = graph.forwardStep(fromGraph.nodes());

        assertEquals(forwardStepSet.nodes().size(), forwardStepGraph.nodes().size());
        assertEquals(forwardStepSet.edges().size(), forwardStepGraph.edges().size());

        assertTrue(forwardStepGraph.nodes().containsAll(forwardStepSet.nodes()));
        assertTrue(forwardStepSet.nodes().containsAll(forwardStepGraph.nodes()));

        assertTrue(forwardStepGraph.edges().containsAll(forwardStepSet.edges()));
        assertTrue(forwardStepSet.edges().containsAll(forwardStepGraph.edges()));

        Graph forwardStepNode = graph.forwardStep(b);
        Graph forwardStepNodeSet = graph.forwardStep(graph.singleton(b));

        assertEquals(forwardStepNodeSet.nodes().size(), forwardStepNode.nodes().size());
        assertEquals(forwardStepNodeSet.edges().size(), forwardStepNode.edges().size());

        assertTrue(forwardStepNode.nodes().containsAll(forwardStepNodeSet.nodes()));
        assertTrue(forwardStepNodeSet.nodes().containsAll(forwardStepNode.nodes()));
    }
}
