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
        Graph empty = graph.difference(graph);
        Graph sub = empty.union(a).union(b).union(c);
        sub = sub.union(ab).union(bc);

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

        Graph g2 = empty.union(c);
        Graph diffGraph = sub.difference(g2);
        assertEquals(2, diffGraph.nodes().size());
        assertTrue(diffGraph.nodes().contains(a));
        assertTrue(diffGraph.nodes().contains(b));
        assertEquals(1, diffGraph.edges().size());
        assertTrue(diffGraph.edges().contains(ab));
    }

    @Test
    public void testUnion() {
        Graph empty = graph.difference(graph);
        Graph g1 = empty.union(a).union(b);
        Graph g2 = empty.union(c).union(d);

        Graph union = g1.union(g2);
        assertEquals(4, union.nodes().size());

        Graph unionNodes = g1.union(e).union(f);
        assertEquals(4, unionNodes.nodes().size());

        Graph unionEdges = g1.union(cd);
        assertEquals(4, unionEdges.nodes().size());
        assertEquals(1, unionEdges.edges().size());
    }

    @Test
    public void testDifferenceEdges() {
        Graph empty = graph.difference(graph);
        Graph sub = empty.union(a).union(b).union(c);
        sub = sub.union(ab).union(bc);

        Graph diffE = sub.differenceEdges(ab);
        assertEquals(3, diffE.nodes().size()); // nodes not removed
        assertEquals(1, diffE.edges().size());
        assertTrue(diffE.edges().contains(bc));

        Graph g2 = empty.union(bc);
        Graph diffG = sub.differenceEdges(g2);
        assertEquals(3, diffG.nodes().size());
        assertEquals(1, diffG.edges().size());
        assertTrue(diffG.edges().contains(ab));
    }

    @Test
    public void testIntersection() {
        Graph empty = graph.difference(graph);
        Graph g1 = empty.union(a).union(b).union(c);
        g1 = g1.union(ab).union(bc);

        Graph g2 = empty.union(b).union(c).union(d);
        g2 = g2.union(bc).union(cd);

        Graph intersect = g1.intersection(g2);
        assertEquals(2, intersect.nodes().size());
        assertTrue(intersect.nodes().contains(b));
        assertTrue(intersect.nodes().contains(c));

        assertEquals(1, intersect.edges().size());
        assertTrue(intersect.edges().contains(bc));

        Graph intersectNodes = g1.intersection(empty.union(b).union(c));
        assertEquals(2, intersectNodes.nodes().size());

        Graph intersectEdges = g1.intersection(bc);
        assertEquals(2, intersectEdges.nodes().size());
        assertEquals(1, intersectEdges.edges().size());
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

    @Test
    public void testAdjacent() {
        assertTrue(graph.adjacent(a, b));
        assertTrue(graph.adjacent(b, c));
        assertTrue(graph.adjacent(c, b)); // There is an edge c->b

        assertFalse(graph.adjacent(b, a));
        assertFalse(graph.adjacent(a, f));
        assertFalse(graph.adjacent(f, a));
    }

    @Test
    public void testDegree() {
        // Node b has 1 in (from a), 1 in (from c), 1 out (to c). Total IN = 2, OUT = 1, BOTH = 3.
        assertEquals(2, graph.degree(b, dev.chpg.pg.api.Node.NodeDirection.IN));
        assertEquals(1, graph.degree(b, dev.chpg.pg.api.Node.NodeDirection.OUT));
        assertEquals(3, graph.degree(b, dev.chpg.pg.api.Node.NodeDirection.BOTH));

        // Node f is isolated
        assertEquals(0, graph.degree(f, dev.chpg.pg.api.Node.NodeDirection.IN));
        assertEquals(0, graph.degree(f, dev.chpg.pg.api.Node.NodeDirection.OUT));
        assertEquals(0, graph.degree(f, dev.chpg.pg.api.Node.NodeDirection.BOTH));
    }

    @Test
    public void testPredecessors() {
        // Node b has predecessors a and c
        dev.chpg.pg.api.NodeSet predsB = graph.predecessors(b);
        assertEquals(2, predsB.size());
        assertTrue(predsB.contains(a));
        assertTrue(predsB.contains(c));

        // Empty graph
        Graph empty = graph.difference(graph);
        dev.chpg.pg.api.NodeSet emptyPreds = graph.predecessors(empty);
        assertEquals(0, emptyPreds.size());

        // NodeSet
        Graph fromSet = empty.union(c).union(d);
        dev.chpg.pg.api.NodeSet setPreds = graph.predecessors(fromSet.nodes());
        assertEquals(2, setPreds.size());
        assertTrue(setPreds.contains(b)); // b -> c
        assertTrue(setPreds.contains(c)); // c -> d
    }

    @Test
    public void testSuccessors() {
        // Node c has successors b and d
        dev.chpg.pg.api.NodeSet succsC = graph.successors(c);
        assertEquals(2, succsC.size());
        assertTrue(succsC.contains(b));
        assertTrue(succsC.contains(d));

        // Empty graph
        Graph empty = graph.difference(graph);
        dev.chpg.pg.api.NodeSet emptySuccs = graph.successors(empty);
        assertEquals(0, emptySuccs.size());

        // NodeSet
        Graph fromSet = empty.union(b).union(d);
        dev.chpg.pg.api.NodeSet setSuccs = graph.successors(fromSet.nodes());
        assertEquals(3, setSuccs.size());
        assertTrue(setSuccs.contains(c)); // b -> c
        assertTrue(setSuccs.contains(e)); // d -> e
        assertTrue(setSuccs.contains(g)); // d -> g
    }

    @Test
    public void testForwardStep() {
        Graph fwdA = graph.forwardStep(a);
        assertEquals(2, fwdA.nodes().size());
        assertTrue(fwdA.nodes().contains(a));
        assertTrue(fwdA.nodes().contains(b));
        assertEquals(1, fwdA.edges().size());
        assertTrue(fwdA.edges().contains(ab));

        Graph empty = graph.difference(graph);
        Graph fromSet = empty.union(c).union(d);
        Graph fwdSet = graph.forwardStep(fromSet.nodes());
        assertEquals(5, fwdSet.nodes().size()); // c, d, b, e, g
        assertTrue(fwdSet.nodes().contains(c));
        assertTrue(fwdSet.nodes().contains(b));
        assertTrue(fwdSet.nodes().contains(d));
        assertTrue(fwdSet.nodes().contains(e));
        assertTrue(fwdSet.nodes().contains(g));
        assertEquals(4, fwdSet.edges().size());
        assertTrue(fwdSet.edges().contains(cb));
        assertTrue(fwdSet.edges().contains(cd));
        assertTrue(fwdSet.edges().contains(de));
        assertTrue(fwdSet.edges().contains(dg));
    }

    @Test
    public void testReverseStep() {
        Graph revC = graph.reverseStep(c);
        assertEquals(2, revC.nodes().size());
        assertTrue(revC.nodes().contains(b));
        assertTrue(revC.nodes().contains(c));
        assertEquals(1, revC.edges().size());
        assertTrue(revC.edges().contains(bc));

        Graph empty = graph.difference(graph);
        Graph fromSet = empty.union(b).union(e);
        Graph revSet = graph.reverseStep(fromSet.nodes());
        assertEquals(5, revSet.nodes().size()); // a, c, b, e, d
        assertTrue(revSet.nodes().contains(a));
        assertTrue(revSet.nodes().contains(c));
        assertTrue(revSet.nodes().contains(b));
        assertTrue(revSet.nodes().contains(e));
        assertTrue(revSet.nodes().contains(d));
        assertEquals(3, revSet.edges().size());
        assertTrue(revSet.edges().contains(ab));
        assertTrue(revSet.edges().contains(cb));
        assertTrue(revSet.edges().contains(de));
    }

    @Test
    public void testBetweenStep() {
        Graph betweenAB = graph.betweenStep(a, b);
        assertEquals(2, betweenAB.nodes().size());
        assertTrue(betweenAB.nodes().contains(a));
        assertTrue(betweenAB.nodes().contains(b));
        assertEquals(1, betweenAB.edges().size());
        assertTrue(betweenAB.edges().contains(ab));

        Graph betweenAC = graph.betweenStep(a, c); // 2 steps away, so betweenStep should be empty
        assertEquals(1, betweenAC.nodes().size());
        assertTrue(betweenAC.nodes().contains(b));
        assertEquals(0, betweenAC.edges().size());

        Graph empty = graph.difference(graph);
        Graph fromSet = empty.union(c);
        Graph toSet = empty.union(b).union(d);
        Graph betweenSet = graph.betweenStep(fromSet.nodes(), toSet.nodes());

        // c->b, c->d
        assertEquals(3, betweenSet.nodes().size()); // c, b, d
        assertTrue(betweenSet.nodes().contains(c));
        assertTrue(betweenSet.nodes().contains(b));
        assertTrue(betweenSet.nodes().contains(d));
        assertEquals(2, betweenSet.edges().size());
        assertTrue(betweenSet.edges().contains(cb));
        assertTrue(betweenSet.edges().contains(cd));
    }

    @Test
    public void testForward() {
        Graph fwdA = graph.forward(a); // all nodes reachable from a: a, b, c, d, e, g. Edges: ab, bc, cb, cd, de, dg
        assertEquals(6, fwdA.nodes().size());
        assertTrue(fwdA.nodes().contains(a));
        assertTrue(fwdA.nodes().contains(b));
        assertTrue(fwdA.nodes().contains(c));
        assertTrue(fwdA.nodes().contains(d));
        assertTrue(fwdA.nodes().contains(e));
        assertTrue(fwdA.nodes().contains(g));
        assertEquals(6, fwdA.edges().size()); // ab, bc, cb, cd, de, dg

        Graph fwdC = graph.forward(c); // b, c, d, e, g. Edges: cb, bc, cd, de, dg
        assertEquals(5, fwdC.nodes().size());
        assertFalse(fwdC.nodes().contains(a));
        assertEquals(5, fwdC.edges().size());

        Graph fwdF = graph.forward(f); // f is isolated
        assertEquals(1, fwdF.nodes().size());
        assertTrue(fwdF.nodes().contains(f));
        assertEquals(0, fwdF.edges().size());
    }

    @Test
    public void testReverse() {
        Graph revE = graph.reverse(e); // Nodes that can reach e: a, b, c, d, e. Edges: ab, bc, cb, cd, de
        assertEquals(5, revE.nodes().size());
        assertTrue(revE.nodes().contains(a));
        assertTrue(revE.nodes().contains(b));
        assertTrue(revE.nodes().contains(c));
        assertTrue(revE.nodes().contains(d));
        assertTrue(revE.nodes().contains(e));
        assertEquals(5, revE.edges().size());
        assertTrue(revE.edges().contains(ab));
        assertTrue(revE.edges().contains(bc));
        assertTrue(revE.edges().contains(cb));
        assertTrue(revE.edges().contains(cd));
        assertTrue(revE.edges().contains(de));

        Graph revC = graph.reverse(c); // b, a, c. Edges: ab, bc, cb
        assertEquals(3, revC.nodes().size());
        assertTrue(revC.nodes().contains(a));
        assertTrue(revC.nodes().contains(b));
        assertTrue(revC.nodes().contains(c));
        assertEquals(3, revC.edges().size());

        Graph revF = graph.reverse(f); // f is isolated
        assertEquals(1, revF.nodes().size());
        assertTrue(revF.nodes().contains(f));
        assertEquals(0, revF.edges().size());
    }

    @Test
    public void testBetween() {
        Graph betweenAE = graph.between(a, e);
        // Paths from a to e: a->b->c->d->e
        assertEquals(5, betweenAE.nodes().size());
        assertTrue(betweenAE.nodes().contains(a));
        assertTrue(betweenAE.nodes().contains(b));
        assertTrue(betweenAE.nodes().contains(c));
        assertTrue(betweenAE.nodes().contains(d));
        assertTrue(betweenAE.nodes().contains(e));
        assertEquals(5, betweenAE.edges().size()); // ab, bc, cb, cd, de

        Graph betweenAG = graph.between(a, g); // a->b->c->d->g
        assertEquals(5, betweenAG.nodes().size());
        assertTrue(betweenAG.nodes().contains(a));
        assertTrue(betweenAG.nodes().contains(b));
        assertTrue(betweenAG.nodes().contains(c));
        assertTrue(betweenAG.nodes().contains(d));
        assertTrue(betweenAG.nodes().contains(g));

        Graph betweenEG = graph.between(e, g); // No paths
        assertEquals(0, betweenEG.nodes().size());
        assertEquals(0, betweenEG.edges().size());
    }

    @Test
    public void testInduce() {
        Graph empty = graph.difference(graph);
        Graph g1 = empty.union(a).union(b).union(c);

        // induce(Edge edge)
        Graph inducedEdge = g1.induce(ab);
        assertEquals(3, inducedEdge.nodes().size());
        assertEquals(1, inducedEdge.edges().size());
        assertTrue(inducedEdge.edges().contains(ab));

        Graph inducedEdgeExcluded = g1.induce(cd);
        assertEquals(3, inducedEdgeExcluded.nodes().size());
        assertEquals(0, inducedEdgeExcluded.edges().size());
        assertFalse(inducedEdgeExcluded.edges().contains(cd));

        // induce(Graph graph)
        Graph inducedGraph = g1.induce(graph);
        assertEquals(3, inducedGraph.nodes().size());
        assertEquals(3, inducedGraph.edges().size());
        assertTrue(inducedGraph.edges().contains(ab));
        assertTrue(inducedGraph.edges().contains(bc));
        assertTrue(inducedGraph.edges().contains(cb));

        Graph g2 = empty.union(c).union(d);
        Graph inducedGraph2 = g2.induce(graph);
        assertEquals(2, inducedGraph2.nodes().size());
        assertEquals(1, inducedGraph2.edges().size());
        assertTrue(inducedGraph2.edges().contains(cd));

        // induce(EdgeSet edges)
        Graph inducedEdgeSet = g1.induce(graph.edges());
        assertEquals(3, inducedEdgeSet.nodes().size());
        assertEquals(3, inducedEdgeSet.edges().size());
        assertTrue(inducedEdgeSet.edges().contains(ab));
        assertTrue(inducedEdgeSet.edges().contains(bc));
        assertTrue(inducedEdgeSet.edges().contains(cb));
    }

    @Test
    public void testTraversalNullHandling() {
        assertThrows(NullPointerException.class, () -> graph.adjacent(null, a));
        assertThrows(NullPointerException.class, () -> graph.adjacent(a, null));

        assertThrows(NullPointerException.class, () -> graph.predecessors((Node) null));
        assertThrows(NullPointerException.class, () -> graph.predecessors((dev.chpg.pg.api.NodeSet) null));
        assertThrows(NullPointerException.class, () -> graph.predecessors((Graph) null));

        assertThrows(NullPointerException.class, () -> graph.successors((Node) null));
        assertThrows(NullPointerException.class, () -> graph.successors((dev.chpg.pg.api.NodeSet) null));
        assertThrows(NullPointerException.class, () -> graph.successors((Graph) null));

        assertThrows(NullPointerException.class, () -> graph.forwardStep((Node) null));
        assertThrows(NullPointerException.class, () -> graph.reverseStep((Node) null));

        assertThrows(NullPointerException.class, () -> graph.betweenStep((Node) null, a));
        assertThrows(NullPointerException.class, () -> graph.betweenStep(a, (Node) null));

        assertThrows(NullPointerException.class, () -> graph.forward((Node) null));
        assertThrows(NullPointerException.class, () -> graph.reverse((Node) null));

        assertThrows(NullPointerException.class, () -> graph.between((Node) null, a));
        assertThrows(NullPointerException.class, () -> graph.between(a, (Node) null));

        assertThrows(NullPointerException.class, () -> graph.induce((Edge) null));
        assertThrows(NullPointerException.class, () -> graph.induce((Graph) null));
        assertThrows(NullPointerException.class, () -> graph.induce((dev.chpg.pg.api.EdgeSet) null));
    }
}
