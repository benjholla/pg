package dev.chpg.pg.multiverse.ephemeral;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

import dev.chpg.pg.api.NodeSet;
import dev.chpg.pg.api.AttributeValue;
import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.multiverse.universe.Universe;

public class ShadowGraphTest {
    private Universe universe;
    private Graph universeGraph;

    // Universe elements
    private Node uNodeA, uNodeB, uNodeC;
    private Edge uEdgeAB, uEdgeBC;

    // Ephemeral setup
    private EphemeralFactory factory;
    private EphemeralGraph shadowGraph;

    @BeforeEach
    public void setUp() {
        universe = new Universe();

        // Setup initial universe state
        EphemeralFactory initFactory = new EphemeralGraph(universe).factory();
        EphemeralGraph initGraph = (EphemeralGraph) initFactory.createGraph();

        Node a = initFactory.createNode();
        a.attributes().put("name", "A");
        a.tags().add("universe_tag");

        Node b = initFactory.createNode();
        b.attributes().put("name", "B");

        Node c = initFactory.createNode();
        c.attributes().put("name", "C");

        Edge ab = initFactory.createEdge(a, b);
        ab.attributes().put("weight", 10);
        ab.tags().add("initial_edge");

        Edge bc = initFactory.createEdge(b, c);

        initGraph.addNode(a);
        initGraph.addNode(b);
        initGraph.addNode(c);
        initGraph.addEdge(ab);
        initGraph.addEdge(bc);

        universeGraph = universe.promote(initGraph);

        uNodeA = findNodeByName("A", universeGraph);
        uNodeB = findNodeByName("B", universeGraph);
        uNodeC = findNodeByName("C", universeGraph);

        uEdgeAB = findEdge(uNodeA, uNodeB, universeGraph);
        uEdgeBC = findEdge(uNodeB, uNodeC, universeGraph);

        // Setup shadow graph
        shadowGraph = new EphemeralGraph(universe);
        factory = shadowGraph.factory();
    }

    private Node findNodeByName(String name, Graph graph) {
        return graph.nodes().withAttribute("name", AttributeValue.value(name)).iterator().next();
    }

    private Edge findEdge(Node from, Node to, Graph graph) {
        for (Edge edge : graph.edges()) {
            if (edge.from().equals(from) && edge.to().equals(to)) {
                return edge;
            }
        }
        throw new IllegalStateException("Edge not found");
    }

    @Test
    public void testSetup() {
        assertEquals(3, universeGraph.nodes().size());
        assertEquals(2, universeGraph.edges().size());
        assertEquals(3, shadowGraph.nodes().size());
        assertEquals(2, shadowGraph.edges().size());
    }

    @Test

    public void testShadowTags() {
        // Add universe node to shadow graph (this simulates a read/write in ephemeral context)
        Graph shadow = shadowGraph.union(uNodeA);
        Node fetchedNodeA = shadow.node(uNodeA.id()).get();

        fetchedNodeA.tags().add("shadow_tag");

        // Assert on the shadow node directly
        assertTrue(fetchedNodeA.tags().contains("shadow_tag"));
        assertTrue(fetchedNodeA.tags().contains("universe_tag"));

        // Verify changes are in shadow but not universe
        assertFalse(uNodeA.tags().contains("shadow_tag"));

        // Remove universe tag in shadow
        fetchedNodeA.tags().remove("universe_tag");
        assertFalse(fetchedNodeA.tags().contains("universe_tag"));

        // Double check universe remains unmodified
        assertTrue(uNodeA.tags().contains("universe_tag"));
    }

    @Test

    public void testShadowAttributes() {
        Graph shadow = shadowGraph.union(uNodeA);
        Node fetchedNodeA = shadow.node(uNodeA.id()).get();

        fetchedNodeA.attributes().put("shadow_attr", AttributeValue.value(100));

        assertTrue(fetchedNodeA.attributes().containsKey("shadow_attr"));
        assertEquals(AttributeValue.value(100), fetchedNodeA.attributes().get("shadow_attr"));
        assertTrue(fetchedNodeA.attributes().containsKey("name"));

        assertFalse(uNodeA.attributes().containsKey("shadow_attr"));

        fetchedNodeA.attributes().remove("name");
        assertFalse(fetchedNodeA.attributes().containsKey("name"));

        assertTrue(uNodeA.attributes().containsKey("name"));
    }


    @Test

    public void testHybridTopologies() {
        // Adding a new ephemeral node
        Node d = factory.createNode();
        d.attributes().put("name", "D");

        // Connect ephemeral node D to universe node C
        Edge cd = factory.createEdge(uNodeC, d);
        cd.tags().add("hybrid_edge");

        Graph hybridGraph = shadowGraph.union(d).union(cd);

        assertEquals(4, hybridGraph.nodes().size()); // 3 baseline + 1 local addition
        // Wait, if cd is added, uNodeC is also added to the hybridGraph due to auto-vivify terminal nodes
        assertTrue(hybridGraph.nodes().contains(d));
        assertTrue(hybridGraph.nodes().contains(uNodeC));

        assertTrue(hybridGraph.edges().contains(cd));

        // Assert we can traverse
        NodeSet successorsOfC = hybridGraph.successors(uNodeC);
        assertTrue(successorsOfC.contains(d));
    }



    @Test

    public void testTombstoningAndUndeleting() {
        // Tombstone universe node A in shadow graph
        Graph withA = shadowGraph.union(uNodeA);
        assertTrue(withA.nodes().contains(uNodeA));

        Graph tombstonedA = withA.difference(uNodeA);
        assertFalse(tombstonedA.nodes().contains(uNodeA));

        // In the original universe graph, A should still exist
        assertTrue(universeGraph.nodes().contains(uNodeA));

        // Now undelete (add back before promotion)
        Graph undeletedA = tombstonedA.union(uNodeA);
        assertTrue(undeletedA.nodes().contains(uNodeA));

        // Test edge tombstoning
        Graph withEdge = shadowGraph.union(uEdgeAB);
        assertTrue(withEdge.edges().contains(uEdgeAB));

        Graph tombstonedEdge = withEdge.differenceEdges(uEdgeAB);
        assertFalse(tombstonedEdge.edges().contains(uEdgeAB));
        assertTrue(universeGraph.edges().contains(uEdgeAB));

        Graph undeletedEdge = tombstonedEdge.union(uEdgeAB);
        assertTrue(undeletedEdge.edges().contains(uEdgeAB));
    }



    @Test

    public void testPromotionValidation() {
        // Build a shadow graph with all the modifications
        Graph shadow = shadowGraph.union(uNodeA).union(uNodeB).union(uEdgeAB);

        // Shadow tag modification
        Node fetchedNodeA = shadow.node(uNodeA.id()).get();
        fetchedNodeA.tags().add("promoted_tag");

        // Tombstoning
        shadow = shadow.difference(uNodeB); // this should also remove uEdgeAB in the shadow graph

        // Hybrid addition
        Node ephemeralNode = factory.createNode();
        ephemeralNode.tags().add("new_ephemeral");
        Edge hybridEdge = factory.createEdge(uNodeA, ephemeralNode);
        shadow = shadow.union(ephemeralNode).union(hybridEdge);

        // Promote the shadow graph
        Graph newUniverseGraph = universe.promote((EphemeralGraph) shadow);

        // Validate promotion
        // A should have the new tag
        Node promotedA = findNodeByName("A", newUniverseGraph);
        assertTrue(promotedA.tags().contains("promoted_tag"));
        assertTrue(promotedA.tags().contains("universe_tag"));

        // B and AB should be tombstoned (removed)
        boolean bFound = false;
        for (Node n : newUniverseGraph.nodes()) {
            AttributeValue nameAttr = n.attributes().get("name");
            if (nameAttr != null && "B".equals(((AttributeValue.StringValue) nameAttr).value())) {
                bFound = true;
                break;
            }
        }
        assertFalse(bFound, "Node B should have been tombstoned and not present in the promoted graph");

        // The new ephemeral node and edge should exist
        boolean ephemeralFound = false;
        for (Node n : newUniverseGraph.nodes()) {
            if (n.tags().contains("new_ephemeral")) {
                ephemeralFound = true;
                break;
            }
        }
        assertTrue(ephemeralFound, "The new ephemeral node should have been promoted");
    }

}
