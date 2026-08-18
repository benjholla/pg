# Architectural Brief: Shadow Elements

## Overview

In the `pg-multiverse` module, an `EphemeralGraph` acts as a high-speed, isolated transaction scratchpad on top of a baseline `Universe`. To provide mathematical isolation without sacrificing performance, the engine uses **Shadow Elements** (`ShadowNode` and `ShadowEdge`).

Shadow elements are read-optimized proxies that wrap baseline universe elements. They provide a transactional view of the universe state seamlessly combined with any uncommitted local mutations.

## 1. Zero-Copy Isolation

When a baseline universe node or edge is added to an `EphemeralGraph`, it is not cloned. Instead, the engine creates a `ShadowNode` or `ShadowEdge`.

*   **Memory Efficiency:** A shadow element is extremely lightweight. It strictly holds two references: the `EphemeralGraph` transaction context and the backing `UniverseNode` (or `UniverseEdge`).
*   **ID Parity:** Shadow elements inherit the ID of their backing baseline element. A `ShadowNode` returns the exact positive array index of its backing `UniverseNode`, allowing it to perfectly interoperate with high-speed array lookups in the baseline graph.

## 2. Transactional Interception

The primary responsibility of a shadow element is to intercept mutations (tags and attributes) and route them to the transaction logs in the `EphemeralGraph`.

### Tags (`ShadowTagSet`)
When a tag is added to a `ShadowNode`, the `ShadowTagSet` records the mutation in a local transaction buffer (`pendingNodeTags`). When checking for existence, it first checks the local buffer and then falls back to the backing universe node's tags. Removal drops a tombstone to mask the baseline tag.

### Attributes (`ShadowAttributeMap`)
Similarly, `ShadowAttributeMap` intercepts key-value properties.
1.  **Reads (`get`):** The map first checks the tombstone log to see if the property was explicitly deleted in the current transaction. Next, it checks the pending overwrite log. Finally, if no local mutation exists, it delegates the read to the backing baseline `UniverseNode`.
2.  **Writes (`put`):** The map clears any existing tombstone for the key and buffers the new value in the pending overwrite log.
3.  **Deletes (`remove`):** The map removes the value from the pending log and, if it exists in the backing universe, drops a tombstone to mask it from future reads in this transaction.

To ensure transactional safety, `ShadowAttributeMap` explicitly overrides bulk map operations (like `compute`, `merge`, and `replaceAll`) to delegate to the `AttributeMap` default implementations, strictly bypassing unsafe abstract map backends that could evade the ephemeral transaction log.

## 3. Shadow Sets and Iterators

### `ShadowNodeSet` and `ShadowEdgeSet`
These are read-only composite sets that project a hybrid view of baseline elements mixed with uncommitted transaction state.
*   **Algebraic Semantics:** They enforce a strict whitelist for set algebra operations, ensuring fast, O(1) existence checks where possible.
*   **Immutability:** Bulk mutation methods (`add`, `addAll`, `removeAll`) on shadow sets intentionally throw `UnsupportedOperationException`, directing users to route topology modifications through the `EphemeralGraph` API directly.

### `ShadowNodeIterator` and `ShadowEdgeIterator`
These iterators wrap standard universe element iterators. As they traverse the underlying baseline data structures, they lazily construct and yield `ShadowNode` or `ShadowEdge` proxies, ensuring that downstream computations remain safely within the transaction context without paying the upfront cost of wrapping the entire set.
