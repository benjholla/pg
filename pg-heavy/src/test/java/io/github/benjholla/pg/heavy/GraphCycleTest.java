package io.github.benjholla.pg.heavy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import io.github.benjholla.pg.api.Edge;
import io.github.benjholla.pg.api.Graph;
import io.github.benjholla.pg.api.Node;

public class GraphCycleTest {
    @Test
    public void testForwardWithCycle() {
        Node a = new HeavyNode();
        Node b = new HeavyNode();
        Node c = new HeavyNode();

        Edge ab = new HeavyEdge(a, b);
        Edge bc = new HeavyEdge(b, c);
        Edge ca = new HeavyEdge(c, a); // Cycle!

        HeavyGraph graph = new HeavyGraph(a, b, c);
        graph.addEdge(ab);
        graph.addEdge(bc);
        graph.addEdge(ca);

        Graph result = graph.forward(a);
        assertEquals(3, result.nodes().size());
        assertEquals(3, result.edges().size());
    }
}
