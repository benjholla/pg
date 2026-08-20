package dev.chpg.pg.multiverse.universe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.Factory;
import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.multiverse.ephemeral.EphemeralGraph;
import java.util.Iterator;

public class IntersectionNodeInvariantTest {

    @Test
    public void testIntersectionNodeInvariant() {
        Universe universe = new Universe();
        EphemeralGraph eg = new EphemeralGraph(universe);
        Factory factory = eg.factory();

        Node n1 = factory.createNode();
        Node n2 = factory.createNode();
        Edge e = factory.createEdge(n1, n2);

        eg.addNode(n1);
        eg.addNode(n2);
        eg.addEdge(e);

        universe.promote(eg);

        Graph uGraph = new UniverseGraph(universe);
        Iterator<Node> it = uGraph.nodes().iterator();
        Node uN1 = null;
        Node uN2 = null;
        while(it.hasNext()) {
            Node n = it.next();
            if(n.id() == e.from().id()) {
                uN1 = n;
            } else if (n.id() == e.to().id()) {
                uN2 = n;
            } else {
                if (uN1 == null) {
                    uN1 = n;
                } else {
                    uN2 = n;
                }
            }
        }

        if (uN1 != null && uN2 != null && uGraph.edges().size() > 0) {
            Edge uE = uGraph.edges().iterator().next();

            Graph intersect3 = uGraph.intersection(uN1);
            assertTrue(intersect3.nodes().contains(uN1));
            assertFalse(intersect3.nodes().contains(uN2));
            assertEquals(1, intersect3.nodes().size());
            assertEquals(0, intersect3.edges().size());
        }
    }
}
