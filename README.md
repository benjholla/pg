# pg

`pg` is a lightweight, intuitive property graph library for Java. It is designed to provide powerful set-theoretic graph operations and transitive traversals on a flexible property graph model.

## Why `pg`?

Graphs are everywhere, but many graph libraries focus on heavy integration with databases or introduce overly complex abstractions. `pg` exists to provide a clean, memory-backed property graph model where you can effortlessly:

- Compose graphs using sets of nodes and edges.
- Perform robust set operations like `union`, `difference`, and `intersection`.
- Navigate seamlessly with operations like `forward`, `between`, and `induce`.
- Decorate graphs with dynamic `tags` and `attributes` for powerful filtering.

## Core Abstractions

- `GraphElement`: The base class for both nodes and edges. Elements have a unique `ElementId`, a `TagSet` for boolean markers, and an `attributes` map for arbitrary key-value properties.
- `Node`: Represents a vertex in the graph.
- `Edge`: Represents a directed connection between a `from` node and a `to` node.
- `PropertyGraph`: The default, in-memory implementation of a graph. It supports creating new subgraphs through composable, set-theoretic operations.

## Quick Start

```java
import io.github.benjholla.pg.Node;
import io.github.benjholla.pg.Edge;
import io.github.benjholla.pg.PropertyGraph;
import io.github.benjholla.pg.Graph;

public class Example {
    public static void main(String[] args) {
        // Create nodes
        Node alice = new Node();
        alice.tags().add("Person");
        alice.putAttr("name", "Alice");

        Node bob = new Node();
        bob.tags().add("Person");
        bob.putAttr("name", "Bob");

        Node charlie = new Node();
        charlie.tags().add("Person");
        charlie.putAttr("name", "Charlie");

        // Create edges
        Edge knows1 = new Edge(alice, bob);
        knows1.tags().add("knows");

        Edge knows2 = new Edge(bob, charlie);
        knows2.tags().add("knows");

        // Instantiate a graph
        PropertyGraph graph = new PropertyGraph(alice, bob, charlie);
        graph.add(knows1);
        graph.add(knows2);

        // Perform set-theoretic operations
        // E.g., Find nodes and edges starting from Alice (forward transitive traversal)
        Graph aliceNetwork = graph.forward(alice);
        System.out.println("Nodes reachable from Alice: " + aliceNetwork.nodes().size()); // 3 (Alice, Bob, Charlie)

        // Take a single step backwards from Charlie
        Graph reverseFromCharlie = graph.reverseStep(charlie);
        System.out.println("One step back from Charlie includes: " + reverseFromCharlie.nodes().size() + " nodes"); // 2 (Bob, Charlie)

        // Combine (Union) subgraphs
        Graph combined = aliceNetwork.union(reverseFromCharlie);
        System.out.println("Combined nodes: " + combined.nodes().size());
    }
}
```

## Installation

Add `pg` to your `build.gradle` or `pom.xml` dependencies using standard Maven configurations (e.g. from Maven Central or your local repository if published).
