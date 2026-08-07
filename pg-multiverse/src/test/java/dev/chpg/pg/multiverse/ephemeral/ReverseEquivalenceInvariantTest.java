package dev.chpg.pg.multiverse.ephemeral;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.Edge;
import dev.chpg.pg.multiverse.universe.Universe;

public class ReverseEquivalenceInvariantTest {

    @Test
    public void testReverseEquivalenceGraphAndSetAndNode() {
        Universe universe = new Universe();
        EphemeralGraph graph = new EphemeralGraph(universe);

        Node a = graph.factory().createNode();
        Node b = graph.factory().createNode();
        Node c = graph.factory().createNode();
        Node d = graph.factory().createNode();

        graph.addNode(a);
        graph.addNode(b);
        graph.addNode(c);
        graph.addNode(d);

        Edge e1 = graph.factory().createEdge(a, b);
        Edge e2 = graph.factory().createEdge(b, c);
        Edge e3 = graph.factory().createEdge(c, d);

        graph.addEdge(e1);
        graph.addEdge(e2);
        graph.addEdge(e3);

        Graph fromGraph = graph.induce(e1);

        Graph reverseGraph = graph.reverse(fromGraph);
        Graph reverseSet = graph.reverse(fromGraph.nodes());

        assertEquals(reverseSet.nodes().size(), reverseGraph.nodes().size());
        assertEquals(reverseSet.edges().size(), reverseGraph.edges().size());

        assertTrue(reverseGraph.nodes().containsAll(reverseSet.nodes()));
        assertTrue(reverseSet.nodes().containsAll(reverseGraph.nodes()));

        assertTrue(reverseGraph.edges().containsAll(reverseSet.edges()));
        assertTrue(reverseSet.edges().containsAll(reverseGraph.edges()));

        Graph reverseNode = graph.reverse(b);
        Graph reverseNodeSet = graph.reverse(graph.singleton(b));

        assertEquals(reverseNodeSet.nodes().size(), reverseNode.nodes().size());
        assertEquals(reverseNodeSet.edges().size(), reverseNode.edges().size());

        assertTrue(reverseNode.nodes().containsAll(reverseNodeSet.nodes()));
        assertTrue(reverseNodeSet.nodes().containsAll(reverseNode.nodes()));
    }
}
