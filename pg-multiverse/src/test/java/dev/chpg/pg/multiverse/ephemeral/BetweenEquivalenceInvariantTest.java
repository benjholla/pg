package dev.chpg.pg.multiverse.ephemeral;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.multiverse.universe.Universe;

public class BetweenEquivalenceInvariantTest {

    @Test
    public void testBetweenEquivalence() {
        Universe universe = new Universe();
        EphemeralGraph graph = new EphemeralGraph(universe);

        Node a = graph.factory().createNode();
        Node b = graph.factory().createNode();
        Node c = graph.factory().createNode();
        Node d = graph.factory().createNode();
        Node e = graph.factory().createNode();

        graph.addNode(a);
        graph.addNode(b);
        graph.addNode(c);
        graph.addNode(d);
        graph.addNode(e);

        Edge e1 = graph.factory().createEdge(a, b);
        Edge e2 = graph.factory().createEdge(b, c);
        Edge e3 = graph.factory().createEdge(c, d);
        Edge e4 = graph.factory().createEdge(a, e);
        Edge e5 = graph.factory().createEdge(e, d);

        graph.addEdge(e1);
        graph.addEdge(e2);
        graph.addEdge(e3);
        graph.addEdge(e4);
        graph.addEdge(e5);

        Graph between = graph.between(a, d);
        Graph equivalent = graph.forward(a).intersection(graph.reverse(d));

        assertEquals(equivalent.nodes().size(), between.nodes().size());
        assertEquals(equivalent.edges().size(), between.edges().size());

        for (Node n : equivalent.nodes()) {
            assertEquals(true, between.nodes().contains(n));
        }
        for (Edge ed : equivalent.edges()) {
            assertEquals(true, between.edges().contains(ed));
        }
    }
}
