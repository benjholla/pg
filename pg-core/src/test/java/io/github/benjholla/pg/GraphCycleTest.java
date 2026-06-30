package io.github.benjholla.pg;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GraphCycleTest {
    @Test
    public void testForwardWithCycle() {
        Node a = new Node();
        Node b = new Node();
        Node c = new Node();

        Edge ab = new Edge(a, b);
        Edge bc = new Edge(b, c);
        Edge ca = new Edge(c, a); // Cycle!

        PropertyGraph graph = new PropertyGraph(a, b, c);
        graph.add(ab);
        graph.add(bc);
        graph.add(ca);

        Graph result = graph.forward(a);
        assertEquals(3, result.nodes().size());
        assertEquals(3, result.edges().size());
    }
}
