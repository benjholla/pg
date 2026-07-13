# Performance Goals

Here is the performance matrix for the pg-api implementations provided in pg-global and pg-multiverse.

**Variables Key:**
 * **V** = Total number of nodes
 * **E** = Total number of edges
 * **D** = Degree of a specific node
 * **K** = Size of a filtered/resulting subset
 * **S** = Size of an incoming collection (for bulk operations)
### 1. Core Topology & Queries (Graph API)
These operations leverage the zero-allocation, 4-pillar primitive integer routing architecture.
| API Method | GlobalGraph Runtime | GlobalGraph Memory | Ephemeral / Universe (Est.) |
|---|---|---|---|
| nodes(), edges() | O(1) | O(1) | O(1) |
| node(id), edge(id) | O(1) | O(1) | O(1) |
| containsNode(n), containsEdge(e) | O(1) | O(1) | O(1) |
| degree(n, direction) | O(1) | O(1) | O(1) |
| edges(n, direction) | O(1) | O(1) | O(1) |
*Note: edges(n, direction) is O(1) memory only if it returns an unmodifiable view of the internal inEdges/outEdges map. If it allocates a new EdgeSet snapshot, memory becomes O(D).*
### 2. Structural Mutations (Graph API)
These bounds are dictated by the HashMap registry and the cascading topological invariants.
| API Method | GlobalGraph Runtime | GlobalGraph Memory | Ephemeral / Universe (Est.) |
|---|---|---|---|
| addNode(n) | O(1) amortized | O(1) per node | *Depends on mutability* |
| linkEdge(e) | O(1) amortized | O(1) per edge | *Depends on mutability* |
| removeNode(n) | O(D) | O(1) | O(D) |
| removeAllNodes(S) | O(S * D) | O(1) | O(S * D) |
| clear() | O(V + E) | O(1) | O(V + E) |
*Note: removeNode is O(D) because maintaining mathematical graph purity requires a cascading removal of all incident edges to prevent ghost topology.*
### 3. Set-Theoretic Algebra & Traversals (Graph API)
Because your API explicitly mandates that these operations yield *new, induced subgraphs* to preserve the immutable query algebra, they cannot be zero-allocation.
| API Method | GlobalGraph Runtime | GlobalGraph Memory | Ephemeral / Universe (Est.) |
|---|---|---|---|
| forward(n), between(n, n) | O(V_sub + E_sub) | O(V_sub + E_sub) | O(V_sub + E_sub) |
| union(Graph) | O(V + E + S) | O(V_sub + E_sub) | O(V + E + S) |
| intersection(Graph) | O(min(V, S)) | O(V_sub + E_sub) | O(min(V, S)) |
| difference(Graph) | O(S) | O(V_sub + E_sub) | O(S) |
*Note: intersection runtime is optimized by iterating over the smaller of the two graphs and checking O(1) existence in the larger.*
### 4. Query & Functional Math (NodeSet / EdgeSet API)
These methods govern the disconnected snapshot boundaries.
| API Method | Runtime | Memory | Implementation Driver |
|---|---|---|---|
| one() | O(1) | O(1) | Extracts the first valid iterator element. |
| withAttribute(attribute) | O(V) | O(K) | Must scan the live view and allocate a snapshot for matches. |
| intersect(S), union(S) | O(V + S) | O(K) | Allocates a new disconnected snapshot for safe traversal. |
| ids() | O(V) | O(V) | Iterates the set to box/extract the integer routing keys. |
