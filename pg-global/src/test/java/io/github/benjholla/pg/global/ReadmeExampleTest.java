package io.github.benjholla.pg.global;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import io.github.benjholla.pg.api.Edge;
import io.github.benjholla.pg.api.Graph;
import io.github.benjholla.pg.api.Node;

public class ReadmeExampleTest {
    @Test
    public void testReadmeExample() {
        // Create nodes
        Node alice = new GlobalNode();
        alice.tags().add("Person");
        alice.attributes().put("name", "Alice");

        Node bob = new GlobalNode();
        bob.tags().add("Person");
        bob.attributes().put("name", "Bob");

        Node charlie = new GlobalNode();
        charlie.tags().add("Person");
        charlie.attributes().put("name", "Charlie");

        // Create edges
        Edge knows1 = new GlobalEdge(alice, bob);
        knows1.tags().add("knows");

        Edge knows2 = new GlobalEdge(bob, charlie);
        knows2.tags().add("knows");

        // Instantiate a graph
        GlobalGraph graph = new GlobalGraph(alice, bob, charlie);
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
