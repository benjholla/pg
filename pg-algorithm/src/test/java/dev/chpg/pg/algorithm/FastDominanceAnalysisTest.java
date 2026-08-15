package dev.chpg.pg.algorithm;

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.global.GlobalGraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FastDominanceAnalysisTest {

    private Graph graph;

    @BeforeEach
    public void setup() {
        graph = new GlobalGraph();
    }

    @Test
    public void testDiamondGraphDominance() {
        // A -> B, A -> C, B -> D, C -> D
        Node a = graph.factory().createNode();
        Node b = graph.factory().createNode();
        Node c = graph.factory().createNode();
        Node d = graph.factory().createNode();

        graph.addNode(a);
        graph.addNode(b);
        graph.addNode(c);
        graph.addNode(d);

        graph.addEdge(graph.factory().createEdge(a, b));
        graph.addEdge(graph.factory().createEdge(a, c));
        graph.addEdge(graph.factory().createEdge(b, d));
        graph.addEdge(graph.factory().createEdge(c, d));

        FastDominanceAnalysis dom = new FastDominanceAnalysis(graph, a, false);

        Map<Node, Node> idoms = dom.getIdoms();
        // A is the root, so it has no idom (or idom is itself, but here getIdoms excludes root)
        assertEquals(a, idoms.get(b));
        assertEquals(a, idoms.get(c));
        assertEquals(a, idoms.get(d)); // A strictly dominates D because all paths to D go through A

        Map<Node, Set<Node>> domFrontiers = dom.getDominanceFrontiers();
        // D is in the dominance frontier of B and C
        assertTrue(domFrontiers.get(b).contains(d));
        assertTrue(domFrontiers.get(c).contains(d));
        assertFalse(domFrontiers.get(a).contains(d));
    }

    @Test
    public void testDiamondGraphPostDominance() {
        // A -> B, A -> C, B -> D, C -> D
        Node a = graph.factory().createNode();
        Node b = graph.factory().createNode();
        Node c = graph.factory().createNode();
        Node d = graph.factory().createNode();

        graph.addNode(a);
        graph.addNode(b);
        graph.addNode(c);
        graph.addNode(d);

        graph.addEdge(graph.factory().createEdge(a, b));
        graph.addEdge(graph.factory().createEdge(a, c));
        graph.addEdge(graph.factory().createEdge(b, d));
        graph.addEdge(graph.factory().createEdge(c, d));

        FastDominanceAnalysis pdom = new FastDominanceAnalysis(graph, d, true); // True for post-dominance

        Map<Node, Node> idoms = pdom.getIdoms();
        assertEquals(d, idoms.get(b));
        assertEquals(d, idoms.get(c));
        assertEquals(d, idoms.get(a)); // D strictly post-dominates A

        Map<Node, Set<Node>> domFrontiers = pdom.getDominanceFrontiers();
        // A is in the post-dominance frontier of B and C
        assertTrue(domFrontiers.get(b).contains(a));
        assertTrue(domFrontiers.get(c).contains(a));
    }

    @Test
    public void testLoopGraphDominance() {
        // A -> B, B -> C, C -> B, C -> D
        Node a = graph.factory().createNode();
        Node b = graph.factory().createNode();
        Node c = graph.factory().createNode();
        Node d = graph.factory().createNode();

        graph.addNode(a);
        graph.addNode(b);
        graph.addNode(c);
        graph.addNode(d);

        graph.addEdge(graph.factory().createEdge(a, b));
        graph.addEdge(graph.factory().createEdge(b, c));
        graph.addEdge(graph.factory().createEdge(c, b)); // loop
        graph.addEdge(graph.factory().createEdge(c, d));

        FastDominanceAnalysis dom = new FastDominanceAnalysis(graph, a, false);

        Map<Node, Node> idoms = dom.getIdoms();
        assertEquals(a, idoms.get(b));
        assertEquals(b, idoms.get(c));
        assertEquals(c, idoms.get(d));

        Map<Node, Set<Node>> domFrontiers = dom.getDominanceFrontiers();
        // B is in its own dominance frontier because of the loop (C -> B) where B dominates C
        assertTrue(domFrontiers.get(c).contains(b));
        assertTrue(domFrontiers.get(b).contains(b));
    }

    @Test
    public void testTopologicalTraversal() {
        Node a = graph.factory().createNode();
        Node b = graph.factory().createNode();
        Node c = graph.factory().createNode();

        graph.addNode(a);
        graph.addNode(b);
        graph.addNode(c);

        graph.addEdge(graph.factory().createEdge(a, b));
        graph.addEdge(graph.factory().createEdge(a, c));

        FastDominanceAnalysis dom = new FastDominanceAnalysis(graph, a, false);
        List<Node> traversal = dom.topologicalTraversal();

        assertEquals(3, traversal.size());
        assertEquals(a, traversal.get(0)); // Root must be first
        assertTrue(traversal.contains(b));
        assertTrue(traversal.contains(c));

        // Test reverse topological
        Iterable<Node> revTraversalIterable = dom.reverseTopologicalTraversal();
        Iterator<Node> revIter = revTraversalIterable.iterator();
        Node last = null;
        while(revIter.hasNext()){
             last = revIter.next();
        }
        assertEquals(a, last); // Root must be last in reverse
    }

    @Test
    public void testInjectEdges() {
        Node a = graph.factory().createNode();
        Node b = graph.factory().createNode();
        Node c = graph.factory().createNode();

        graph.addNode(a);
        graph.addNode(b);
        graph.addNode(c);

        graph.addEdge(graph.factory().createEdge(a, b));
        graph.addEdge(graph.factory().createEdge(b, c));
        graph.addEdge(graph.factory().createEdge(a, c)); // A directly to C

        FastDominanceAnalysis dom = new FastDominanceAnalysis(graph, a, false);
        dom.injectEdges();

        boolean foundIdomB = false;
        boolean foundIdomC = false;

        for (Edge e : graph.edges()) {
            if (e.tags().contains(FastDominanceAnalysis.DOMINATOR_TREE_EDGE)) {
                if (e.from().equals(a) && e.to().equals(b)) {
                    foundIdomB = true;
                }
                if (e.from().equals(a) && e.to().equals(c)) {
                    foundIdomC = true;
                }
            }
        }

        assertTrue(foundIdomB);
        assertTrue(foundIdomC); // A dominates C despite path through B
    }


    @Test
    public void testNullGraph() {
        boolean thrown = false;
        try {
            new FastDominanceAnalysis(null, graph.factory().createNode(), false);
        } catch (NullPointerException e) {
            thrown = true;
        }
        assertTrue(thrown, "Expected NullPointerException when graph is null");
    }

    @Test
    public void testNullRootNode() {
        boolean thrown = false;
        try {
            new FastDominanceAnalysis(graph, null, false);
        } catch (NullPointerException e) {
            thrown = true;
        }
        assertTrue(thrown, "Expected NullPointerException when root is null");
    }

}
