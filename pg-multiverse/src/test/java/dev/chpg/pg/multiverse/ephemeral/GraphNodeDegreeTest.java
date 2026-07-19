package dev.chpg.pg.multiverse.ephemeral;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.NodeSet;

public class GraphNodeDegreeTest {

    @Test
    public void testRootsLeavesIsolated() {
        EphemeralGraph graph = new EphemeralGraph();

        Node a = graph.createNode();
        Node b = graph.createNode();
        Node c = graph.createNode();
        Node isolated = graph.createNode();

        // a -> b -> c
        graph.addNode(a);
        graph.addNode(b);
        graph.addNode(c);
        graph.addNode(isolated);

        graph.linkEdge(graph.createEdge(a, b));
        graph.linkEdge(graph.createEdge(b, c));

        NodeSet roots = graph.roots();
        assertEquals(2, roots.size());
        assertTrue(roots.contains(a));
        assertTrue(roots.contains(isolated));

        NodeSet leaves = graph.leaves();
        assertEquals(2, leaves.size());
        assertTrue(leaves.contains(c));
        assertTrue(leaves.contains(isolated));

        NodeSet iso = graph.isolated();
        assertEquals(1, iso.size());
        assertTrue(iso.contains(isolated));
    }
}
