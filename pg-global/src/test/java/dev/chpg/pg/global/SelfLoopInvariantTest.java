package dev.chpg.pg.global;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;

public class SelfLoopInvariantTest {
    @Test
    public void testSelfLoop() {
        Node a = new GlobalNode();
        Edge loop = new GlobalEdge(a, a);

        GlobalGraph graph = new GlobalGraph(a);
        graph.addEdge(loop);

        Graph result = graph.forward(a);
        assertEquals(1, result.nodes().size());
        assertEquals(1, result.edges().size());

        Graph step = graph.forwardStep(a);
        assertEquals(1, step.nodes().size());
        assertEquals(1, step.edges().size());
    }
}
