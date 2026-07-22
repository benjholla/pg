package dev.chpg.pg.global;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.EdgeSet;
import dev.chpg.pg.api.Node.NodeDirection;
import dev.chpg.pg.api.NodeSet;

public class GlobalGraphMissingCoverageTest {

    private GlobalGraph graph;
    private GlobalNode a, b, c;
    private GlobalEdge ab, bc;

    @BeforeEach
    public void setUp() {
        graph = new GlobalGraph();
        a = new GlobalNode();
        b = new GlobalNode();
        c = new GlobalNode();

        ab = new GlobalEdge(a, b);
        bc = new GlobalEdge(b, c);
    }

    @Test
    public void testAddAndRemoveExceptions() {
        dev.chpg.pg.api.Node foreignNode = new dev.chpg.pg.api.Node() {
            public int id() { return 100; }
            public dev.chpg.pg.api.TagSet tags() { return null; }
            public dev.chpg.pg.api.AttributeMap attributes() { return null; }
        };

        assertThrows(IllegalArgumentException.class, () -> graph.addNode(foreignNode));

        dev.chpg.pg.api.Edge foreignEdge = new dev.chpg.pg.api.Edge() {
            public int id() { return 100; }
            public dev.chpg.pg.api.Node from() { return a; }
            public dev.chpg.pg.api.Node to() { return b; }
            public dev.chpg.pg.api.TagSet tags() { return null; }
            public dev.chpg.pg.api.AttributeMap attributes() { return null; }
        };

        assertThrows(IllegalArgumentException.class, () -> graph.addEdge(foreignEdge));

        assertFalse(graph.removeNode(foreignNode));
    }

    @Test
    public void testEdgesBoth() {
        graph.addEdge(ab);
        graph.addEdge(bc);

        EdgeSet bEdges = graph.edges(b, NodeDirection.BOTH);
        assertEquals(2, bEdges.size());
        assertTrue(bEdges.contains(ab));
        assertTrue(bEdges.contains(bc));
    }

    @Test
    public void testDegree() {
        graph.addEdge(ab);
        graph.addEdge(bc);

        assertEquals(0, graph.degree(a, NodeDirection.IN));
        assertEquals(1, graph.degree(a, NodeDirection.OUT));
        assertEquals(1, graph.degree(a, NodeDirection.BOTH));

        assertEquals(1, graph.degree(b, NodeDirection.IN));
        assertEquals(1, graph.degree(b, NodeDirection.OUT));
        assertEquals(2, graph.degree(b, NodeDirection.BOTH));

        assertEquals(1, graph.degree(c, NodeDirection.IN));
        assertEquals(0, graph.degree(c, NodeDirection.OUT));
        assertEquals(1, graph.degree(c, NodeDirection.BOTH));

        // Foreign node
        GlobalNode foreign = new GlobalNode();
        assertEquals(0, graph.degree(foreign, NodeDirection.IN));
        assertEquals(0, graph.degree(foreign, NodeDirection.OUT));
        assertEquals(0, graph.degree(foreign, NodeDirection.BOTH));
    }

    @Test
    public void testLinkAllEdges() {
        graph.addNode(a);
        graph.addNode(b);
        graph.addNode(c);

        assertTrue(graph.linkAllEdges(Arrays.asList(ab, bc)));
        assertTrue(graph.edges().contains(ab));
        assertTrue(graph.edges().contains(bc));

        // Foreign edge
        dev.chpg.pg.api.Edge foreignEdge = new dev.chpg.pg.api.Edge() {
            public int id() { return 100; }
            public dev.chpg.pg.api.Node from() { return a; }
            public dev.chpg.pg.api.Node to() { return b; }
            public dev.chpg.pg.api.TagSet tags() { return null; }
            public dev.chpg.pg.api.AttributeMap attributes() { return null; }
        };

        assertThrows(IllegalArgumentException.class, () -> {
            graph.linkAllEdges(Arrays.asList(foreignEdge));
        });
    }

    @Test
    public void testContainsAllNodesAndEdges() {
        graph.addEdge(ab);

        assertTrue(graph.nodes().containsAll(Arrays.asList(a, b)));
        assertFalse(graph.nodes().containsAll(Arrays.asList(a, c)));

        assertTrue(graph.edges().containsAll(Arrays.asList(ab)));
        assertFalse(graph.edges().containsAll(Arrays.asList(ab, bc)));

        // Foreign node / edge testing
        dev.chpg.pg.api.Node foreignNode = new dev.chpg.pg.api.Node() {
            public int id() { return 100; }
            public dev.chpg.pg.api.TagSet tags() { return null; }
            public dev.chpg.pg.api.AttributeMap attributes() { return null; }
        };

        assertFalse(graph.nodes().containsAll(Arrays.asList(a, foreignNode)));

        dev.chpg.pg.api.Edge foreignEdge = new dev.chpg.pg.api.Edge() {
            public int id() { return 100; }
            public dev.chpg.pg.api.Node from() { return a; }
            public dev.chpg.pg.api.Node to() { return b; }
            public dev.chpg.pg.api.TagSet tags() { return null; }
            public dev.chpg.pg.api.AttributeMap attributes() { return null; }
        };
        assertFalse(graph.edges().containsAll(Arrays.asList(ab, foreignEdge)));
    }

    @Test
    public void testCreateEdge() {
        GlobalEdge edge = graph.createEdge(a, b);
        assertEquals(a, edge.from());
        assertEquals(b, edge.to());

        dev.chpg.pg.api.Node foreignNode1 = new dev.chpg.pg.api.Node() {
            public int id() { return 100; }
            public dev.chpg.pg.api.TagSet tags() { return null; }
            public dev.chpg.pg.api.AttributeMap attributes() { return null; }
        };

        assertThrows(IllegalArgumentException.class, () -> graph.createEdge(foreignNode1, b));
        assertThrows(IllegalArgumentException.class, () -> graph.createEdge(a, foreignNode1));
    }

    @Test
    public void testBetween() {
        graph.addNode(a);
        graph.addNode(b);
        graph.addNode(c);
        graph.addEdge(ab);
        graph.addEdge(bc);

        NodeSet sources = graph.nodes().withAttribute("id", dev.chpg.pg.api.AttributeValue.value(a.id()));
        NodeSet targets = graph.nodes().withAttribute("id", dev.chpg.pg.api.AttributeValue.value(c.id()));

        // Ensure between and betweenStep handle empty step branches
        GlobalNode isolatedNode = new GlobalNode();
        graph.addNode(isolatedNode);

        NodeSet isolatedSource = graph.nodes().withAttribute("id", dev.chpg.pg.api.AttributeValue.value(isolatedNode.id()));

        // Ensure reverse step is non-empty when testing empty forward step
        GlobalNode isolatedNode2 = new GlobalNode();
        graph.addNode(isolatedNode2);
        NodeSet isolatedSource2 = graph.nodes().withAttribute("id", dev.chpg.pg.api.AttributeValue.value(isolatedNode2.id()));

        // Ensure reverse step is non-empty by providing an actual target from the graph ('c')
        // Forward step is empty since isolatedNode has no outgoing edges
        dev.chpg.pg.api.Graph betweenGraph1 = graph.between(isolatedSource, targets);
        assertEquals(0, betweenGraph1.nodes().size());
        dev.chpg.pg.api.Graph betweenStepGraph1 = graph.betweenStep(isolatedSource, targets);
        assertEquals(0, betweenStepGraph1.nodes().size());

        // We also need reverse step to be empty and forward step to be non-empty.
        // For forward step to be non-empty, we need it to reach nodes that are in `isolatedSource2`.
        // So we add an edge from 'a' to `isolatedNode2` just to make the forward step from 'a' yield something.
        graph.addEdge(graph.createEdge(a, isolatedNode2));

        dev.chpg.pg.api.Graph betweenGraph2 = graph.between(sources, isolatedSource2);
        assertEquals(0, betweenGraph2.nodes().size());
        dev.chpg.pg.api.Graph betweenStepGraph2 = graph.betweenStep(sources, isolatedSource2);
        assertEquals(0, betweenStepGraph2.nodes().size());

        GlobalGraph g2 = new GlobalGraph();
        g2.addNode(new GlobalNode());

        dev.chpg.pg.api.Graph g1 = graph.between(g2.nodes(), targets);
        assertEquals(0, g1.nodes().size());
        dev.chpg.pg.api.Graph g3 = graph.between(sources, g2.nodes());
        assertEquals(0, g3.nodes().size());
        dev.chpg.pg.api.Graph g4 = graph.betweenStep(g2.nodes(), targets);
        assertEquals(0, g4.nodes().size());
        dev.chpg.pg.api.Graph g5 = graph.betweenStep(sources, g2.nodes());
        assertEquals(0, g5.nodes().size());

        // Also ensure full forward/reverse can hit the empty branches
        dev.chpg.pg.api.Graph fullBetweenForward = graph.between(isolatedSource, targets);
        assertEquals(0, fullBetweenForward.nodes().size());
        dev.chpg.pg.api.Graph fullBetweenReverse = graph.between(sources, isolatedSource2);
        assertEquals(0, fullBetweenReverse.nodes().size());
    }

    @Test
    public void testSetIntersections() {
        GlobalNodeSet nodeSet = new GlobalNodeSet();
        nodeSet.add(a);

        GlobalNodeSet otherNodeSet = new GlobalNodeSet();
        dev.chpg.pg.api.Node foreignNode = new dev.chpg.pg.api.Node() {
            public int id() { return 100; }
            public dev.chpg.pg.api.TagSet tags() { return null; }
            public dev.chpg.pg.api.AttributeMap attributes() { return null; }
        };
        assertThrows(IllegalArgumentException.class, () -> otherNodeSet.add(foreignNode));
        otherNodeSet.add(a);
        otherNodeSet.add(b);

        NodeSet nodeIntersect = nodeSet.intersect(otherNodeSet);
        assertEquals(1, nodeIntersect.size());
        assertTrue(nodeIntersect.contains(a));

        GlobalEdgeSet edgeSet = new GlobalEdgeSet();
        edgeSet.add(ab);

        GlobalEdgeSet otherEdgeSet = new GlobalEdgeSet();
        dev.chpg.pg.api.Edge foreignEdge = new dev.chpg.pg.api.Edge() {
            public int id() { return 100; }
            public dev.chpg.pg.api.Node from() { return a; }
            public dev.chpg.pg.api.Node to() { return b; }
            public dev.chpg.pg.api.TagSet tags() { return null; }
            public dev.chpg.pg.api.AttributeMap attributes() { return null; }
        };
        assertThrows(IllegalArgumentException.class, () -> otherEdgeSet.add(foreignEdge));
        otherEdgeSet.add(ab);
        otherEdgeSet.add(bc);

        EdgeSet edgeIntersect = edgeSet.intersect(otherEdgeSet);
        assertEquals(1, edgeIntersect.size());
        assertTrue(edgeIntersect.contains(ab));

        // Singletons handling
        NodeSet singleNode = nodeSet.intersect(nodeSet);
        assertEquals(1, singleNode.size());

        EdgeSet singleEdge = edgeSet.intersect(edgeSet);
        assertEquals(1, singleEdge.size());

        // UnmodifiableLiveSets
        graph.addNode(a);
        graph.addNode(b);
        graph.addEdge(ab);

        NodeSet unmodNodes = graph.nodes();
        unmodNodes.intersect(otherNodeSet);

        EdgeSet unmodEdges = graph.edges();
        unmodEdges.intersect(otherEdgeSet);

        unmodNodes.union(otherNodeSet);
        unmodEdges.union(otherEdgeSet);

        NodeSet emptyNodeSet = nodeSet.intersect(NodeSet.empty());
        assertEquals(0, emptyNodeSet.size());

        EdgeSet emptyEdgeSet = edgeSet.intersect(EdgeSet.empty());
        assertEquals(0, emptyEdgeSet.size());

        NodeSet unmodNodesEmpty = unmodNodes.intersect(NodeSet.empty());
        assertEquals(0, unmodNodesEmpty.size());

        EdgeSet unmodEdgesEmpty = unmodEdges.intersect(EdgeSet.empty());
        assertEquals(0, unmodEdgesEmpty.size());

        // Singletons handling for edge set sizes
        GlobalEdgeSet singletonEdgeSet = new GlobalEdgeSet();
        singletonEdgeSet.add(ab);
        assertEquals(1, singletonEdgeSet.intersect(singletonEdgeSet).size());

        GlobalNodeSet singletonNodeSet = new GlobalNodeSet();
        singletonNodeSet.add(a);
        assertEquals(1, singletonNodeSet.intersect(singletonNodeSet).size());
    }

    @Test
    public void testSingleton() {
        NodeSet singleNode = graph.singleton(a);
        assertEquals(1, singleNode.size());
        assertTrue(singleNode.contains(a));

        EdgeSet singleEdge = graph.singleton(ab);
        assertEquals(1, singleEdge.size());
        assertTrue(singleEdge.contains(ab));
    }

    @Test
    public void testCreateGraph() {
        GlobalGraph emptyGraph = graph.createGraph();
        assertTrue(emptyGraph.nodes().isEmpty());

        GlobalGraph nodeGraph = graph.createGraph(a, b);
        assertEquals(2, nodeGraph.nodes().size());

        GlobalNodeSet nodeSet = new GlobalNodeSet();
        nodeSet.add(a);
        nodeSet.add(b);
        GlobalGraph nodeSetGraph = graph.createGraph(nodeSet);
        assertEquals(2, nodeSetGraph.nodes().size());

        GlobalGraph edgeGraph = graph.createGraph(ab);
        assertEquals(2, edgeGraph.nodes().size());
        assertEquals(1, edgeGraph.edges().size());

        GlobalEdgeSet edgeSet = new GlobalEdgeSet();
        edgeSet.add(ab);
        GlobalGraph edgeSetGraph = graph.createGraph(edgeSet);
        assertEquals(2, edgeSetGraph.nodes().size());
        assertEquals(1, edgeSetGraph.edges().size());

        GlobalGraph multiGraph = graph.createGraph(nodeSet, edgeSet);
        assertEquals(2, multiGraph.nodes().size());
        assertEquals(1, multiGraph.edges().size());

        GlobalGraph copyGraph = graph.createGraph(multiGraph);
        assertEquals(2, copyGraph.nodes().size());
        assertEquals(1, copyGraph.edges().size());

        assertEquals(graph, graph.factory());
    }
}
