package dev.chpg.pg.multiverse.universe;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.Graph;
import dev.chpg.pg.multiverse.ephemeral.EphemeralGraph;
import dev.chpg.pg.multiverse.ephemeral.EphemeralNode;

public class UniverseGraphInvariantTest {

    private Universe universe;
    private Graph graph;

    @BeforeEach
    public void setUp() {
        universe = new Universe();
        EphemeralGraph eg = new EphemeralGraph();

        EphemeralNode a = eg.createNode();
        EphemeralNode b = eg.createNode();
        EphemeralNode c = eg.createNode();

        eg.addEdge(eg.createEdge(a, b));
        eg.addEdge(eg.createEdge(b, c));

        graph = universe.promote(eg);
    }

    @Test
    public void testEdgeEndpointExistenceInvariant() {
        for (Edge edge : graph.edges()) {
            assertTrue(graph.nodes().contains(edge.from()), "Graph must contain the source node of every edge");
            assertTrue(graph.nodes().contains(edge.to()), "Graph must contain the target node of every edge");
        }
    }
}
