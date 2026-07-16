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
        assertTrue(graph.containsEdge(ab));
        assertTrue(graph.containsEdge(bc));

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

        assertTrue(graph.containsAllNodes(Arrays.asList(a, b)));
        assertFalse(graph.containsAllNodes(Arrays.asList(a, c)));

        assertTrue(graph.containsAllEdges(Arrays.asList(ab)));
        assertFalse(graph.containsAllEdges(Arrays.asList(ab, bc)));

        // Foreign node / edge testing
        dev.chpg.pg.api.Node foreignNode = new dev.chpg.pg.api.Node() {
            public int id() { return 100; }
            public dev.chpg.pg.api.TagSet tags() { return null; }
            public dev.chpg.pg.api.AttributeMap attributes() { return null; }
        };

        assertFalse(graph.containsAllNodes(Arrays.asList(a, foreignNode)));

        dev.chpg.pg.api.Edge foreignEdge = new dev.chpg.pg.api.Edge() {
            public int id() { return 100; }
            public dev.chpg.pg.api.Node from() { return a; }
            public dev.chpg.pg.api.Node to() { return b; }
            public dev.chpg.pg.api.TagSet tags() { return null; }
            public dev.chpg.pg.api.AttributeMap attributes() { return null; }
        };
        assertFalse(graph.containsAllEdges(Arrays.asList(ab, foreignEdge)));
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

        // Ensure between and betweenStep handle invalid nodes properly and run without error
        dev.chpg.pg.api.Graph betweenGraph = graph.between(sources, targets);
        assertEquals(0, betweenGraph.nodes().size());
        dev.chpg.pg.api.Graph betweenStepGraph = graph.betweenStep(sources, targets);
        assertEquals(0, betweenStepGraph.nodes().size());

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
}
