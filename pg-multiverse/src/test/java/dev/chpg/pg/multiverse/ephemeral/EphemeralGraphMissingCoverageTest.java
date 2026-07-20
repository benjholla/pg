package dev.chpg.pg.multiverse.ephemeral;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Node.NodeDirection;

public class EphemeralGraphMissingCoverageTest {

    private EphemeralGraph graph;
    private EphemeralNode a, b, c;
    private EphemeralEdge ab, bc;
    private EphemeralFactory factory;

    @BeforeEach
    public void setUp() {
        graph = new EphemeralGraph();
        factory = graph.factory();
        a = (EphemeralNode) factory.createNode();
        b = (EphemeralNode) factory.createNode();
        c = (EphemeralNode) factory.createNode();

        ab = (EphemeralEdge) factory.createEdge(a, b);
        bc = (EphemeralEdge) factory.createEdge(b, c);
    }

    @Test
    public void testDegree() {
        graph.addNode(a);
        graph.addNode(b);
        graph.addNode(c);
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
        graph.addNode(a);
        graph.addNode(b);
        graph.addNode(c);
        graph.addEdge(ab);

        assertTrue(graph.nodes().containsAll(Arrays.asList(a, b)));
        assertTrue(graph.nodes().containsAll(Arrays.asList(a, c)));

        graph.addNode(a);
        graph.addNode(b);
        graph.addNode(c);
        graph.addEdge(ab);
        assertTrue(graph.edges().containsAll(Arrays.asList(ab)));
        assertFalse(graph.edges().containsAll(Arrays.asList(ab, bc)));

        // Foreign node / edge testing
        dev.chpg.pg.api.Node foreignNode2 = new dev.chpg.pg.api.Node() {
            public int id() { return 100; }
            public dev.chpg.pg.api.TagSet tags() { return null; }
            public dev.chpg.pg.api.AttributeMap attributes() { return null; }
        };

        assertFalse(graph.nodes().containsAll(Arrays.asList(a, foreignNode2)));

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
        EphemeralEdge edge = (EphemeralEdge) graph.createEdge(a, b);
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
    public void testSetIntersections() {
        EphemeralNodeSet nodeSet = new EphemeralNodeSet();
        nodeSet.add(a);

        EphemeralNodeSet otherNodeSet = new EphemeralNodeSet();
        dev.chpg.pg.api.Node foreignNode = new dev.chpg.pg.api.Node() {
            public int id() { return 100; }
            public dev.chpg.pg.api.TagSet tags() { return null; }
            public dev.chpg.pg.api.AttributeMap attributes() { return null; }
        };
        assertThrows(IllegalArgumentException.class, () -> otherNodeSet.add(foreignNode));
        otherNodeSet.add(a);
        otherNodeSet.add(b);

        dev.chpg.pg.api.NodeSet nodeIntersect = nodeSet.intersect(otherNodeSet);
        assertEquals(1, nodeIntersect.size());
        assertTrue(nodeIntersect.contains(a));

        EphemeralEdgeSet edgeSet = new EphemeralEdgeSet();
        edgeSet.add(ab);

        EphemeralEdgeSet otherEdgeSet = new EphemeralEdgeSet();
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

        dev.chpg.pg.api.EdgeSet edgeIntersect = edgeSet.intersect(otherEdgeSet);
        assertEquals(1, edgeIntersect.size());
        assertTrue(edgeIntersect.contains(ab));

        // Singletons handling
        dev.chpg.pg.api.NodeSet singleNode = nodeSet.intersect(nodeSet);
        assertEquals(1, singleNode.size());

        dev.chpg.pg.api.EdgeSet singleEdge = edgeSet.intersect(edgeSet);
        assertEquals(1, singleEdge.size());

        // UnmodifiableLiveSets
        graph.addNode(a);
        graph.addNode(b);
        graph.addEdge(ab);

        dev.chpg.pg.api.NodeSet unmodNodes = graph.nodes();
        unmodNodes.intersect(otherNodeSet);

        dev.chpg.pg.api.EdgeSet unmodEdges = graph.edges();
        unmodEdges.intersect(otherEdgeSet);

        unmodNodes.union(otherNodeSet);
        unmodEdges.union(otherEdgeSet);

        dev.chpg.pg.api.NodeSet emptyNodeSet = nodeSet.intersect(dev.chpg.pg.api.NodeSet.empty());
        assertEquals(0, emptyNodeSet.size());

        dev.chpg.pg.api.EdgeSet emptyEdgeSet = edgeSet.intersect(dev.chpg.pg.api.EdgeSet.empty());
        assertEquals(0, emptyEdgeSet.size());

        dev.chpg.pg.api.NodeSet unmodNodesEmpty = unmodNodes.intersect(dev.chpg.pg.api.NodeSet.empty());
        assertEquals(0, unmodNodesEmpty.size());

        dev.chpg.pg.api.EdgeSet unmodEdgesEmpty = unmodEdges.intersect(dev.chpg.pg.api.EdgeSet.empty());
        assertEquals(0, unmodEdgesEmpty.size());

        // Singletons handling for edge set sizes
        EphemeralEdgeSet singletonEdgeSet = new EphemeralEdgeSet();
        singletonEdgeSet.add(ab);
        assertEquals(1, singletonEdgeSet.intersect(singletonEdgeSet).size());

        EphemeralNodeSet singletonNodeSet = new EphemeralNodeSet();
        singletonNodeSet.add(a);
        assertEquals(1, singletonNodeSet.intersect(singletonNodeSet).size());
    }

    @Test
    public void testBetween() {
        graph.addNode(a);
        graph.addNode(b);
        graph.addNode(c);
        graph.addEdge(ab);
        graph.addEdge(bc);

        dev.chpg.pg.api.NodeSet sources = graph.nodes().withAttribute("id", dev.chpg.pg.api.AttributeValue.value(a.id()));
        dev.chpg.pg.api.NodeSet targets = graph.nodes().withAttribute("id", dev.chpg.pg.api.AttributeValue.value(c.id()));

        dev.chpg.pg.api.Graph betweenGraph = graph.between(sources, targets);
        assertEquals(0, betweenGraph.nodes().size());
        dev.chpg.pg.api.Graph betweenStepGraph = graph.betweenStep(sources, targets);
        assertEquals(0, betweenStepGraph.nodes().size());

        EphemeralGraph g2 = new EphemeralGraph();
        g2.addNode((EphemeralNode) g2.factory().createNode());

        dev.chpg.pg.api.Graph g1 = graph.between(g2.nodes(), targets);
        assertEquals(0, g1.nodes().size());
        dev.chpg.pg.api.Graph g3 = graph.between(sources, g2.nodes());
        assertEquals(0, g3.nodes().size());
        dev.chpg.pg.api.Graph g4 = graph.betweenStep(g2.nodes(), targets);
        assertEquals(0, g4.nodes().size());
        dev.chpg.pg.api.Graph g5 = graph.betweenStep(sources, g2.nodes());
        assertEquals(0, g5.nodes().size());
    }

}
