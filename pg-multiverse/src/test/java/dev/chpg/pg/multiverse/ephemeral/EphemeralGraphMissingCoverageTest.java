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
    public void testValidateLineageExceptions() {
        // Block Cross-Engine Contamination
        dev.chpg.pg.api.Graph mockGraph = new dev.chpg.pg.api.Graph() {
            public java.util.Optional<dev.chpg.pg.api.Node> node(int id) { return java.util.Optional.empty(); }
            public java.util.Optional<dev.chpg.pg.api.Edge> edge(int id) { return java.util.Optional.empty(); }
            public dev.chpg.pg.api.NodeSet nodes() { return null; }
            public dev.chpg.pg.api.EdgeSet edges() { return null; }
            public boolean addNode(dev.chpg.pg.api.Node node) { return false; }
            public boolean addAllNodes(java.util.Collection<? extends dev.chpg.pg.api.Node> nodes) { return false; }
            public boolean addEdge(dev.chpg.pg.api.Edge edge) { return false; }
            public boolean addAllEdges(java.util.Collection<? extends dev.chpg.pg.api.Edge> edges) { return false; }
            public boolean linkEdge(dev.chpg.pg.api.Edge edge) { return false; }
            public boolean linkAllEdges(java.util.Collection<? extends dev.chpg.pg.api.Edge> edges) { return false; }
            public boolean removeNode(dev.chpg.pg.api.Node node) { return false; }
            public boolean removeAllNodes(java.util.Collection<? extends dev.chpg.pg.api.Node> nodes) { return false; }
            public boolean retainAllNodes(java.util.Collection<? extends dev.chpg.pg.api.Node> nodes) { return false; }
            public boolean removeEdge(dev.chpg.pg.api.Edge edge) { return false; }
            public boolean removeAllEdges(java.util.Collection<? extends dev.chpg.pg.api.Edge> edges) { return false; }
            public boolean retainAllEdges(java.util.Collection<? extends dev.chpg.pg.api.Edge> edges) { return false; }
            public void clear() {}
            public void clearEdges() {}
            public dev.chpg.pg.api.NodeSet roots() { return null; }
            public dev.chpg.pg.api.NodeSet leaves() { return null; }
            public dev.chpg.pg.api.NodeSet isolated() { return null; }
            public boolean adjacent(dev.chpg.pg.api.Node source, dev.chpg.pg.api.Node target) { return false; }
            public dev.chpg.pg.api.EdgeSet edges(dev.chpg.pg.api.Node node, dev.chpg.pg.api.Node.NodeDirection direction) { return null; }
            public dev.chpg.pg.api.EdgeSet edges(dev.chpg.pg.api.Node source, dev.chpg.pg.api.Node target) { return null; }
            public int degree(dev.chpg.pg.api.Node node, dev.chpg.pg.api.Node.NodeDirection direction) { return 0; }
            public dev.chpg.pg.api.Graph union(dev.chpg.pg.api.Node... nodes) { return null; }
            public dev.chpg.pg.api.Graph union(dev.chpg.pg.api.Edge... edges) { return null; }
            public dev.chpg.pg.api.Graph union(dev.chpg.pg.api.NodeSet nodes) { return null; }
            public dev.chpg.pg.api.Graph union(dev.chpg.pg.api.EdgeSet edges) { return null; }
            public dev.chpg.pg.api.Graph union(dev.chpg.pg.api.Graph graph) { return null; }
            public dev.chpg.pg.api.Graph difference(dev.chpg.pg.api.Node... nodes) { return null; }
            public dev.chpg.pg.api.Graph difference(dev.chpg.pg.api.Edge... edges) { return null; }
            public dev.chpg.pg.api.Graph difference(dev.chpg.pg.api.NodeSet nodes) { return null; }
            public dev.chpg.pg.api.Graph difference(dev.chpg.pg.api.EdgeSet edges) { return null; }
            public dev.chpg.pg.api.Graph difference(dev.chpg.pg.api.Graph graph) { return null; }
            public dev.chpg.pg.api.Graph differenceEdges(dev.chpg.pg.api.Edge... edges) { return null; }
            public dev.chpg.pg.api.Graph differenceEdges(dev.chpg.pg.api.EdgeSet edges) { return null; }
            public dev.chpg.pg.api.Graph differenceEdges(dev.chpg.pg.api.Graph graph) { return null; }
            public dev.chpg.pg.api.Graph intersection(dev.chpg.pg.api.Node... nodes) { return null; }
            public dev.chpg.pg.api.Graph intersection(dev.chpg.pg.api.Edge... edges) { return null; }
            public dev.chpg.pg.api.Graph intersection(dev.chpg.pg.api.NodeSet nodes) { return null; }
            public dev.chpg.pg.api.Graph intersection(dev.chpg.pg.api.EdgeSet edges) { return null; }
            public dev.chpg.pg.api.Graph intersection(dev.chpg.pg.api.Graph graph) { return null; }
            public dev.chpg.pg.api.Graph betweenStep(dev.chpg.pg.api.Node from, dev.chpg.pg.api.Node to) { return null; }
            public dev.chpg.pg.api.Graph betweenStep(dev.chpg.pg.api.Graph from, dev.chpg.pg.api.Graph to) { return null; }
            public dev.chpg.pg.api.Graph betweenStep(dev.chpg.pg.api.NodeSet from, dev.chpg.pg.api.NodeSet to) { return null; }
            public dev.chpg.pg.api.Graph between(dev.chpg.pg.api.Node from, dev.chpg.pg.api.Node to) { return null; }
            public dev.chpg.pg.api.Graph between(dev.chpg.pg.api.Graph from, dev.chpg.pg.api.Graph to) { return null; }
            public dev.chpg.pg.api.Graph between(dev.chpg.pg.api.NodeSet from, dev.chpg.pg.api.NodeSet to) { return null; }
            public dev.chpg.pg.api.NodeSet successors(dev.chpg.pg.api.Node... origin) { return null; }
            public dev.chpg.pg.api.NodeSet successors(dev.chpg.pg.api.Graph origin) { return null; }
            public dev.chpg.pg.api.NodeSet successors(dev.chpg.pg.api.NodeSet origin) { return null; }
            public dev.chpg.pg.api.NodeSet predecessors(dev.chpg.pg.api.Node... origin) { return null; }
            public dev.chpg.pg.api.NodeSet predecessors(dev.chpg.pg.api.Graph origin) { return null; }
            public dev.chpg.pg.api.NodeSet predecessors(dev.chpg.pg.api.NodeSet origin) { return null; }
            public dev.chpg.pg.api.Graph forwardStep(dev.chpg.pg.api.Node... origin) { return null; }
            public dev.chpg.pg.api.Graph forwardStep(dev.chpg.pg.api.Graph origin) { return null; }
            public dev.chpg.pg.api.Graph forwardStep(dev.chpg.pg.api.NodeSet origin) { return null; }
            public dev.chpg.pg.api.Graph forward(dev.chpg.pg.api.Node... origin) { return null; }
            public dev.chpg.pg.api.Graph forward(dev.chpg.pg.api.Graph origin) { return null; }
            public dev.chpg.pg.api.Graph forward(dev.chpg.pg.api.NodeSet origin) { return null; }
            public dev.chpg.pg.api.Graph reverseStep(dev.chpg.pg.api.Node... origin) { return null; }
            public dev.chpg.pg.api.Graph reverseStep(dev.chpg.pg.api.Graph origin) { return null; }
            public dev.chpg.pg.api.Graph reverseStep(dev.chpg.pg.api.NodeSet origin) { return null; }
            public dev.chpg.pg.api.Graph reverse(dev.chpg.pg.api.Node... origin) { return null; }
            public dev.chpg.pg.api.Graph reverse(dev.chpg.pg.api.Graph origin) { return null; }
            public dev.chpg.pg.api.Graph reverse(dev.chpg.pg.api.NodeSet origin) { return null; }
            public dev.chpg.pg.api.Graph induce(dev.chpg.pg.api.Edge... edges) { return null; }
            public dev.chpg.pg.api.Graph induce(dev.chpg.pg.api.EdgeSet edges) { return null; }
            public dev.chpg.pg.api.Graph induce(dev.chpg.pg.api.Graph graph) { return null; }
        };
        assertThrows(IllegalArgumentException.class, () -> graph.union(mockGraph));

        // Block Cross-Sandbox Contamination
        EphemeralGraph otherGraph = new EphemeralGraph();
        assertThrows(IllegalArgumentException.class, () -> graph.union(otherGraph));
    }

    @Test
    public void testAddAndRemoveExceptions() {
        graph.addNode(a);

        dev.chpg.pg.api.Node foreignNode = new dev.chpg.pg.api.Node() {
            public int id() { return -100; }
            public dev.chpg.pg.api.TagSet tags() { return null; }
            public dev.chpg.pg.api.AttributeMap attributes() { return null; }
        };

        assertThrows(IllegalArgumentException.class, () -> graph.addNode(foreignNode));

        dev.chpg.pg.api.Edge foreignEdge = new dev.chpg.pg.api.Edge() {
            public int id() { return -100; }
            public dev.chpg.pg.api.Node from() { return a; }
            public dev.chpg.pg.api.Node to() { return b; }
            public dev.chpg.pg.api.TagSet tags() { return null; }
            public dev.chpg.pg.api.AttributeMap attributes() { return null; }
        };

        assertThrows(IllegalArgumentException.class, () -> graph.addEdge(foreignEdge));

        assertFalse(graph.removeNode(foreignNode));

        // Positive ID Exception
        EphemeralNode posIdNode = new EphemeralNode(1);
        assertThrows(IllegalArgumentException.class, () -> graph.addNode(posIdNode));

        EphemeralEdge posIdEdge = new EphemeralEdge(1, a, b);
        assertThrows(IllegalArgumentException.class, () -> graph.addEdge(posIdEdge));
    }

    @Test
    public void testEdgesBoth() {
        graph.addNode(a);
        graph.addNode(b);
        graph.addNode(c);
        graph.addEdge(ab);
        graph.addEdge(bc);

        dev.chpg.pg.api.EdgeSet bEdges = graph.edges(b, NodeDirection.BOTH);
        assertEquals(2, bEdges.size());
        assertTrue(bEdges.contains(ab));
        assertTrue(bEdges.contains(bc));

        dev.chpg.pg.api.EdgeSet aEdges = graph.edges(a, NodeDirection.BOTH);
        assertEquals(1, aEdges.size());
        assertTrue(aEdges.contains(ab));

        dev.chpg.pg.api.EdgeSet cEdges = graph.edges(c, NodeDirection.BOTH);
        assertEquals(1, cEdges.size());
        assertTrue(cEdges.contains(bc));
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

        // Foreign node
        dev.chpg.pg.api.Node foreignNode = new dev.chpg.pg.api.Node() {
            public int id() { return -100; }
            public dev.chpg.pg.api.TagSet tags() { return null; }
            public dev.chpg.pg.api.AttributeMap attributes() { return null; }
        };
        assertEquals(0, graph.degree(foreignNode, NodeDirection.IN));
        assertEquals(0, graph.degree(foreignNode, NodeDirection.OUT));
        assertEquals(0, graph.degree(foreignNode, NodeDirection.BOTH));

        EphemeralNode isolatedNode = (EphemeralNode) factory.createNode();
        assertEquals(0, graph.degree(isolatedNode, NodeDirection.BOTH));
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

        // Ensure between and betweenStep handle empty step branches
        EphemeralNode isolatedNode = (EphemeralNode) factory.createNode();
        graph.addNode(isolatedNode);

        dev.chpg.pg.api.NodeSet isolatedSource = graph.nodes().withAttribute("id", dev.chpg.pg.api.AttributeValue.value(isolatedNode.id()));

        // Ensure reverse step is non-empty when testing empty forward step
        EphemeralNode isolatedNode2 = (EphemeralNode) factory.createNode();
        graph.addNode(isolatedNode2);
        dev.chpg.pg.api.NodeSet isolatedSource2 = graph.nodes().withAttribute("id", dev.chpg.pg.api.AttributeValue.value(isolatedNode2.id()));

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

        EphemeralGraph g2 = (EphemeralGraph) graph.createGraph();
        g2.addNode((EphemeralNode) g2.factory().createNode());

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
    public void testCreateGraph() {
        EphemeralGraph emptyGraph = graph.createGraph();
        assertTrue(emptyGraph.nodes().isEmpty());

        EphemeralGraph nodeGraph = graph.createGraph(a, b);
        assertEquals(2, nodeGraph.nodes().size());

        EphemeralNodeSet nodeSet = new EphemeralNodeSet();
        nodeSet.add(a);
        nodeSet.add(b);
        EphemeralGraph nodeSetGraph = graph.createGraph(nodeSet);
        assertEquals(2, nodeSetGraph.nodes().size());

        EphemeralGraph edgeGraph = graph.createGraph(ab);
        assertEquals(2, edgeGraph.nodes().size());
        assertEquals(1, edgeGraph.edges().size());

        EphemeralEdgeSet edgeSet = new EphemeralEdgeSet();
        edgeSet.add(ab);
        EphemeralGraph edgeSetGraph = graph.createGraph(edgeSet);
        assertEquals(2, edgeSetGraph.nodes().size());
        assertEquals(1, edgeSetGraph.edges().size());

        EphemeralGraph multiGraph = graph.createGraph(nodeSet, edgeSet);
        assertEquals(2, multiGraph.nodes().size());
        assertEquals(1, multiGraph.edges().size());

        EphemeralGraph copyGraph = graph.createGraph(multiGraph);
        assertEquals(2, copyGraph.nodes().size());
        assertEquals(1, copyGraph.edges().size());
    }
}
