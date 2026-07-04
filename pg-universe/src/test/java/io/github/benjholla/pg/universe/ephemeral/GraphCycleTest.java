package io.github.benjholla.pg.universe.ephemeral;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import io.github.benjholla.pg.api.Edge;
import io.github.benjholla.pg.api.Graph;
import io.github.benjholla.pg.api.Node;

public class GraphCycleTest {
    @Test
    public void testForwardWithCycle() {
        Node a = new EphemeralNode();
        Node b = new EphemeralNode();
        Node c = new EphemeralNode();

        Edge ab = new EphemeralEdge(a, b);
        Edge bc = new EphemeralEdge(b, c);
        Edge ca = new EphemeralEdge(c, a); // Cycle!

        EphemeralGraph graph = new EphemeralGraph(a, b, c);
        graph.addEdge(ab);
        graph.addEdge(bc);
        graph.addEdge(ca);

        Graph result = graph.forward(a);
        assertEquals(3, result.nodes().size());
        assertEquals(3, result.edges().size());
    }
}
