package io.github.benjholla.pg;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class SelfLoopInvariantTest {
    @Test
    public void testSelfLoop() {
        Node a = new HeavyNode();
        Edge loop = new HeavyEdge(a, a);

        HeavyGraph graph = new HeavyGraph(a);
        graph.add(loop);

        Graph result = graph.forward(a);
        assertEquals(1, result.nodes().size());
        assertEquals(1, result.edges().size());

        Graph step = graph.forwardStep(a);
        assertEquals(1, step.nodes().size());
        assertEquals(1, step.edges().size());
    }
}
