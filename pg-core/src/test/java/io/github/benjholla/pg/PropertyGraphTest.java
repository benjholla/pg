package io.github.benjholla.pg;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PropertyGraphTest {

    private PropertyGraph graph;
    private Node a, b, c, d, e, f, g;
    private Edge ab, bc, cb, cd, de, dg;

    @BeforeEach
    public void setUp() {
        graph = new PropertyGraph();

        a = new Node();
        a.putAttr("name", "a");
        b = new Node();
        b.putAttr("name", "b");
        c = new Node();
        c.putAttr("name", "c");
        d = new Node();
        d.putAttr("name", "d");
        e = new Node();
        e.putAttr("name", "e");
        f = new Node();
        f.putAttr("name", "f");
        g = new Node();
        g.putAttr("name", "g");

        ab = new Edge(a, b);
        bc = new Edge(b, c);
        cb = new Edge(c, b);
        cd = new Edge(c, d);
        de = new Edge(d, e);
        dg = new Edge(d, g);

        graph.add(a);
        graph.add(b);
        graph.add(c);
        graph.add(d);
        graph.add(e);
        graph.add(f); // f is added, no edges
        graph.add(g);

        graph.add(ab);
        graph.add(bc);
        graph.add(cb);
        graph.add(cd);
        graph.add(de);
        graph.add(dg);
    }

    @Test
    public void testRoots() {
        NodeSet roots = graph.roots();
        assertEquals(2, roots.size());
        assertTrue(roots.contains(a));
        assertTrue(roots.contains(f));
    }

    @Test
    public void testLeaves() {
        NodeSet leaves = graph.leaves();
        assertEquals(3, leaves.size());
        assertTrue(leaves.contains(e));
        assertTrue(leaves.contains(g));
        assertTrue(leaves.contains(f));
    }

    @Test
    public void testPredecessors() {
        NodeSet predecessorsOfB = graph.predecessors(b);
        assertEquals(2, predecessorsOfB.size());
        assertTrue(predecessorsOfB.contains(a));
        assertTrue(predecessorsOfB.contains(c));
    }

    @Test
    public void testSuccessors() {
        NodeSet successorsOfC = graph.successors(c);
        assertEquals(2, successorsOfC.size());
        assertTrue(successorsOfC.contains(b));
        assertTrue(successorsOfC.contains(d));
    }

    @Test
    public void testForwardStep() {
        Graph forwardStepC = graph.forwardStep(c);
        assertEquals(3, forwardStepC.nodes().size());
        assertTrue(forwardStepC.nodes().contains(c));
        assertTrue(forwardStepC.nodes().contains(b));
        assertTrue(forwardStepC.nodes().contains(d));

        assertEquals(2, forwardStepC.edges().size());
        assertTrue(forwardStepC.edges().contains(cb));
        assertTrue(forwardStepC.edges().contains(cd));
    }

    @Test
    public void testReverseStep() {
        Graph reverseStepC = graph.reverseStep(c);
        assertEquals(2, reverseStepC.nodes().size());
        assertTrue(reverseStepC.nodes().contains(c));
        assertTrue(reverseStepC.nodes().contains(b));

        assertEquals(1, reverseStepC.edges().size());
        assertTrue(reverseStepC.edges().contains(bc));
    }

    @Test
    public void testForward() {
        Graph forwardD = graph.forward(d);
        assertEquals(3, forwardD.nodes().size());
        assertTrue(forwardD.nodes().contains(d));
        assertTrue(forwardD.nodes().contains(e));
        assertTrue(forwardD.nodes().contains(g));

        assertEquals(2, forwardD.edges().size());
        assertTrue(forwardD.edges().contains(de));
        assertTrue(forwardD.edges().contains(dg));
    }

    @Test
    public void testReverse() {
        Graph reverseD = graph.reverse(d);
        assertEquals(4, reverseD.nodes().size());
        assertTrue(reverseD.nodes().contains(a));
        assertTrue(reverseD.nodes().contains(b));
        assertTrue(reverseD.nodes().contains(c));
        assertTrue(reverseD.nodes().contains(d));

        assertEquals(4, reverseD.edges().size());
        assertTrue(reverseD.edges().contains(ab));
        assertTrue(reverseD.edges().contains(bc));
        assertTrue(reverseD.edges().contains(cb));
        assertTrue(reverseD.edges().contains(cd));
    }

    @Test
    public void testBetweenStep() {
        Graph betweenStepCB = graph.betweenStep(c, b);
        assertEquals(2, betweenStepCB.nodes().size());
        assertTrue(betweenStepCB.nodes().contains(c));
        assertTrue(betweenStepCB.nodes().contains(b));

        assertEquals(1, betweenStepCB.edges().size());
        assertTrue(betweenStepCB.edges().contains(cb));
    }

    @Test
    public void testBetween() {
        Graph betweenAD = graph.between(a, d);
        assertEquals(4, betweenAD.nodes().size());
        assertTrue(betweenAD.nodes().contains(a));
        assertTrue(betweenAD.nodes().contains(b));
        assertTrue(betweenAD.nodes().contains(c));
        assertTrue(betweenAD.nodes().contains(d));

        assertEquals(4, betweenAD.edges().size());
        assertTrue(betweenAD.edges().contains(ab));
        assertTrue(betweenAD.edges().contains(bc));
        assertTrue(betweenAD.edges().contains(cb));
        assertTrue(betweenAD.edges().contains(cd));
    }
}
