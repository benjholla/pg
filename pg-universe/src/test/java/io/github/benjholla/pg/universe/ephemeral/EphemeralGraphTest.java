package io.github.benjholla.pg.universe.ephemeral;

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
import io.github.benjholla.pg.api.GraphElement;
import io.github.benjholla.pg.api.Node;
import io.github.benjholla.pg.api.NodeSet;

public class EphemeralGraphTest {

    private EphemeralGraph graph;
    private Node a, b, c, d, e, f, g;
    private Edge ab, bc, cb, cd, de, dg;

    @BeforeEach
    public void setUp() {
        graph = new EphemeralGraph();

        a = new EphemeralNode();
        a.attributes().put("name", "a");
        a.tags().add("vowel");

        b = new EphemeralNode();
        b.attributes().put("name", "b");
        b.tags().add("consonant");
        b.tags().add("letter");

        c = new EphemeralNode();
        c.attributes().put("name", "c");
        c.tags().add("consonant");
        c.tags().add("letter");

        d = new EphemeralNode();
        d.attributes().put("name", "d");
        d.tags().add("consonant");

        e = new EphemeralNode();
        e.attributes().put("name", "e");
        e.tags().add("vowel");

        f = new EphemeralNode();
        f.attributes().put("name", "f");
        f.tags().add("consonant");

        g = new EphemeralNode();
        g.attributes().put("name", "g");
        g.tags().add("consonant");

        ab = new EphemeralEdge(a, b);
        ab.attributes().put("weight", 1);
        ab.tags().add("path");

        bc = new EphemeralEdge(b, c);
        bc.attributes().put("weight", 2);
        bc.tags().add("path");
        bc.tags().add("main");

        cb = new EphemeralEdge(c, b);
        cb.attributes().put("weight", 3);
        cb.tags().add("back");

        cd = new EphemeralEdge(c, d);
        cd.attributes().put("weight", 4);
        cd.tags().add("path");

        de = new EphemeralEdge(d, e);
        de.attributes().put("weight", 5);
        de.tags().add("path");

        dg = new EphemeralEdge(d, g);
        dg.attributes().put("weight", 6);
        dg.tags().add("branch");

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
    public void testAddNull() {
        assertThrows(NullPointerException.class, () -> graph.add((GraphElement) null));
    }

    @Test
    public void testConstructors() {
        EphemeralGraph empty = new EphemeralGraph();
        assertTrue(empty.isEmpty());

        EphemeralGraph withNodes = new EphemeralGraph(a, b);
        assertEquals(2, withNodes.nodes().size());

        EphemeralGraph withNodeSet = new EphemeralGraph(new EphemeralNodeSet(a, b));
        assertEquals(2, withNodeSet.nodes().size());

        EphemeralGraph withEdges = new EphemeralGraph(ab, bc);
        assertEquals(3, withEdges.nodes().size());
        assertEquals(2, withEdges.edges().size());

        EphemeralGraph withEdgeSet = new EphemeralGraph(new EphemeralEdgeSet(ab, bc));
        assertEquals(3, withEdgeSet.nodes().size());
        assertEquals(2, withEdgeSet.edges().size());

        EphemeralGraph withSets = new EphemeralGraph(new EphemeralNodeSet(a, b, c), new EphemeralEdgeSet(ab, bc));
        assertEquals(3, withSets.nodes().size());
        assertEquals(2, withSets.edges().size());

        EphemeralGraph withGraphs = new EphemeralGraph(withNodes, withEdges);
        assertEquals(3, withGraphs.nodes().size());
        assertEquals(2, withGraphs.edges().size());

        EphemeralGraph withGraphsColl = new EphemeralGraph(java.util.Arrays.asList(withNodes, withEdges));
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
        EphemeralGraph g1 = new EphemeralGraph(a, b);
        EphemeralGraph g2 = new EphemeralGraph(c, d);

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
        EphemeralGraph sub = new EphemeralGraph(a, b, c);
        sub.add(ab);
        sub.add(bc);

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

        EphemeralGraph g2 = new EphemeralGraph(c);
        Graph diffGraph = sub.difference(g2);
        assertEquals(2, diffGraph.nodes().size());
        assertTrue(diffGraph.nodes().contains(a));
        assertTrue(diffGraph.nodes().contains(b));
        assertEquals(1, diffGraph.edges().size());
        assertTrue(diffGraph.edges().contains(ab));
    }

    @Test
    public void testDifferenceEdges() {
        EphemeralGraph sub = new EphemeralGraph(a, b, c);
        sub.add(ab);
        sub.add(bc);

        Graph diffE = sub.differenceEdges(ab);
        assertEquals(3, diffE.nodes().size()); // nodes not removed
        assertEquals(1, diffE.edges().size());
        assertTrue(diffE.edges().contains(bc));

        EphemeralGraph g2 = new EphemeralGraph(bc);
        Graph diffG = sub.differenceEdges(g2);
        assertEquals(3, diffG.nodes().size());
        assertEquals(1, diffG.edges().size());
        assertTrue(diffG.edges().contains(ab));
    }

    @Test
    public void testIntersection() {
        EphemeralGraph g1 = new EphemeralGraph(a, b, c);
        g1.add(ab);
        g1.add(bc);

        EphemeralGraph g2 = new EphemeralGraph(b, c, d);
        g2.add(bc);
        g2.add(cd);

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
        EphemeralGraph g1 = new EphemeralGraph(a, b, c); // nodes only

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
        NodeSet weighted = graph.selectNodes("name");
        assertEquals(7, weighted.size());

        NodeSet bNode = graph.selectNodes("name", new AttributeValue.StringVal("b"));
        assertEquals(1, bNode.size());
        assertTrue(bNode.contains(b));

        EdgeSet weightedEdges = graph.selectEdges("weight");
        assertEquals(6, weightedEdges.size());

        EdgeSet w1 = graph.selectEdges("weight", new AttributeValue.IntVal(1), new AttributeValue.IntVal(3));
        assertEquals(2, w1.size());
        assertTrue(w1.contains(ab));
        assertTrue(w1.contains(cb));
    }

    @Test
    public void testTags() {
        NodeSet vowels = graph.nodes("vowel");
        assertEquals(2, vowels.size());
        assertTrue(vowels.contains(a));
        assertTrue(vowels.contains(e));

        NodeSet any = graph.nodesTaggedWithAny("vowel", "letter");
        assertEquals(4, any.size()); // a, e, b, c

        NodeSet all = graph.nodesTaggedWithAll("consonant", "letter");
        assertEquals(2, all.size()); // b, c

        EdgeSet paths = graph.edges("path");
        assertEquals(4, paths.size());

        EdgeSet anyE = graph.edgesTaggedWithAny("main", "back");
        assertEquals(2, anyE.size()); // bc, cb

        EdgeSet allE = graph.edgesTaggedWithAll("path", "main");
        assertEquals(1, allE.size()); // bc
    }
}
