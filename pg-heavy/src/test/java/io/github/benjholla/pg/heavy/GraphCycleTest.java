package io.github.benjholla.pg.heavy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import io.github.benjholla.pg.api.Edge;
import io.github.benjholla.pg.api.Graph;
import io.github.benjholla.pg.api.Node;

public class GraphCycleTest {
    @Test
    public void testForwardWithCycle() {
        Node a = (HeavyNode) new HeavyGraph().createNode();
        Node b = (HeavyNode) new HeavyGraph().createNode();
        Node c = (HeavyNode) new HeavyGraph().createNode();

        Edge ab = (HeavyEdge) new HeavyGraph().createEdge(a, b);
        Edge bc = (HeavyEdge) new HeavyGraph().createEdge(b, c);
        Edge ca = (HeavyEdge) new HeavyGraph().createEdge(c, a); // Cycle!

        HeavyGraph graph = (HeavyGraph) new HeavyGraph().createGraph(a, b, c);
        graph.addEdge(ab);
        graph.addEdge(bc);
        graph.addEdge(ca);

        Graph result = graph.forward(a);
        assertEquals(3, result.nodes().size());
        assertEquals(3, result.edges().size());
    }
}
