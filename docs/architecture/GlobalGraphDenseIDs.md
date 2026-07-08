# GlobalGraph Dense IDs

## Overview

The `GlobalGraph` is the "primary data store" of the pg ecosystem. It is distinguished from the `EphemeralGraph` primarily by its **memory layout**. The defining characteristic of `GlobalGraph` is that its element IDs (nodes and edges) are **dense and 0-indexed**.

This dense, 0-indexed structure provides a critical performance advantage: it allows the graph to back properties, tags, and adjacency lists with raw arrays rather than hash maps.

### GlobalGraph vs EphemeralGraph Memory Layout

Both engines strictly implement the same `Graph` interface, meaning both fully support properties and tags. The distinction lies in their memory layout:

*   **GlobalGraph:** Because its IDs are dense and start at 0, it can back its properties and topology with raw arrays (e.g., `AttributeMap[] properties = new AttributeMap[size]`). This provides O(1) lookup speed with perfect cache locality, making it the ultimate engine for global, permanent data querying.
*   **EphemeralGraph:** Because its IDs are negative, it must rely on HashMaps (`IntObjectMap`) for property lookups. It incurs a slight hashing overhead, which is the mathematically necessary trade-off for acting as a temporary, isolated scratchpad.

## The Problem: Dynamic Sizing vs Array Performance

A graph engine that requires pre-allocation of the exact number of nodes and edges at instantiation is severely limited, particularly during data parsing or iterative graph generation.

However, standard Java collections like `java.util.ArrayList` cannot be used to achieve dynamic sizing because they introduce an unacceptable performance penalty that destroys the hyper-optimized memory layout required by `GlobalGraph`.

### The ArrayList Object Boxing Penalty

The core advantage of dense, 0-indexed IDs is the ability to use raw primitives (like `int`) for topological routing.

Standard Java collections cannot hold primitives. If an `ArrayList<Integer>` is used for adjacency lists:

1.  **Boxing Overhead:** The JVM is forced to box every single `int` ID into an `Integer` object. A raw `int` takes 4 bytes. An `Integer` object takes 16 to 24 bytes (object header + payload).
2.  **Cache Locality Destruction:** More importantly, an array of `Integer` objects is actually an array of pointers pointing to randomly scattered memory locations across the heap. This completely shatters CPU L1 cache locality, causing massive cache misses during graph traversals.

## The Solution: Dynamic Primitive Arrays

To achieve dynamic sizing without sacrificing raw array performance, `GlobalGraph` utilizes **Dynamic Primitive Arrays**.

Under the hood, `java.util.ArrayList` uses a raw `Object[]` array. When full, it allocates a new, larger array and uses hardware-accelerated `System.arraycopy()` to move the data.

`GlobalGraph` applies this exact same strategy, but does so directly using raw `int[]` and element arrays (like `Node[]` or `AttributeMap[]`) to completely avoid the object wrapper overhead.

### Internal Mechanics

The internal management of dynamic primitive arrays inside `GlobalGraph` follows this pattern:

```java
public final class GlobalGraph implements Graph {
    // Start with a reasonable default capacity
    private int[] nodeIds = new int[1024];
    private int[][] outgoingEdges = new int[1024][];

    private int nextNodeId = 0;

    public GlobalNode createNode() {
        int id = nextNodeId++;

        // The Dynamic Resize Trigger
        if (id == nodeIds.length) {
            growArrays();
        }

        nodeIds[id] = id;
        return new GlobalNode(id);
    }

    private void growArrays() {
        // Double the capacity (or grow by 1.5x)
        int newCapacity = nodeIds.length * 2;

        // Hardware-accelerated contiguous memory copy
        int[] newNodeIds = new int[newCapacity];
        System.arraycopy(nodeIds, 0, newNodeIds, 0, nodeIds.length);
        this.nodeIds = newNodeIds;

        int[][] newOutgoing = new int[newCapacity][];
        System.arraycopy(outgoingEdges, 0, newOutgoing, 0, outgoingEdges.length);
        this.outgoingEdges = newOutgoing;
    }
}
```

## Architectural Payoff

By manually managing primitive arrays (or utilizing primitive collection libraries that perform this logic under the hood), `GlobalGraph` achieves:

1.  **Infinite Mutability:** Analysts can add nodes and edges dynamically forever without pre-calculating sizes.
2.  **Zero Boxing:** Adjacency checks and ID storage remain pure primitive math.
3.  **Perfect Density:** The arrays remain contiguous, 0-indexed, and perfectly packed for the CPU cache, preserving maximum traversal speed.

## Clarification: Taxonomy and the Multi-Universe Ecosystem

To resolve semantic drift and create a clear mental model, the terminology around `GlobalGraph` and the module structure is redefined as follows:

### 1. `pg-global`: The Unified Baseline

*   **Renaming:** `GlobalGraph` (in `pg-global`) will transition to `GlobalGraph` (in `pg-global`).
*   **Global is implicitly 1 universe:** A developer immediately understands that if they instantiate two `GlobalGraph`s, they are pulling from the same global sequence. Unions will never collide, merging is effortless, and they don't need to worry about lineage validation.
*   **The Split Singleton:** It still respects the domain boundary (separate global Node and Edge counters), but remains the forgiving, easy-to-use, object-oriented baseline.

### 2. `pg-universe`: The Multi-Universe Engine

This perfectly frames `pg-universe` as the domain of strict, high-performance isolation. If a developer needs highly parallelized graph construction, isolated namespaces, or multi-stage semantic projections, they graduate from `pg-global` to `pg-universe`.

### 3. The Array-Backed Ephemeral Variant

Inside `pg-universe`, we can add another variant of `EphemeralGraph` that uses arrays. This bridges the gap between temporary mutations and raw speed, offering dynamic primitive array performance while remaining strictly within the isolated universe lifecycle.

*   **Array Index Masking:** Because standard `EphemeralGraph` IDs are negative (-1, -2, -3), you cannot plug them directly into a Java array. You apply a mathematical mask to convert the negative ID into a 0-based array index.
    *   **The Math:** `int arrayIndex = Math.abs(id) - 1;`
    *   **The Result:** ID -1 routes to array index 0. ID -2 routes to array index 1. ID -100 routes to array index 99.

This absolute-value math provides the L1 cache speed of primitive arrays while maintaining the strict negative-ID firewall that protects the UniverseGraph from contamination. The specific class name for this new array variant (e.g., `DenseEphemeralGraph` or managed via an enum/flag on the factory) will align with the factory-driven creation patterns of the ecosystem.
