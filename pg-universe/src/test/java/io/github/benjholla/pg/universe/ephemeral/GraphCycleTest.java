package io.github.benjholla.pg.universe.ephemeral;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import io.github.benjholla.pg.api.Edge;
import io.github.benjholla.pg.api.Graph;
import io.github.benjholla.pg.api.Node;

public class GraphCycleTest {
    @Test
    public void testForwardWithCycle() {
        Node a = (EphemeralNode) new EphemeralGraph().createNode();
        Node b = (EphemeralNode) new EphemeralGraph().createNode();
        Node c = (EphemeralNode) new EphemeralGraph().createNode();

        Edge ab = (EphemeralEdge) new EphemeralGraph().createEdge(a, b);
        Edge bc = (EphemeralEdge) new EphemeralGraph().createEdge(b, c);
        Edge ca = (EphemeralEdge) new EphemeralGraph().createEdge(c, a); // Cycle!

        EphemeralGraph graph = (EphemeralGraph) new EphemeralGraph().createGraph(a, b, c);
        graph.addEdge(ab);
        graph.addEdge(bc);
        graph.addEdge(ca);

        Graph result = graph.forward(a);
        assertEquals(3, result.nodes().size());
        assertEquals(3, result.edges().size());
    }
}
