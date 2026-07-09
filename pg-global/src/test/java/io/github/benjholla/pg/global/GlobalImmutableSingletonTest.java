package io.github.benjholla.pg.global;

import io.github.benjholla.pg.api.NodeSet;
import io.github.benjholla.pg.api.EdgeSet;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class GlobalImmutableSingletonTest {

    @Test
    public void testSingletonCreation() {
        GlobalGraph graph = new GlobalGraph();
        GlobalNode n1 = graph.createNode();
        GlobalNode n2 = graph.createNode();
        GlobalEdge e1 = graph.createEdge(n1, n2);

        NodeSet nodeSingleton = graph.singleton(n1);
        assertTrue(nodeSingleton instanceof GlobalImmutableSingletonNodeSet);
        assertEquals(1, nodeSingleton.size());

        EdgeSet edgeSingleton = graph.singleton(e1);
        assertTrue(edgeSingleton instanceof GlobalImmutableSingletonEdgeSet);
        assertEquals(1, edgeSingleton.size());
    }

    @Test
    public void testSetOperationsYieldingSingleton() {
        GlobalGraph graph = new GlobalGraph();
        GlobalNode n1 = graph.createNode();
        GlobalNode n2 = graph.createNode();

        GlobalNodeSet set1 = new GlobalNodeSet();
        set1.add(n1);
        set1.add(n2);

        GlobalNodeSet set2 = new GlobalNodeSet();
        set2.add(n1);

        NodeSet intersectResult = set1.intersect(set2);
        assertTrue(intersectResult instanceof GlobalImmutableSingletonNodeSet);

        NodeSet diffResult = set1.difference(set2);
        assertTrue(diffResult instanceof GlobalImmutableSingletonNodeSet);
    }
}
