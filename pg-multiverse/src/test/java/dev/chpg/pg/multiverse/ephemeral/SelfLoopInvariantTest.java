package dev.chpg.pg.multiverse.ephemeral;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;

public class SelfLoopInvariantTest {
    private static final EphemeralFactory factory = new EphemeralGraph().factory();

    @Test
    public void testSelfLoop() {
        Node a = factory.createNode();
        Edge loop = factory.createEdge(a, a);

        Graph graph = factory.createGraph(a);
        graph.addEdge(loop);

        Graph result = graph.forward(a);
        assertEquals(1, result.nodes().size());
        assertEquals(1, result.edges().size());

        Graph step = graph.forwardStep(a);
        assertEquals(1, step.nodes().size());
        assertEquals(1, step.edges().size());
    }
}
