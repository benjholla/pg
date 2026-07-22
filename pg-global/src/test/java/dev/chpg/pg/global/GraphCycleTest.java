package dev.chpg.pg.global;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;

public class GraphCycleTest {
    @Test
    public void testForwardWithCycle() {
        Node a = new GlobalNode();
        Node b = new GlobalNode();
        Node c = new GlobalNode();

        Edge ab = new GlobalEdge(a, b);
        Edge bc = new GlobalEdge(b, c);
        Edge ca = new GlobalEdge(c, a); // Cycle!

        GlobalGraph graph = new GlobalGraph(new GlobalNodeSet(a, b, c));
        graph.addEdge(ab);
        graph.addEdge(bc);
        graph.addEdge(ca);

        Graph result = graph.forward(a);
        assertEquals(3, result.nodes().size());
        assertEquals(3, result.edges().size());
    }
}
