package dev.chpg.pg.multiverse;

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.AttributeValue;
import dev.chpg.pg.multiverse.ephemeral.EphemeralGraph;
import dev.chpg.pg.multiverse.ephemeral.EphemeralFactory;
import dev.chpg.pg.multiverse.universe.Universe;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GraphEdgeOperationsInvariantTest {

    @Test
    public void testGraphEdgeOperationsInvariantEphemeral() {
        Universe u = new Universe();
        EphemeralFactory factory = new EphemeralGraph(u).factory();
        Node n1 = factory.createNode();
        Node n2 = factory.createNode();
        Node n3 = factory.createNode();
        Edge e = factory.createEdge(n1, n2);

        // Case 1: graph has neither node
        Graph g0 = factory.createGraph();

        Graph intersect0 = g0.intersection(e);
        assertEquals(0, intersect0.nodes().size());
        assertEquals(0, intersect0.edges().size());

        Graph diff0 = g0.difference(e);
        assertEquals(0, diff0.nodes().size());
        assertEquals(0, diff0.edges().size());

        Graph diffEdge0 = g0.differenceEdges(e);
        assertEquals(0, diffEdge0.nodes().size());
        assertEquals(0, diffEdge0.edges().size());

        Graph induce0 = g0.induce(e);
        assertEquals(0, induce0.nodes().size());
        assertEquals(0, induce0.edges().size());

        // Case 2: graph has one node
        Graph g1 = factory.createGraph();
        g1.addNode(n1);
        g1.addNode(n3);

        Graph intersect1 = g1.intersection(e);
        assertTrue(intersect1.nodes().contains(n1));
        assertFalse(intersect1.nodes().contains(n2));
        assertFalse(intersect1.nodes().contains(n3));
        assertEquals(1, intersect1.nodes().size());
        assertEquals(0, intersect1.edges().size());

        Graph diff1 = g1.difference(e);
        assertFalse(diff1.nodes().contains(n1)); // n1 should be removed
        assertTrue(diff1.nodes().contains(n3));
        assertEquals(1, diff1.nodes().size());
        assertEquals(0, diff1.edges().size());

        Graph diffEdge1 = g1.differenceEdges(e);
        assertTrue(diffEdge1.nodes().contains(n1));
        assertTrue(diffEdge1.nodes().contains(n3));
        assertEquals(2, diffEdge1.nodes().size());
        assertEquals(0, diffEdge1.edges().size());

        Graph induce1 = g1.induce(e);
        assertEquals(2, induce1.nodes().size());
        assertEquals(0, induce1.edges().size());

        // Case 3: graph has both nodes, but not the edge
        Graph g2 = factory.createGraph();
        g2.addNode(n1);
        g2.addNode(n2);
        g2.addNode(n3);

        Graph intersect2 = g2.intersection(e);
        assertTrue(intersect2.nodes().contains(n1));
        assertTrue(intersect2.nodes().contains(n2));
        assertFalse(intersect2.nodes().contains(n3));
        assertEquals(2, intersect2.nodes().size());
        assertEquals(0, intersect2.edges().size());

        Graph diff2 = g2.difference(e);
        assertFalse(diff2.nodes().contains(n1));
        assertFalse(diff2.nodes().contains(n2));
        assertTrue(diff2.nodes().contains(n3));
        assertEquals(1, diff2.nodes().size());
        assertEquals(0, diff2.edges().size());

        Graph diffEdge2 = g2.differenceEdges(e);
        assertTrue(diffEdge2.nodes().contains(n1));
        assertTrue(diffEdge2.nodes().contains(n2));
        assertTrue(diffEdge2.nodes().contains(n3));
        assertEquals(3, diffEdge2.nodes().size());
        assertEquals(0, diffEdge2.edges().size());

        Graph induce2 = g2.induce(e);
        assertEquals(3, induce2.nodes().size());
        assertEquals(1, induce2.edges().size());
        assertTrue(induce2.edges().contains(e));

        // Case 4: graph has both nodes and the edge
        Graph g3 = factory.createGraph();
        g3.addNode(n1);
        g3.addNode(n2);
        g3.addNode(n3);
        g3.addEdge(e);

        Graph intersect3 = g3.intersection(e);
        assertTrue(intersect3.nodes().contains(n1));
        assertTrue(intersect3.nodes().contains(n2));
        assertFalse(intersect3.nodes().contains(n3));
        assertTrue(intersect3.edges().contains(e));
        assertEquals(2, intersect3.nodes().size());
        assertEquals(1, intersect3.edges().size());

        Graph diff3 = g3.difference(e);
        assertFalse(diff3.nodes().contains(n1));
        assertFalse(diff3.nodes().contains(n2));
        assertTrue(diff3.nodes().contains(n3));
        assertFalse(diff3.edges().contains(e));
        assertEquals(1, diff3.nodes().size());
        assertEquals(0, diff3.edges().size());

        Graph diffEdge3 = g3.differenceEdges(e);
        assertTrue(diffEdge3.nodes().contains(n1));
        assertTrue(diffEdge3.nodes().contains(n2));
        assertTrue(diffEdge3.nodes().contains(n3));
        assertFalse(diffEdge3.edges().contains(e));
        assertEquals(3, diffEdge3.nodes().size());
        assertEquals(0, diffEdge3.edges().size());

        Graph induce3 = g3.induce(e);
        assertEquals(3, induce3.nodes().size());
        assertEquals(1, induce3.edges().size());
        assertTrue(induce3.edges().contains(e));
    }

    @Test
    public void testGraphEdgeOperationsInvariantUniverse() {
        Universe u = new Universe();
        EphemeralFactory factory = new EphemeralGraph(u).factory();
        Node n1 = factory.createNode();
        Node n2 = factory.createNode();
        Node n3 = factory.createNode();
        Edge e = factory.createEdge(n1, n2);
        n1.attributes().put("id", new AttributeValue.IntegerValue(1));
        n2.attributes().put("id", new AttributeValue.IntegerValue(2));
        n3.attributes().put("id", new AttributeValue.IntegerValue(3));

        Graph ephG = factory.createGraph();
        ephG.addNode(n1); ephG.addNode(n2); ephG.addNode(n3); ephG.addEdge(e);
        Graph uniG = u.promote((EphemeralGraph)ephG);

        Node uN1 = null, uN2 = null, uN3 = null;
        for (Node n : uniG.nodes()) {
           if (n.attributes().get("id").equals(new AttributeValue.IntegerValue(1))) {
               uN1 = n;
           }
           if (n.attributes().get("id").equals(new AttributeValue.IntegerValue(2))) {
               uN2 = n;
           }
           if (n.attributes().get("id").equals(new AttributeValue.IntegerValue(3))) {
               uN3 = n;
           }
        }
        Edge uE = uniG.edges().iterator().next();

        // Case 1: graph has neither node
        Graph g0 = uniG.difference(uniG);

        Graph intersect0 = g0.intersection(uE);
        assertEquals(0, intersect0.nodes().size());
        assertEquals(0, intersect0.edges().size());

        Graph diff0 = g0.difference(uE);
        assertEquals(0, diff0.nodes().size());
        assertEquals(0, diff0.edges().size());

        Graph diffEdge0 = g0.differenceEdges(uE);
        assertEquals(0, diffEdge0.nodes().size());
        assertEquals(0, diffEdge0.edges().size());

        Graph induce0 = g0.induce(uE);
        assertEquals(0, induce0.nodes().size());
        assertEquals(0, induce0.edges().size());

        // Case 2: graph has one node
        Graph g1 = g0.union(uN1).union(uN3);

        Graph intersect1 = g1.intersection(uE);
        assertTrue(intersect1.nodes().contains(uN1));
        assertFalse(intersect1.nodes().contains(uN2));
        assertFalse(intersect1.nodes().contains(uN3));
        assertEquals(1, intersect1.nodes().size());
        assertEquals(0, intersect1.edges().size());

        Graph diff1 = g1.difference(uE);
        assertFalse(diff1.nodes().contains(uN1));
        assertTrue(diff1.nodes().contains(uN3));
        assertEquals(1, diff1.nodes().size());
        assertEquals(0, diff1.edges().size());

        Graph diffEdge1 = g1.differenceEdges(uE);
        assertTrue(diffEdge1.nodes().contains(uN1));
        assertTrue(diffEdge1.nodes().contains(uN3));
        assertEquals(2, diffEdge1.nodes().size());
        assertEquals(0, diffEdge1.edges().size());

        Graph induce1 = g1.induce(uE);
        assertEquals(2, induce1.nodes().size());
        assertEquals(0, induce1.edges().size());

        // Case 3: graph has both nodes, but not the edge
        Graph g2 = g0.union(uN1).union(uN2).union(uN3);

        Graph intersect2 = g2.intersection(uE);
        assertTrue(intersect2.nodes().contains(uN1));
        assertTrue(intersect2.nodes().contains(uN2));
        assertFalse(intersect2.nodes().contains(uN3));
        assertEquals(2, intersect2.nodes().size());
        assertEquals(0, intersect2.edges().size());

        Graph diff2 = g2.difference(uE);
        assertFalse(diff2.nodes().contains(uN1));
        assertFalse(diff2.nodes().contains(uN2));
        assertTrue(diff2.nodes().contains(uN3));
        assertEquals(1, diff2.nodes().size());
        assertEquals(0, diff2.edges().size());

        Graph diffEdge2 = g2.differenceEdges(uE);
        assertTrue(diffEdge2.nodes().contains(uN1));
        assertTrue(diffEdge2.nodes().contains(uN2));
        assertTrue(diffEdge2.nodes().contains(uN3));
        assertEquals(3, diffEdge2.nodes().size());
        assertEquals(0, diffEdge2.edges().size());

        Graph induce2 = g2.induce(uE);
        assertEquals(3, induce2.nodes().size());
        assertEquals(1, induce2.edges().size());
        assertTrue(induce2.edges().contains(uE));

        // Case 4: graph has both nodes and the edge
        Graph g3 = g0.union(uN1).union(uN2).union(uN3).union(uE);

        Graph intersect3 = g3.intersection(uE);
        assertTrue(intersect3.nodes().contains(uN1));
        assertTrue(intersect3.nodes().contains(uN2));
        assertFalse(intersect3.nodes().contains(uN3));
        assertTrue(intersect3.edges().contains(uE));
        assertEquals(2, intersect3.nodes().size());
        assertEquals(1, intersect3.edges().size());

        Graph diff3 = g3.difference(uE);
        assertFalse(diff3.nodes().contains(uN1));
        assertFalse(diff3.nodes().contains(uN2));
        assertTrue(diff3.nodes().contains(uN3));
        assertFalse(diff3.edges().contains(uE));
        assertEquals(1, diff3.nodes().size());
        assertEquals(0, diff3.edges().size());

        Graph diffEdge3 = g3.differenceEdges(uE);
        assertTrue(diffEdge3.nodes().contains(uN1));
        assertTrue(diffEdge3.nodes().contains(uN2));
        assertTrue(diffEdge3.nodes().contains(uN3));
        assertFalse(diffEdge3.edges().contains(uE));
        assertEquals(3, diffEdge3.nodes().size());
        assertEquals(0, diffEdge3.edges().size());

        Graph induce3 = g3.induce(uE);
        assertEquals(3, induce3.nodes().size());
        assertEquals(1, induce3.edges().size());
        assertTrue(induce3.edges().contains(uE));
    }
}
