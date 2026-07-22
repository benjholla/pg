package dev.chpg.pg.multiverse.ephemeral;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;

public class ReadmeExampleTest {
    private static final EphemeralFactory factory = new EphemeralGraph().factory();

    @Test
    public void testReadmeExample() {
        // Create nodes
        Node alice = factory.createNode();
        alice.tags().add("Person");
        alice.attributes().put("name", "Alice");

        Node bob = factory.createNode();
        bob.tags().add("Person");
        bob.attributes().put("name", "Bob");

        Node charlie = factory.createNode();
        charlie.tags().add("Person");
        charlie.attributes().put("name", "Charlie");

        // Create edges
        Edge knows1 = factory.createEdge(alice, bob);
        knows1.tags().add("knows");

        Edge knows2 = factory.createEdge(bob, charlie);
        knows2.tags().add("knows");

        // Instantiate a graph
        Graph graph = factory.createGraph(new EphemeralNodeSet(alice, bob, charlie));
        graph.addEdge(knows1);
        graph.addEdge(knows2);

        // Perform set-theoretic operations
        // E.g., Find nodes and edges starting from Alice (forward transitive traversal)
        Graph aliceNetwork = graph.forward(alice);
        assertEquals(3, aliceNetwork.nodes().size()); // 3 (Alice, Bob, Charlie)

        // Take a single step backwards from Charlie
        Graph reverseFromCharlie = graph.reverseStep(charlie);
        assertEquals(2, reverseFromCharlie.nodes().size()); // 2 (Bob, Charlie)

        // Combine (Union) subgraphs
        Graph combined = aliceNetwork.union(reverseFromCharlie);
        assertEquals(3, combined.nodes().size());
    }
}
