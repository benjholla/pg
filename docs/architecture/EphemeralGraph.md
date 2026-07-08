# Architectural Brief: EphemeralGraph Memory & ID Management
## 1. The Core ID Architecture
The EphemeralGraph is designed as an infinitely scalable, high-speed mutation scratchpad. It relies on mathematical isolation rather than global object tracking to maintain memory safety.
 * **Stateless Flyweights:** EphemeralNode and EphemeralEdge are pure, naked coordinates. They carry exactly one 32-bit int id and **do not** hold a reference back to their parent .graph(). This prevents cyclic garbage collection leaks and keeps elements at an absolute minimum memory footprint (roughly 8 bytes).
 * **The Dedicated Generator:** Every root sandbox is instantiated with its own independent EphemeralIdGenerator.
 * **Isolated, Negative Sequences:** Inside the generator, nodes and edges maintain completely separate integer counters (nextNodeId and nextEdgeId). Both counters start at -1 and count down. This guarantees mathematically dense, gap-less bitsets for maximum CPU L1 cache efficiency.
 * **Domain Validation:** The graph strictly rejects any positive ID. This creates a hard mathematical firewall preventing permanent UniverseGraph elements from polluting the temporary sandbox.
## 2. Subgraphs and Lineage Protection
Because nodes lack a parent reference, the engine protects the topology by validating the origin of the elements at the macro-graph level during set algebra.
 * **Zero-Cloning Subgraphs:** When an operation like forward() or induce() spawns a subgraph, the elements are not cloned. The origin graph simply passes its EphemeralIdGenerator reference into the subgraph's private constructor, perfectly preserving the ID lineage.
 * **The Lineage Validator:** A centralized internal method (validateLineage(Graph... graphs)) intercepts all multi-graph algebra (e.g., union(), intersection()).
 * **Hard Isolation:** The validator blocks operations between disparate engines (e.g., throwing an exception if an analyst tries to union an EphemeralGraph directly with a GlobalGraph), and blocks operations between independent ephemeral sandboxes by verifying this.idGenerator == other.idGenerator.
## 3. Final Justification: The Rejection of Bit-Packing
Initially, bit-packing (embedding a unique "Sandbox ID" into the upper bits of the 32-bit integer) was considered to physically prevent single-element cross-sandbox contamination. This approach was officially rejected in favor of pure, unbounded negative counters for the following critical reasons:
### A. The Scalability Bottleneck (The Fatal Flaw)
Bit-packing within a 32-bit integer is a zero-sum game. Dedicating 8 bits to a Sandbox ID hard-caps the JVM at 256 concurrent sandboxes, and limits each sandbox to 8.38 million elements. Because these graphs act as high-speed disposable lenses for intermediate PCG computations and semantic projections, the engine must support spawning thousands of throwaway sandboxes per minute. Bit-packing instantly chokes this primary use case.
### B. The Array Indexing Penalty
To utilize ultra-fast array indexing or standard BitSet logic, bit-packed IDs require mandatory masking (stripping the Sandbox ID via bitwise AND) before every single storage access. Failing to strip the bits attempts multi-gigabyte array allocations. Pure sequences (-1, -2) can simply be inverted Math.abs(id) for instant, direct array routing with zero masking overhead.
### C. The 64-Bit Trap
The only way to achieve both infinite sandboxes and bit-packed safety is to upgrade the foundational IDs from a 32-bit int to a 64-bit long. This doubles the memory footprint of every array, halves L1 cache hit rates, and destroys compatibility with highly optimized standard Java BitSets.
### D. The Acceptable Trade-Off
By rejecting bit-packing, we accept a localized failure mode: manual, single-element cross-sandbox polling (e.g., manually checking if Sandbox A's node exists in Sandbox B) will yield a silent false positive. This is treated as a user-space logical error. The architectural tradeoff heavily favors pure O(1) performance and infinite sandbox scalability over hand-holding disjoint element queries.