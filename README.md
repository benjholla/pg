# pg

`pg` is a lightweight, intuitive property graph library for Java. It is designed to provide powerful set-theoretic graph operations and transitive traversals on a flexible property graph model.

## Why `pg`?

Graphs are everywhere, but many graph libraries focus on heavy integration with databases or introduce overly complex abstractions. `pg` exists to provide a clean, memory-backed property graph model where you can effortlessly:

- Compose graphs using sets of nodes and edges.
- Perform robust set operations like `union`, `difference`, and `intersection`.
- Navigate seamlessly with operations like `forward`, `between`, and `induce`.
- Decorate graphs with dynamic `tags` and `attributes` for powerful filtering.

## Core Abstractions

- `GraphElement`: The base interface for both nodes and edges. Elements have a unique primitive `int` ID, a `TagSet` for boolean markers, and an `attributes` map for arbitrary key-value properties.
- `Node`: Represents a vertex in the graph.
- `Edge`: Represents a directed connection between a `from` node and a `to` node.
- `HeavyGraph`: The default, in-memory implementation of a graph. It supports creating new subgraphs through composable, set-theoretic operations.

## Quick Start

```java
import io.github.benjholla.pg.api.Node;
import io.github.benjholla.pg.api.Edge;
import io.github.benjholla.pg.heavy.HeavyNode;
import io.github.benjholla.pg.heavy.HeavyEdge;
import io.github.benjholla.pg.heavy.HeavyGraph;
import io.github.benjholla.pg.api.Graph;

public class Example {
    public static void main(String[] args) {
        // Create nodes
        Node alice = new HeavyNode();
        alice.tags().add("Person");
        alice.attributes().put("name", "Alice");

        Node bob = new HeavyNode();
        bob.tags().add("Person");
        bob.attributes().put("name", "Bob");

        Node charlie = new HeavyNode();
        charlie.tags().add("Person");
        charlie.attributes().put("name", "Charlie");

        // Create edges
        Edge knows1 = new HeavyEdge(alice, bob);
        knows1.tags().add("knows");

        Edge knows2 = new HeavyEdge(bob, charlie);
        knows2.tags().add("knows");

        // Instantiate a graph
        HeavyGraph graph = new HeavyGraph(alice, bob, charlie);
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

## Test Coverage

This project uses Jacoco to track test coverage. To generate the coverage report, run the following command:

```bash
./gradlew test jacocoTestReport
```

After the command completes successfully, you can view the HTML coverage report by opening the following file in your web browser:

```
pg-heavy/build/reports/jacoco/test/html/index.html
```
