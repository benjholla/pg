package io.github.benjholla.pg.heavy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import io.github.benjholla.pg.api.Edge;
import io.github.benjholla.pg.api.Graph;
import io.github.benjholla.pg.api.Node;

public class SelfLoopInvariantTest {
    @Test
    public void testSelfLoop() {
        Node a = (HeavyNode) new HeavyGraph().createNode();
        Edge loop = (HeavyEdge) new HeavyGraph().createEdge(a, a);

        HeavyGraph graph = (HeavyGraph) new HeavyGraph().createGraph(a);
        graph.addEdge(loop);

        Graph result = graph.forward(a);
        assertEquals(1, result.nodes().size());
        assertEquals(1, result.edges().size());

        Graph step = graph.forwardStep(a);
        assertEquals(1, step.nodes().size());
        assertEquals(1, step.edges().size());
    }
}
