package dev.chpg.pg.multiverse.universe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.AttributeValue;
import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.multiverse.ephemeral.EphemeralFactory;
import dev.chpg.pg.multiverse.ephemeral.EphemeralGraph;

public class UniverseGraphTest {
    private Universe universe;
    private Graph graph;
    private Node a, b, c, d, e, f, g;
    private Edge ab, bc, cb, cd, de, dg;

    @BeforeEach
    public void setUp() {
        universe = new Universe();
        EphemeralFactory factory = new EphemeralGraph().factory();
        EphemeralGraph ephemeralGraph = (EphemeralGraph) factory.createGraph();

        Node ea = factory.createNode();
        ea.attributes().put("name", "a");
        ea.tags().add("vowel");

        Node eb = factory.createNode();
        eb.attributes().put("name", "b");
        eb.tags().add("consonant");
        eb.tags().add("letter");

        Node ec = factory.createNode();
        ec.attributes().put("name", "c");
        ec.tags().add("consonant");
        ec.tags().add("letter");

        Node ed = factory.createNode();
        ed.attributes().put("name", "d");
        ed.tags().add("consonant");

        Node ee = factory.createNode();
        ee.attributes().put("name", "e");
        ee.tags().add("vowel");

        Node ef = factory.createNode();
        ef.attributes().put("name", "f");
        ef.tags().add("consonant");

        Node eg = factory.createNode();
        eg.attributes().put("name", "g");
        eg.tags().add("consonant");

        Edge eab = factory.createEdge(ea, eb);
        eab.attributes().put("weight", 1);
        eab.tags().add("path");

        Edge ebc = factory.createEdge(eb, ec);
        ebc.attributes().put("weight", 2);
        ebc.tags().add("path");
        ebc.tags().add("main");

        Edge ecb = factory.createEdge(ec, eb);
        ecb.attributes().put("weight", 3);
        ecb.tags().add("back");

        Edge ecd = factory.createEdge(ec, ed);
        ecd.attributes().put("weight", 4);
        ecd.tags().add("path");

        Edge ede = factory.createEdge(ed, ee);
        ede.attributes().put("weight", 5);
        ede.tags().add("path");

        Edge edg = factory.createEdge(ed, eg);
        edg.attributes().put("weight", 6);
        edg.tags().add("branch");

        ephemeralGraph.addNode(ea);
        ephemeralGraph.addNode(eb);
        ephemeralGraph.addNode(ec);
        ephemeralGraph.addNode(ed);
        ephemeralGraph.addNode(ee);
        ephemeralGraph.addNode(ef);
        ephemeralGraph.addNode(eg);

        ephemeralGraph.addEdge(eab);
        ephemeralGraph.addEdge(ebc);
        ephemeralGraph.addEdge(ecb);
        ephemeralGraph.addEdge(ecd);
        ephemeralGraph.addEdge(ede);
        ephemeralGraph.addEdge(edg);

        this.graph = universe.promote(ephemeralGraph);

        this.a = findNodeByName("a");
        this.b = findNodeByName("b");
        this.c = findNodeByName("c");
        this.d = findNodeByName("d");
        this.e = findNodeByName("e");
        this.f = findNodeByName("f");
        this.g = findNodeByName("g");

        this.ab = findEdge(this.a, this.b);
        this.bc = findEdge(this.b, this.c);
        this.cb = findEdge(this.c, this.b);
        this.cd = findEdge(this.c, this.d);
        this.de = findEdge(this.d, this.e);
        this.dg = findEdge(this.d, this.g);
    }

    private Node findNodeByName(String name) {
        return this.graph.nodes().withAttribute("name", AttributeValue.value(name)).iterator().next();
    }

    private Edge findEdge(Node from, Node to) {
        for (Edge edge : this.graph.edges()) {
            if (edge.from().equals(from) && edge.to().equals(to)) {
                return edge;
            }
        }
        throw new IllegalStateException("Edge not found between " + from + " and " + to);
    }

    @Test
    public void testAddNull() {
        assertThrows(IllegalArgumentException.class, () -> graph.addNode(null));
        assertThrows(IllegalArgumentException.class, () -> graph.addEdge(null));
    }
    @Test
    public void testRemoveNode() {
        assertTrue(graph.nodes().contains(a));
        assertTrue(graph.edges().contains(ab));

        boolean removed = graph.removeNode(a);
        assertTrue(removed);

        assertFalse(graph.nodes().contains(a));
        // Removing a node should cascade and remove connected edges
        assertFalse(graph.edges().contains(ab));

        boolean removedAgain = graph.removeNode(a);
        assertFalse(removedAgain);
    }

    @Test
    public void testRemoveEdge() {
        assertTrue(graph.edges().contains(bc));

        boolean removed = graph.removeEdge(bc);
        assertTrue(removed);

        assertFalse(graph.edges().contains(bc));
        // Node b and c should still exist
        assertTrue(graph.nodes().contains(b));
        assertTrue(graph.nodes().contains(c));
    }

    @Test
    public void testDifference() {
        // Based on EphemeralGraphTest's testDifference
        Graph diffNodes = graph.difference(a);

        assertEquals(6, diffNodes.nodes().size()); // 7 - 1 (a)
        assertFalse(diffNodes.nodes().contains(a));
        assertTrue(diffNodes.nodes().contains(b));

        assertEquals(5, diffNodes.edges().size()); // 6 - 1 (ab)
        assertFalse(diffNodes.edges().contains(ab)); // ab removed because a is removed
        assertTrue(diffNodes.edges().contains(bc));
    }

    @Test
    public void testRoots() {
        dev.chpg.pg.api.NodeSet roots = graph.roots();
        assertEquals(2, roots.size());
        assertTrue(roots.contains(a));
        assertTrue(roots.contains(f));
    }

    @Test
    public void testLeaves() {
        dev.chpg.pg.api.NodeSet leaves = graph.leaves();
        assertEquals(3, leaves.size());
        assertTrue(leaves.contains(e));
        assertTrue(leaves.contains(g));
        assertTrue(leaves.contains(f));
    }
}
