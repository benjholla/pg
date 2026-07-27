package dev.chpg.pg.multiverse.universe;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Graph;
import dev.chpg.pg.multiverse.ephemeral.EphemeralGraph;
import dev.chpg.pg.multiverse.ephemeral.EphemeralNode;
import dev.chpg.pg.api.Node;

public class UniverseGraphTest {

    private Universe universe;
    private Graph fullGraph;
    private Node startNode;

    @BeforeEach
    public void setUp() {
        universe = new Universe();

        EphemeralGraph eg = new EphemeralGraph();
        EphemeralNode n1 = eg.createNode(); n1.tags().add("start");
        EphemeralNode n2 = eg.createNode();
        EphemeralNode n3 = eg.createNode();

        eg.addEdge(eg.createEdge(n1, n2));
        eg.addEdge(eg.createEdge(n2, n3));

        fullGraph = universe.promote(eg);

        for (Node n : fullGraph.nodes()) {
            if (n.tags().contains("start")) {
                startNode = n;
                break;
            }
        }
    }

    @Test
    public void testForwardTraversal() {
        Graph fwd = fullGraph.forward(startNode);
        assertEquals(3, fwd.nodes().size());
        assertEquals(2, fwd.edges().size());
    }

    @Test
    public void testForwardStep() {
        Graph step = fullGraph.forwardStep(startNode);
        assertEquals(2, step.nodes().size());
        assertEquals(1, step.edges().size());
    }
}
