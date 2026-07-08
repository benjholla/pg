# pg

`pg` is a lightweight, intuitive property graph library for Java. It is designed to provide powerful set-theoretic graph operations and transitive traversals on a flexible property graph model.

## Why `pg`?

Graphs are everywhere, but many graph libraries focus on global integration with databases or introduce overly complex abstractions. `pg` exists to provide a clean, memory-backed property graph model where you can effortlessly:

- Compose graphs using sets of nodes and edges.
- Perform robust set operations like `union`, `difference`, and `intersection`.
- Navigate seamlessly with operations like `forward`, `between`, and `induce`.
- Decorate graphs with dynamic `tags` and `attributes` for powerful filtering.

### The Competitor Landscape

To understand where `pg` fits, it is helpful to contrast it with the three existing archetypes of Java graph libraries:

**1. JGraphT (The Algorithmic Giant)**
*   **Their Focus:** JGraphT is the academic standard for graph *algorithms* (Dijkstra, A*, flow networks).
*   **The Contrast:** It relies heavily on Java generics (`Graph<V, E>`), which often leads to massive object allocation overhead and verbose type signatures. It is not fundamentally designed as a "property graph" (where nodes/edges have arbitrary tags and key-value attributes). The `pg-api` interface is vastly cleaner, shielding the user from generic soup while enforcing a strict property-graph data model.

**2. Apache TinkerPop / Gremlin (The Database Behemoth)**
*   **Their Focus:** This is the industry standard for property graphs, backed by a massive ecosystem, designed to act as a driver for distributed databases (like Neo4j or AWS Neptune).
*   **The Contrast:** TinkerPop is incredibly global. Its traversal language (Gremlin) relies on complex, side-effect-global iterators. If you just want an isolated, blazing-fast in-memory graph to manipulate data and pipe it to a visualizer, TinkerPop is like bringing a battleship to a knife fight. `pg`'s O(1) primitive routing and lightweight mechanical isolation completely undercut TinkerPop's overhead.

**3. Google Guava common.graph (The Lightweight Utility)**
*   **Their Focus:** Guava provides beautiful, modern, lightweight graph interfaces.
*   **The Contrast:** Guava provides basic topology, but it deliberately lacks rich property graph semantics (no native `AttributeMap` or `TagSet`). More importantly, it does not provide the set-theoretic algebra that `pg` is built upon.

### The Niche `pg` Dominates

`pg` fills a critical void between the global database drivers and the purely academic algorithmic libraries. It is a highly specialized, precision engine—an uncompromising, memory-efficient staging and analysis engine.

*   **The Set-Theoretic Algebra:** This is `pg`'s biggest differentiator. In most graph libraries, extracting a subgraph requires writing a custom iterator, filtering elements, and manually assembling a new graph. By putting `difference()`, `union()`, `intersection()`, and `forwardStep()` directly on the interface—and mandating that they return *new, induced subgraphs* rather than mutating the root—`pg` provides a functional query algebra. You can carve out slices of a massive AST or data-flow graph mathematically, without permanently destroying the original data structure. (Note: The API for these fluent mathematical operations was heavily inspired by [EnSoft Atlas](https://www.ensoftcorp.com/products/atlas/) and its MIT-licensed open-source [sandbox implementation](https://github.com/EnSoftCorp/toolbox-commons/blob/master/com.ensoftcorp.open.commons%2Fsrc%2Fcom%2Fensoftcorp%2Fopen%2Fcommons%2Fsandbox%2FSandboxGraph.java), but reimagined from the ground up to blend with modern Java APIs and practices. It is also fundamentally backend-agnostic, meaning the same operations could easily delegate to databases like Neo4j or TinkerPop, and is completely decoupled from Eclipse and any specific program analysis ecosystem).
*   **The Mechanical vs. Mathematical Boundary:** `pg` successfully decouples the mathematical contract from the mechanical storage. Downstream consumers interact with a purely set-theoretic interface (`pg-api`), completely oblivious to the fact that under the hood, implementations like `GlobalGraph` are executing queries using highly defensive, zero-allocation primitive Integer maps.
*   **Strict Pipeline Defenses:** Because `pg` designed strict boundaries like `linkEdge` (which violently rejects missing anchors or foreign types) and avoids auto-vivification in the core pipeline, this engine is perfectly suited for complex polyglot environments. When transferring JSON schemas between a Java backend, a TypeScript visualizer, or a C++ desktop plotting engine, silent data corruption is fatal. The `pg` API guarantees that if a graph instantiates successfully, its topology is mathematically sound.

## Core Abstractions

- `GraphElement`: The base interface for both nodes and edges. Elements have a unique primitive `int` ID, a `TagSet` for boolean markers, and an `attributes` map for arbitrary key-value properties.
- `Node`: Represents a vertex in the graph.
- `Edge`: Represents a directed connection between a `from` node and a `to` node.
- `GlobalGraph`: The default, in-memory implementation of a graph. It supports creating new subgraphs through composable, set-theoretic operations.

## Quick Start

*Note: This example is continuously validated by the executable test `ReadmeExampleTest.java` to ensure it remains synchronized with the implementation.*

```java
import io.github.benjholla.pg.api.Node;
import io.github.benjholla.pg.api.Edge;
import io.github.benjholla.pg.global.GlobalNode;
import io.github.benjholla.pg.global.GlobalEdge;
import io.github.benjholla.pg.global.GlobalGraph;
import io.github.benjholla.pg.api.Graph;

public class Example {
    public static void main(String[] args) {
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

## Documentation & Architecture

For deeper insights into the design, performance characteristics, and query optimizations of `pg`, please see the architecture documentation:
* [Deferred Execution Engine](docs/architecture/DeferredGraph.md)
* [Deferred Graph Optimizations](docs/architecture/DeferredGraphOptimizations.md)
* [Performance Goals](docs/architecture/PerformanceGoals.md)

## Installation

Add `pg` to your `build.gradle` or `pom.xml` dependencies using standard Maven configurations (e.g. from Maven Central or your local repository if published).

## Test Coverage

This project uses Jacoco to track test coverage. To generate the coverage report, run the following command:

```bash
./gradlew test jacocoTestReport
```

After the command completes successfully, you can view the HTML coverage report by opening the following file in your web browser:

```
pg-global/build/reports/jacoco/test/html/index.html
```
