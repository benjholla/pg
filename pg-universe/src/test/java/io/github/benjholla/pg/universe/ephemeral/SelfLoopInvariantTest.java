package io.github.benjholla.pg.universe.ephemeral;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import io.github.benjholla.pg.api.Edge;
import io.github.benjholla.pg.api.Graph;
import io.github.benjholla.pg.api.Node;

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
