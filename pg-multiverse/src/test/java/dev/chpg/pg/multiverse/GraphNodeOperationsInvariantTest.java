package dev.chpg.pg.multiverse;

import dev.chpg.pg.api.AttributeValue;
import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.multiverse.ephemeral.EphemeralGraph;
import dev.chpg.pg.multiverse.ephemeral.EphemeralFactory;
import dev.chpg.pg.multiverse.universe.Universe;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GraphNodeOperationsInvariantTest {

    @Test
    public void testGraphNodeOperationsInvariantEphemeral() {
        Universe u = new Universe();
        EphemeralFactory factory = new EphemeralGraph(u).factory();
        Node n1 = factory.createNode();
        Node n2 = factory.createNode();
        Node n3 = factory.createNode();
        Edge e = factory.createEdge(n1, n2);

        // Case 1: graph has neither node
        Graph g0 = factory.createGraph();

        Graph intersect0 = g0.intersection(n1);
        assertEquals(0, intersect0.nodes().size());
        assertEquals(0, intersect0.edges().size());

        Graph diff0 = g0.difference(n1);
        assertEquals(0, diff0.nodes().size());
        assertEquals(0, diff0.edges().size());

        Graph union0 = g0.union(n1);
        assertEquals(1, union0.nodes().size());
        assertTrue(union0.nodes().contains(n1));
        assertEquals(0, union0.edges().size());

        // Case 2: graph has one node
        Graph g1 = factory.createGraph();
        g1.addNode(n1);

        Graph intersect1 = g1.intersection(n1);
        assertTrue(intersect1.nodes().contains(n1));
        assertEquals(1, intersect1.nodes().size());
        assertEquals(0, intersect1.edges().size());

        Graph diff1 = g1.difference(n1);
        assertFalse(diff1.nodes().contains(n1));
        assertEquals(0, diff1.nodes().size());
        assertEquals(0, diff1.edges().size());

        Graph union1 = g1.union(n1);
        assertTrue(union1.nodes().contains(n1));
        assertEquals(1, union1.nodes().size());
        assertEquals(0, union1.edges().size());

        Graph union1_n2 = g1.union(n2);
        assertTrue(union1_n2.nodes().contains(n1));
        assertTrue(union1_n2.nodes().contains(n2));
        assertEquals(2, union1_n2.nodes().size());
        assertEquals(0, union1_n2.edges().size());

        // Case 3: graph has both nodes and an edge
        Graph g3 = factory.createGraph();
        g3.addNode(n1);
        g3.addNode(n2);
        g3.addEdge(e);
        g3.addNode(n3);

        Graph intersect3 = g3.intersection(n1);
        assertTrue(intersect3.nodes().contains(n1));
        assertFalse(intersect3.nodes().contains(n2));
        assertEquals(1, intersect3.nodes().size());
        assertEquals(0, intersect3.edges().size());

        Graph diff3 = g3.difference(n1);
        assertFalse(diff3.nodes().contains(n1));
        assertTrue(diff3.nodes().contains(n2));
        assertTrue(diff3.nodes().contains(n3));
        assertFalse(diff3.edges().contains(e));
        assertEquals(2, diff3.nodes().size());
        assertEquals(0, diff3.edges().size());

        Graph union3 = g3.union(n1);
        assertEquals(3, union3.nodes().size());
        assertEquals(1, union3.edges().size());
    }

    private Node findByWatermark(Graph graph, int expectedLineage) {
        return graph.nodes().stream().filter(n -> {
            AttributeValue attr = n.attributes().get("test_lineage");
            return attr instanceof AttributeValue.IntegerValue iv && iv.value() == expectedLineage;
        }).findFirst().orElseThrow(() -> new IllegalStateException("Node with lineage " + expectedLineage + " not found"));
    }

    @Test
    public void testGraphNodeOperationsInvariantUniverse() {
        Universe u = new Universe();
        EphemeralFactory factory = new EphemeralGraph(u).factory();

        // We need to commit these to universe to get Universe elements
        EphemeralGraph eg = (EphemeralGraph) factory.createGraph();
        Node tempN1 = factory.createNode();
        Node tempN2 = factory.createNode();
        Node tempN3 = factory.createNode();

        tempN1.attributes().put("test_lineage", new AttributeValue.IntegerValue(1));
        tempN2.attributes().put("test_lineage", new AttributeValue.IntegerValue(2));
        tempN3.attributes().put("test_lineage", new AttributeValue.IntegerValue(3));

        Edge tempE = factory.createEdge(tempN1, tempN2);

        eg.addNode(tempN1);
        eg.addNode(tempN2);
        eg.addNode(tempN3);
        eg.addEdge(tempE);

        u.promote(eg);
        Graph universeGraph = u.asGraph();

        Node uN1 = findByWatermark(universeGraph, 1);
        Node uN2 = findByWatermark(universeGraph, 2);
        Node uN3 = findByWatermark(universeGraph, 3);

        Edge uE = universeGraph.edges().one().orElseThrow(() -> new IllegalStateException("Edge not found"));

        // Now test operations using the Universe projections
        // Case 1: graph has neither node
        Graph g0 = u.asGraph().difference(u.asGraph()); // Empty universe graph

        Graph intersect0 = g0.intersection(uN1);
        assertEquals(0, intersect0.nodes().size());
        assertEquals(0, intersect0.edges().size());

        Graph diff0 = g0.difference(uN1);
        assertEquals(0, diff0.nodes().size());
        assertEquals(0, diff0.edges().size());

        Graph union0 = g0.union(uN1);
        assertEquals(1, union0.nodes().size());
        assertTrue(union0.nodes().contains(uN1));
        assertEquals(0, union0.edges().size());

        // Case 2: graph has one node
        Graph g1 = g0.union(uN1);

        Graph intersect1 = g1.intersection(uN1);
        assertTrue(intersect1.nodes().contains(uN1));
        assertEquals(1, intersect1.nodes().size());
        assertEquals(0, intersect1.edges().size());

        Graph diff1 = g1.difference(uN1);
        assertFalse(diff1.nodes().contains(uN1));
        assertEquals(0, diff1.nodes().size());
        assertEquals(0, diff1.edges().size());

        Graph union1 = g1.union(uN1);
        assertTrue(union1.nodes().contains(uN1));
        assertEquals(1, union1.nodes().size());
        assertEquals(0, union1.edges().size());

        Graph union1_n2 = g1.union(uN2);
        assertTrue(union1_n2.nodes().contains(uN1));
        assertTrue(union1_n2.nodes().contains(uN2));
        assertEquals(2, union1_n2.nodes().size());
        assertEquals(0, union1_n2.edges().size());

        // Case 3: graph has both nodes and an edge
        Graph g3 = u.asGraph();

        Graph intersect3 = g3.intersection(uN1);
        assertTrue(intersect3.nodes().contains(uN1));
        assertFalse(intersect3.nodes().contains(uN2));
        assertEquals(1, intersect3.nodes().size());
        assertEquals(0, intersect3.edges().size());

        Graph diff3 = g3.difference(uN1);
        assertFalse(diff3.nodes().contains(uN1));
        assertTrue(diff3.nodes().contains(uN2));
        assertTrue(diff3.nodes().contains(uN3));
        assertFalse(diff3.edges().contains(uE));
        assertEquals(2, diff3.nodes().size());
        assertEquals(0, diff3.edges().size());

        Graph union3 = g3.union(uN1);
        assertEquals(3, union3.nodes().size());
        assertEquals(1, union3.edges().size());
    }
}
