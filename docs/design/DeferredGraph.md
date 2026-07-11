# Architectural Brief: The Deferred Query Engine (DeferredGraph)
## 1. Overview & Philosophy
The foundational pg-api set-theoretic algebra is inherently **eager**: calling graph.forward(node) immediately computes the traversal and allocates memory for the resulting subgraph. While fast for localized operations, chaining complex, multi-step queries across massive datasets (e.g., full-program ASTs or CFGs) requires query optimization to prevent unnecessary memory allocations and redundant traversals.
To solve this, the ecosystem supports a **Deferred Execution Layer (Lazy Evaluation)**. This layer implements the Decorator/Builder pattern over the core Graph interface. Instead of executing topological math immediately, it constructs an Abstract Syntax Tree (AST) of the query. The query is only optimized and executed against the underlying storage engine (pg-global or pg-multiverse) when a terminal operation demands the physical data.
## 2. The Core Mechanics
The deferred execution model relies on the DeferredGraph, which perfectly masquerades as a standard Graph to the external caller.
 * **The AST Builder:** Calling intermediate algebraic methods (forward(), union(), difference()) on a DeferredGraph does not touch internal maps or bitsets. It simply returns a new DeferredGraph wrapping the next ASTNode (e.g., ForwardOp(sourceNode)).
 * **The Query Optimizer:** Before execution, the engine traverses the AST to reorder operations for maximum efficiency. For example, the optimizer can move a highly restrictive .filter("isVulnerable") operation to execute *before* a massive transitive .forward() traversal, drastically reducing the intermediate memory footprint.
 * **The Factory Anchor:** The DeferredGraph inherits and securely holds a reference to the GraphFactory that initiated the query. When it eventually compiles and executes the AST, it uses this exact factory to stamp out the final, concrete result graph, guaranteeing memory layout consistency and preventing cross-engine contamination.
## 3. The Operation Boundaries
The API strictly separates operations into two categories to dictate exactly when execution occurs.
### Intermediate Operations (AST Builders)
These methods construct the execution plan. They return composable elements and trigger absolutely zero computation against the backend.
 * **Graph Algebra:** forward(), reverse(), union(), intersection(), between()
 * **Set Selectors:** nodes(), edges(), selectNodes(), filter()
### Terminal Operations (Execution Triggers)
Any method that breaks out of the composable Graph/NodeSet/EdgeSet abstractions and requests a standard Java type or primitive forces the engine to compile the AST and evaluate the topology.
 * **Existence Checks:** containsNode(), containsEdge(), adjacent()
 * **Targeted Lookups:** node(id), edge(id)
 * **Metrics & Routing:** isEmpty(), size(), degree(), ids(), toIdArray()
 * **Extraction:** iterator(), toArray(), forEach(), one()
## 4. Explicit Concretization: The materialize() Contract
To support complex query branching—where an analyst computes an expensive intermediate subgraph and wants to run multiple independent traversals from it without re-computing the base query—the Graph interface defines an explicit evaluation boundary. The name materialize() borrows heavily from data engineering to clearly communicate to the developer that a memory-intensive allocation is about to occur.
```java
/**
 * Forces the materialization of this graph.
 * For lazy implementations, this compiles the query and computes the physical graph.
 * For concrete implementations, this safely returns itself (no-op).
 */
Graph materialize();

```
 * **Eager Engines (GlobalGraph, UniverseGraph):** Implementation is a strict O(1) no-op (return this;), as the topology is already physically realized in memory.
 * **Lazy Engines (DeferredGraph):** Triggers the query optimizer, executes the AST against the underlying backend, and returns a fully populated, concrete Graph (e.g., a GlobalGraph or EphemeralGraph), permanently severing the lazy chain.
## 5. Execution Safety & JVM Protections
Because chained deferred operations create a deeply nested AST, recursive evaluation models risk crashing the JVM with a StackOverflowError when executing highly iterative graph algorithms (e.g., fixed-point analysis). To guarantee engine stability, DeferredGraph employs strict structural safeguards:
 * **Heap-Allocated Iterative Evaluation:** Instead of relying on the JVM call stack, materialize() executes the AST using an explicit Post-Order Traversal managed on the heap (via java.util.ArrayDeque). This allows the engine to safely evaluate ASTs of massive depth (50,000+ operations) without stacking execution frames.
 * **Auto-Materialization Circuit Breaker:** To prevent a user from inadvertently hoarding memory by chaining unexecuted operations in a runaway while loop, every DeferredGraph tracks its AST depth. If the internal depth exceeds a configured safety threshold (e.g., 500 nodes), the builder automatically triggers materialize(), computes the intermediate graph, and starts a fresh lazy chain.
 * **AST Compaction:** The builder inspects sequential operations during construction. It flattens repetitive, linear chains into singular, optimized nodes (e.g., collapsing 100 consecutive .forward() calls into a single ForwardOp(steps = 100)) to keep the AST shallow and prime it for batch evaluation algorithms.
## 6. Deployment Strategies
This deferred execution engine can be injected at two distinct tiers of the architecture:
 * **Tier 1 (pg-query):** A purely topological lazy evaluator that sits alongside pg-global and pg-multiverse. It optimizes raw structural set-math.
 * **Tier 2 (chpg-core):** A semantic query engine. Because resolving semantic projections (e.g., architectural boundaries, naming conventions) is computationally expensive, deferring execution here allows the engine to wait until a complete semantic question is constructed before deciding which raw topological slices of the underlying flat graph to hydrate.