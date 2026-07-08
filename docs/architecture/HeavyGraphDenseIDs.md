# HeavyGraph Dense IDs

## Overview

The `HeavyGraph` is the "primary data store" of the pg ecosystem. It is distinguished from the `EphemeralGraph` primarily by its **memory layout**. The defining characteristic of `HeavyGraph` is that its element IDs (nodes and edges) are **dense and 0-indexed**.

This dense, 0-indexed structure provides a critical performance advantage: it allows the graph to back properties, tags, and adjacency lists with raw arrays rather than hash maps.

### HeavyGraph vs EphemeralGraph Memory Layout

Both engines strictly implement the same `Graph` interface, meaning both fully support properties and tags. The distinction lies in their memory layout:

*   **HeavyGraph:** Because its IDs are dense and start at 0, it can back its properties and topology with raw arrays (e.g., `AttributeMap[] properties = new AttributeMap[size]`). This provides O(1) lookup speed with perfect cache locality, making it the ultimate engine for heavy, permanent data querying.
*   **EphemeralGraph:** Because its IDs are negative, it must rely on HashMaps (`IntObjectMap`) for property lookups. It incurs a slight hashing overhead, which is the mathematically necessary trade-off for acting as a temporary, isolated scratchpad.

## The Problem: Dynamic Sizing vs Array Performance

A graph engine that requires pre-allocation of the exact number of nodes and edges at instantiation is severely limited, particularly during data parsing or iterative graph generation.

However, standard Java collections like `java.util.ArrayList` cannot be used to achieve dynamic sizing because they introduce an unacceptable performance penalty that destroys the hyper-optimized memory layout required by `HeavyGraph`.

### The ArrayList Object Boxing Penalty

The core advantage of dense, 0-indexed IDs is the ability to use raw primitives (like `int`) for topological routing.

Standard Java collections cannot hold primitives. If an `ArrayList<Integer>` is used for adjacency lists:

1.  **Boxing Overhead:** The JVM is forced to box every single `int` ID into an `Integer` object. A raw `int` takes 4 bytes. An `Integer` object takes 16 to 24 bytes (object header + payload).
2.  **Cache Locality Destruction:** More importantly, an array of `Integer` objects is actually an array of pointers pointing to randomly scattered memory locations across the heap. This completely shatters CPU L1 cache locality, causing massive cache misses during graph traversals.

## The Solution: Dynamic Primitive Arrays

To achieve dynamic sizing without sacrificing raw array performance, `HeavyGraph` utilizes **Dynamic Primitive Arrays**.

Under the hood, `java.util.ArrayList` uses a raw `Object[]` array. When full, it allocates a new, larger array and uses hardware-accelerated `System.arraycopy()` to move the data.

`HeavyGraph` applies this exact same strategy, but does so directly using raw `int[]` and element arrays (like `Node[]` or `AttributeMap[]`) to completely avoid the object wrapper overhead.

### Internal Mechanics

The internal management of dynamic primitive arrays inside `HeavyGraph` follows this pattern:

```java
public final class HeavyGraph implements Graph {
    // Start with a reasonable default capacity
    private int[] nodeIds = new int[1024];
    private int[][] outgoingEdges = new int[1024][];

    private int nextNodeId = 0;

    public HeavyNode createNode() {
        int id = nextNodeId++;

        // The Dynamic Resize Trigger
        if (id == nodeIds.length) {
            growArrays();
        }

        nodeIds[id] = id;
        return new HeavyNode(id);
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

By manually managing primitive arrays (or utilizing primitive collection libraries that perform this logic under the hood), `HeavyGraph` achieves:

1.  **Infinite Mutability:** Analysts can add nodes and edges dynamically forever without pre-calculating sizes.
2.  **Zero Boxing:** Adjacency checks and ID storage remain pure primitive math.
3.  **Perfect Density:** The arrays remain contiguous, 0-indexed, and perfectly packed for the CPU cache, preserving maximum traversal speed.
