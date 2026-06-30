package io.github.benjholla.pg;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MultiGraphInvariantTest {
    @Test
    public void testMultipleEdgesBetweenSameNodes() {
        Node a = new Node();
        Node b = new Node();
        Edge e1 = new Edge(a, b);
        e1.tags().add("type1");
        Edge e2 = new Edge(a, b);
        e2.tags().add("type2");

        PropertyGraph graph = new PropertyGraph(a, b);
        graph.add(e1);
        graph.add(e2);

        Graph result = graph.forwardStep(a);
        assertEquals(2, result.nodes().size());
        assertEquals(2, result.edges().size(), "Should include both edges between the same nodes");
        assertTrue(result.edges().contains(e1));
        assertTrue(result.edges().contains(e2));
    }
}
