package io.github.benjholla.pg.global;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.benjholla.pg.api.AttributeValue;
import io.github.benjholla.pg.api.Edge;
import io.github.benjholla.pg.api.EdgeSet;
import io.github.benjholla.pg.api.Graph;
import io.github.benjholla.pg.api.Node;
import io.github.benjholla.pg.api.NodeSet;

public class GlobalGraphTest {

    private GlobalGraph graph;
    private Node a, b, c, d, e, f, g;
    private Edge ab, bc, cb, cd, de, dg;

    @BeforeEach
    public void setUp() {
        graph = new GlobalGraph();

        a = new GlobalNode();
        a.attributes().put("name", "a");
        a.tags().add("vowel");

        b = new GlobalNode();
        b.attributes().put("name", "b");
        b.tags().add("consonant");
        b.tags().add("letter");

        c = new GlobalNode();
        c.attributes().put("name", "c");
        c.tags().add("consonant");
        c.tags().add("letter");

        d = new GlobalNode();
        d.attributes().put("name", "d");
        d.tags().add("consonant");

        e = new GlobalNode();
        e.attributes().put("name", "e");
        e.tags().add("vowel");

        f = new GlobalNode();
        f.attributes().put("name", "f");
        f.tags().add("consonant");

        g = new GlobalNode();
        g.attributes().put("name", "g");
        g.tags().add("consonant");

        ab = new GlobalEdge(a, b);
        ab.attributes().put("weight", 1);
        ab.tags().add("path");

        bc = new GlobalEdge(b, c);
        bc.attributes().put("weight", 2);
        bc.tags().add("path");
        bc.tags().add("main");

        cb = new GlobalEdge(c, b);
        cb.attributes().put("weight", 3);
        cb.tags().add("back");

        cd = new GlobalEdge(c, d);
        cd.attributes().put("weight", 4);
        cd.tags().add("path");

        de = new GlobalEdge(d, e);
        de.attributes().put("weight", 5);
        de.tags().add("path");

        dg = new GlobalEdge(d, g);
        dg.attributes().put("weight", 6);
        dg.tags().add("branch");

        graph.addNode(a);
        graph.addNode(b);
        graph.addNode(c);
        graph.addNode(d);
        graph.addNode(e);
        graph.addNode(f); // f is added, no edges
        graph.addNode(g);

        graph.addEdge(ab);
        graph.addEdge(bc);
        graph.addEdge(cb);
        graph.addEdge(cd);
        graph.addEdge(de);
        graph.addEdge(dg);
    }

    @Test
    public void testAddNull() {
        assertThrows(NullPointerException.class, () -> graph.addNode((Node) null));
        assertThrows(NullPointerException.class, () -> graph.addEdge((Edge) null));
    }

    @Test
    public void testConstructors() {
        GlobalGraph empty = new GlobalGraph();
        assertTrue(empty.isEmpty());

        GlobalGraph withNodes = new GlobalGraph(a, b);
        assertEquals(2, withNodes.nodes().size());

        GlobalGraph withNodeSet = new GlobalGraph(new GlobalNodeSet(a, b));
        assertEquals(2, withNodeSet.nodes().size());

        GlobalGraph withEdges = new GlobalGraph(ab, bc);
        assertEquals(3, withEdges.nodes().size());
        assertEquals(2, withEdges.edges().size());

        GlobalGraph withEdgeSet = new GlobalGraph(new GlobalEdgeSet(ab, bc));
        assertEquals(3, withEdgeSet.nodes().size());
        assertEquals(2, withEdgeSet.edges().size());

        GlobalGraph withSets = new GlobalGraph(new GlobalNodeSet(a, b, c), new GlobalEdgeSet(ab, bc));
        assertEquals(3, withSets.nodes().size());
        assertEquals(2, withSets.edges().size());

        GlobalGraph withGraphs = new GlobalGraph(withNodes, withEdges);
        assertEquals(3, withGraphs.nodes().size());
        assertEquals(2, withGraphs.edges().size());

        GlobalGraph withGraphsColl = new GlobalGraph(java.util.Arrays.asList(withNodes, withEdges));
        assertEquals(3, withGraphsColl.nodes().size());
        assertEquals(2, withGraphsColl.edges().size());
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

        Graph emptyBetween = graph.betweenStep(a, e);
        assertTrue(emptyBetween.isEmpty());
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

    @Test
    public void testUnion() {
        GlobalGraph g1 = new GlobalGraph(a, b);
        GlobalGraph g2 = new GlobalGraph(c, d);

        Graph union = g1.union(g2);
        assertEquals(4, union.nodes().size());

        Graph unionNodes = g1.union(e, f);
        assertEquals(4, unionNodes.nodes().size());

        Graph unionEdges = g1.union(cd);
        assertEquals(4, unionEdges.nodes().size());
        assertEquals(1, unionEdges.edges().size());
    }

    @Test
    public void testDifference() {
        GlobalGraph sub = new GlobalGraph(a, b, c);
        sub.addEdge(ab);
        sub.addEdge(bc);

        Graph diffNodes = sub.difference(a);
        assertEquals(2, diffNodes.nodes().size());
        assertTrue(diffNodes.nodes().contains(b));
        assertTrue(diffNodes.nodes().contains(c));
        assertEquals(1, diffNodes.edges().size());
        assertTrue(diffNodes.edges().contains(bc)); // ab removed because a is removed

        Graph diffEdges = sub.difference(bc);
        assertEquals(1, diffEdges.nodes().size());
        assertTrue(diffEdges.nodes().contains(a));
        assertEquals(0, diffEdges.edges().size());

        GlobalGraph g2 = new GlobalGraph(c);
        Graph diffGraph = sub.difference(g2);
        assertEquals(2, diffGraph.nodes().size());
        assertTrue(diffGraph.nodes().contains(a));
        assertTrue(diffGraph.nodes().contains(b));
        assertEquals(1, diffGraph.edges().size());
        assertTrue(diffGraph.edges().contains(ab));
    }

    @Test
    public void testDifferenceEdges() {
        GlobalGraph sub = new GlobalGraph(a, b, c);
        sub.addEdge(ab);
        sub.addEdge(bc);

        Graph diffE = sub.differenceEdges(ab);
        assertEquals(3, diffE.nodes().size()); // nodes not removed
        assertEquals(1, diffE.edges().size());
        assertTrue(diffE.edges().contains(bc));

        GlobalGraph g2 = new GlobalGraph(bc);
        Graph diffG = sub.differenceEdges(g2);
        assertEquals(3, diffG.nodes().size());
        assertEquals(1, diffG.edges().size());
        assertTrue(diffG.edges().contains(ab));
    }

    @Test
    public void testIntersection() {
        GlobalGraph g1 = new GlobalGraph(a, b, c);
        g1.addEdge(ab);
        g1.addEdge(bc);

        GlobalGraph g2 = new GlobalGraph(b, c, d);
        g2.addEdge(bc);
        g2.addEdge(cd);

        Graph intersect = g1.intersection(g2);
        assertEquals(2, intersect.nodes().size());
        assertTrue(intersect.nodes().contains(b));
        assertTrue(intersect.nodes().contains(c));

        assertEquals(1, intersect.edges().size());
        assertTrue(intersect.edges().contains(bc));

        Graph intersectNodes = g1.intersection(b, c);
        assertEquals(2, intersectNodes.nodes().size());

        Graph intersectEdges = g1.intersection(bc);
        assertEquals(2, intersectEdges.nodes().size());
        assertEquals(1, intersectEdges.edges().size());
    }

    @Test
    public void testInduce() {
        GlobalGraph g1 = new GlobalGraph(a, b, c); // nodes only

        Graph induced = g1.induce(ab, bc, cd);
        assertEquals(3, induced.nodes().size());
        assertEquals(2, induced.edges().size());
        assertTrue(induced.edges().contains(ab));
        assertTrue(induced.edges().contains(bc));
        assertFalse(induced.edges().contains(cd)); // d is not in g1

        Graph inducedGraph = g1.induce(graph);
        assertEquals(3, inducedGraph.nodes().size());
        assertEquals(3, inducedGraph.edges().size());
        assertTrue(inducedGraph.edges().contains(ab));
        assertTrue(inducedGraph.edges().contains(bc));
        assertTrue(inducedGraph.edges().contains(cb));
    }

    @Test
    public void testSelectNodesAndEdges() {
        NodeSet weighted = graph.nodes().filter("name");
        assertEquals(7, weighted.size());

        NodeSet bNode = graph.nodes().filter("name", AttributeValue.value("b"));
        assertEquals(1, bNode.size());
        assertTrue(bNode.contains(b));

        EdgeSet weightedEdges = graph.edges().filter("weight");
        assertEquals(6, weightedEdges.size());

        EdgeSet w1 = graph.edges().filter("weight", AttributeValue.value(1), AttributeValue.value(3));
        assertEquals(2, w1.size());
        assertTrue(w1.contains(ab));
        assertTrue(w1.contains(cb));
    }

    @Test
    public void testTags() {
        NodeSet vowels = graph.nodes().taggedWithAny("vowel");
        assertEquals(2, vowels.size());
        assertTrue(vowels.contains(a));
        assertTrue(vowels.contains(e));

        NodeSet any = graph.nodes().taggedWithAny("vowel", "letter");
        assertEquals(4, any.size()); // a, e, b, c

        NodeSet all = graph.nodes().taggedWithAll("consonant", "letter");
        assertEquals(2, all.size()); // b, c

        EdgeSet paths = graph.edges().taggedWithAny("path");
        assertEquals(4, paths.size());

        EdgeSet anyE = graph.edges().taggedWithAny("main", "back");
        assertEquals(2, anyE.size()); // bc, cb

        EdgeSet allE = graph.edges().taggedWithAll("path", "main");
        assertEquals(1, allE.size()); // bc
    }
}
