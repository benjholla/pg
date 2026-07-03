package io.github.benjholla.pg.universe.ephemeral;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import io.github.benjholla.pg.api.Edge;
import io.github.benjholla.pg.api.Graph;
import io.github.benjholla.pg.api.Node;

public class SelfLoopInvariantTest {
    @Test
    public void testSelfLoop() {
        Node a = new EphemeralNode();
        Edge loop = new EphemeralEdge(a, a);

        EphemeralGraph graph = new EphemeralGraph(a);
        graph.add(loop);

        Graph result = graph.forward(a);
        assertEquals(1, result.nodes().size());
        assertEquals(1, result.edges().size());

        Graph step = graph.forwardStep(a);
        assertEquals(1, step.nodes().size());
        assertEquals(1, step.edges().size());
    }
}
