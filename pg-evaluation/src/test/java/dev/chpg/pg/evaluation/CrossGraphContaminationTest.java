package dev.chpg.pg.evaluation;

import dev.chpg.pg.multiverse.universe.Universe;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.EdgeSet;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.NodeSet;
import dev.chpg.pg.global.GlobalGraph;
import dev.chpg.pg.multiverse.ephemeral.EphemeralGraph;
import dev.chpg.pg.multiverse.ephemeral.ShadowNodeSet;

public class CrossGraphContaminationTest {

    @Test
    public void globalGraphRejectsEphemeralElements() {
        GlobalGraph globalGraph = new GlobalGraph();
        EphemeralGraph ephemeralGraph = new EphemeralGraph(new Universe());

        Node ephemeralNode = ephemeralGraph.factory().createNode();
        Edge ephemeralEdge = ephemeralGraph.factory().createEdge(ephemeralNode, ephemeralNode);

        assertThrows(IllegalArgumentException.class, () -> globalGraph.addNode(ephemeralNode));
        assertThrows(IllegalArgumentException.class, () -> globalGraph.addEdge(ephemeralEdge));
        assertFalse(globalGraph.nodes().contains(ephemeralNode));
        assertFalse(globalGraph.edges().contains(ephemeralEdge));
    }

    @Test
    public void ephemeralGraphRejectsGlobalElements() {
        GlobalGraph globalGraph = new GlobalGraph();
        EphemeralGraph ephemeralGraph = new EphemeralGraph(new Universe());

        Node globalNode = globalGraph.factory().createNode();
        Edge globalEdge = globalGraph.factory().createEdge(globalNode, globalNode);

        assertThrows(IllegalArgumentException.class, () -> ephemeralGraph.addNode(globalNode));
        assertThrows(IllegalArgumentException.class, () -> ephemeralGraph.addEdge(globalEdge));
        assertFalse(ephemeralGraph.nodes().contains(globalNode));
        assertFalse(ephemeralGraph.edges().contains(globalEdge));
    }

    @Test
    public void globalGraphRejectsEphemeralGraphsInSetOperations() {
        GlobalGraph globalGraph = new GlobalGraph();
        EphemeralGraph ephemeralGraph = new EphemeralGraph(new Universe());

        Node globalNode = globalGraph.factory().createNode();
        globalGraph.addNode(globalNode);

        Node ephemeralNode = ephemeralGraph.factory().createNode();
        ephemeralGraph.addNode(ephemeralNode);

        assertThrows(IllegalArgumentException.class, () -> globalGraph.union(ephemeralGraph));
        assertThrows(IllegalArgumentException.class, () -> globalGraph.union(ephemeralNode));
    }

    @Test
    public void ephemeralGraphRejectsGlobalGraphsInSetOperations() {
        GlobalGraph globalGraph = new GlobalGraph();
        EphemeralGraph ephemeralGraph = new EphemeralGraph(new Universe());

        Node globalNode = globalGraph.factory().createNode();
        globalGraph.addNode(globalNode);

        Node ephemeralNode = ephemeralGraph.factory().createNode();
        ephemeralGraph.addNode(ephemeralNode);

        assertThrows(IllegalArgumentException.class, () -> ephemeralGraph.union(globalGraph));
        assertThrows(IllegalArgumentException.class, () -> ephemeralGraph.union(globalNode));
        assertThrows(IllegalArgumentException.class, () -> ephemeralGraph.difference(globalGraph));
        assertThrows(IllegalArgumentException.class, () -> ephemeralGraph.intersection(globalGraph));
    }

    @Test
    public void nodeSetRejectsCrossContaminationInSetOperations() {
        GlobalGraph globalGraph = new GlobalGraph();
        EphemeralGraph ephemeralGraph = new EphemeralGraph(new Universe());

        Node globalNode = globalGraph.factory().createNode();
        globalGraph.addNode(globalNode);
        NodeSet globalNodeSet = globalGraph.nodes();
        NodeSet globalImmutableNodeSet = globalNodeSet.toImmutable();

        Node ephemeralNode = ephemeralGraph.factory().createNode();
        ephemeralGraph.addNode(ephemeralNode);
        NodeSet ephemeralNodeSet = ephemeralGraph.nodes();
        NodeSet ephemeralImmutableNodeSet = ephemeralNodeSet.toImmutable();

        // GlobalNodeSet implicitly filters elements through instanceof checks without throwing an exception.
        NodeSet unionResult = globalNodeSet.union(ephemeralNodeSet);
        assertTrue(unionResult.contains(globalNode));
        assertFalse(unionResult.contains(ephemeralNode));

        // EphemeralUnmodifiableLiveNodeSet implicitly filters elements without throwing an exception.
        NodeSet ephemeralUnionResult = ephemeralNodeSet.union(globalNodeSet);
        assertTrue(ephemeralUnionResult.contains(ephemeralNode));
        assertFalse(ephemeralUnionResult.contains(globalNode));

        assertFalse(globalNodeSet.contains(ephemeralNode));
        assertFalse(globalImmutableNodeSet.contains(ephemeralNode));
        assertFalse(ephemeralNodeSet.contains(globalNode));
        assertFalse(ephemeralImmutableNodeSet.contains(globalNode));
    }

    @Test
    public void edgeSetRejectsCrossContaminationInSetOperations() {
        GlobalGraph globalGraph = new GlobalGraph();
        EphemeralGraph ephemeralGraph = new EphemeralGraph(new Universe());

        Node globalNode = globalGraph.factory().createNode();
        Edge globalEdge = globalGraph.factory().createEdge(globalNode, globalNode);
        globalGraph.addEdge(globalEdge);
        EdgeSet globalEdgeSet = globalGraph.edges();
        EdgeSet globalImmutableEdgeSet = globalEdgeSet.toImmutable();

        Node ephemeralNode = ephemeralGraph.factory().createNode();
        Edge ephemeralEdge = ephemeralGraph.factory().createEdge(ephemeralNode, ephemeralNode);
        ephemeralGraph.addEdge(ephemeralEdge);
        EdgeSet ephemeralEdgeSet = ephemeralGraph.edges();
        EdgeSet ephemeralImmutableEdgeSet = ephemeralEdgeSet.toImmutable();

        EdgeSet unionResult = globalEdgeSet.union(ephemeralEdgeSet);
        assertTrue(unionResult.contains(globalEdge));
        assertFalse(unionResult.contains(ephemeralEdge));

        EdgeSet ephemeralUnionResult = ephemeralEdgeSet.union(globalEdgeSet);
        assertTrue(ephemeralUnionResult.contains(ephemeralEdge));
        assertFalse(ephemeralUnionResult.contains(globalEdge));

        assertFalse(globalEdgeSet.contains(ephemeralEdge));
        assertFalse(globalImmutableEdgeSet.contains(ephemeralEdge));
        assertFalse(ephemeralEdgeSet.contains(globalEdge));
        assertFalse(ephemeralImmutableEdgeSet.contains(globalEdge));
    }

    @Test
    public void operationsWithEmptySetSucceed() {
        GlobalGraph globalGraph = new GlobalGraph();
        EphemeralGraph ephemeralGraph = new EphemeralGraph(new Universe());

        Node globalNode = globalGraph.factory().createNode();
        globalGraph.addNode(globalNode);
        NodeSet globalNodeSet = globalGraph.nodes();

        Node ephemeralNode = ephemeralGraph.factory().createNode();
        ephemeralGraph.addNode(ephemeralNode);
        NodeSet ephemeralNodeSet = ephemeralGraph.nodes();

        assertDoesNotThrow(() -> globalNodeSet.union(NodeSet.empty()));
        assertDoesNotThrow(() -> ephemeralNodeSet.union(NodeSet.empty()));
        assertDoesNotThrow(() -> globalNodeSet.difference(NodeSet.empty()));
        assertDoesNotThrow(() -> ephemeralNodeSet.difference(NodeSet.empty()));

        EdgeSet globalEdgeSet = globalGraph.edges();
        EdgeSet ephemeralEdgeSet = ephemeralGraph.edges();

        assertDoesNotThrow(() -> globalEdgeSet.union(EdgeSet.empty()));
        assertDoesNotThrow(() -> ephemeralEdgeSet.union(EdgeSet.empty()));
        assertDoesNotThrow(() -> globalEdgeSet.difference(EdgeSet.empty()));
        assertDoesNotThrow(() -> ephemeralEdgeSet.difference(EdgeSet.empty()));
    }

    @Test
    public void testShadowSetRejectsForeignUniverse() {
        // Setup a shadow set in Universe A
        Universe universeA = new Universe();
        EphemeralGraph transactionA = new EphemeralGraph(universeA);
        ShadowNodeSet shadowSetA = new ShadowNodeSet(transactionA, new dev.chpg.pg.multiverse.universe.UniverseNodeSet(universeA, new java.util.BitSet()));

        // Setup a shadow set in Universe B
        Universe universeB = new Universe();
        EphemeralGraph transactionB = new EphemeralGraph(universeB);
        ShadowNodeSet shadowSetB = new ShadowNodeSet(transactionB, new dev.chpg.pg.multiverse.universe.UniverseNodeSet(universeB, new java.util.BitSet()));

        // The test must now assert that the firewall holds and throws an exception
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> shadowSetA.union(shadowSetB),
            "Expected union to fail fast on cross-universe contamination"
        );

        assertTrue(exception.getMessage().contains("Cross-universe contamination"));
    }
}
