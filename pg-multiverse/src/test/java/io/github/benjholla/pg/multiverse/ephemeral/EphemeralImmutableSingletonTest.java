package io.github.benjholla.pg.multiverse.ephemeral;

import io.github.benjholla.pg.api.NodeSet;
import io.github.benjholla.pg.api.EdgeSet;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class EphemeralImmutableSingletonTest {

    @Test
    public void testSingletonCreation() {
        EphemeralGraph graph = new EphemeralGraph();
        EphemeralNode n1 = graph.createNode();
        EphemeralNode n2 = graph.createNode();
        EphemeralEdge e1 = graph.createEdge(n1, n2);

        NodeSet nodeSingleton = graph.singleton(n1);
        assertTrue(nodeSingleton instanceof EphemeralImmutableSingletonNodeSet);
        assertEquals(1, nodeSingleton.size());

        EdgeSet edgeSingleton = graph.singleton(e1);
        assertTrue(edgeSingleton instanceof EphemeralImmutableSingletonEdgeSet);
        assertEquals(1, edgeSingleton.size());
    }

    @Test
    public void testSetOperationsYieldingSingleton() {
        EphemeralGraph graph = new EphemeralGraph();
        EphemeralNode n1 = graph.createNode();
        EphemeralNode n2 = graph.createNode();

        EphemeralNodeSet set1 = new EphemeralNodeSet();
        set1.add(n1);
        set1.add(n2);

        EphemeralNodeSet set2 = new EphemeralNodeSet();
        set2.add(n1);

        NodeSet intersectResult = set1.intersect(set2);
        assertTrue(intersectResult instanceof EphemeralImmutableSingletonNodeSet);

        NodeSet diffResult = set1.difference(set2);
        assertTrue(diffResult instanceof EphemeralImmutableSingletonNodeSet);
    }
}
