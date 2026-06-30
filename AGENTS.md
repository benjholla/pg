**System Instruction / Architectural Onboarding Brief**
You are an expert Java systems engineer tasked with implementing a custom, high-performance directed graph library (Target JDK 17). Follow this architectural brief, the provided interfaces, and the test suite with absolute strictness. Do not hallucinate APIs or dependencies.

## 1. Project Overview, Philosophy, & The CHPG Vision
This library is built to handle the static analysis of massive datasets (e.g., millions of nodes representing ASTs or CFGs). Standard Java graph implementations suffer from severe object-header bloat and pointer-chasing overhead, causing JVM heap exhaustion at scale.
To solve this, our library uses a **Central Registry Pattern** backed by primitive integer mapping and bitwise arithmetic, ensuring maximum performance and near-zero allocation overhead.
**The Long-Term Vision (CHPG):** This "Flat Graph" library is the foundational storage engine for a future "Compound Hierarchical Property Graph" (CHPG). To support this, the core Universe must remain completely agnostic to schemas, tag hierarchies, or containment rules. The CHPG will eventually act as a "Semantic Wrapper" around this flat graph (e.g., executing a bitwise OR across flat tags to compute a hierarchy on the fly). Therefore, **strict decoupling is mandatory**. The flat graph handles raw state and speed; the future wrapper will handle logic and meaning.

## 2. The Core Architecture: The Universe
The architecture is centralized. We do not use standalone Graph objects that act as heavy containers.
 * **The Universe (Scoped Registry):** The absolute source of truth. It is instantiated normally (new Universe()), allowing multiple isolated universes per JVM.
 * **The IdGenerator:** Owned by the Universe. Issues universally unique integer IDs to every graph element.
 * **Global Inverted Indices (BitSets):** The Universe maintains global indices for tags and attributes using java.util.BitSet.
 * **Graph Views:** A Graph implementation holds a BitSet of IDs belonging to it. Set operations are instantaneous bitwise math.
 * **Fail-Fast Concurrency (modCount):** The Universe maintains a universeModCount integer. Every structural or state mutation increments this counter. All iterators, streams, and .forEach() loops must snapshot this count upon creation and throw a ConcurrentModificationException if it changes during traversal.

## 3. Elements, Identity, & The Flyweight Pattern
 * **Interfaces over Base Classes:** Node and Edge are strictly interfaces extending GraphElement.
 * **Two Flavors (Memory Layouts):** Implementations are split to guarantee zero memory bloat during read operations.
   * **Heavyweights (EphemeralNode / EphemeralEdge):** Hold local HashMaps and HashSets for state tracking.
   * **Flyweights (UniverseNode / UniverseEdge):** Created dynamically during the read-phase. These are ultra-lean wrappers containing solely final int id and final Universe universe. They have zero local collection fields.
 * **ID-Driven Identity:** The integer id assigned at creation is final. The equals() and hashCode() methods on all implementations are strictly backed by this ID to guarantee cross-flavor hash map performance.
 * **Polymorphic Smart Proxies:** Elements expose proxy collections (node.tags(), node.attributes()).
   * For Ephemeral elements, these return locally-backed collections.
   * For Universe elements, these return smart proxies that command the Universe to update global BitSet indices and increment modCount.
   * AttributeValue strictly encapsulates String, primitive wrappers, or byte[].

## 4. Operational Modes & The "Hybrid" Write-Phase
Graph operations inherently conflict: writes are fast in localized memory, queries are fast in global indices. We split the lifecycle based on **ID Polarity**.
| Feature | EphemeralGraph (Write-Optimized) | UniverseGraph (Read-Optimized) |
|---|---|---|
| **Use Case** | Parsing, local scratchpad, hybrid staging. | Static analysis, set math, traversal. |
| **ID Polarity** | Assigns **Negative IDs** (starts at -1). | Assigns **Positive IDs** (starts at 1). |
| **Storage** | Hybrid: Local maps + References to Universe. | Global BitSet indices inside the Universe. |
| **Indexing** | Bypasses BitSet indices for local elements. | Fully indexed. |
| **Structural Mutation** | Allowed (on Native Ephemeral elements). | **Forbidden.** Modifying collections throws UnsupportedOperationException. |
| **State Mutation** | Allowed on Native (local) & Foreign (global). | **Allowed.** Updates via .tags() immediately update global BitSet indices. |
**The Hybrid EphemeralGraph:** The write-phase graph is a viewport. It can hold native EphemeralNodes (negative IDs) and act as a sandbox. It can also hold references to foreign UniverseNodes (positive IDs) to allow linking new local data to existing persistent data.

## 5. Transactional Promotion
When universe.promote(ephemeralGraph) is called, the Universe:
 1. Translates native Ephemeral nodes to new positive IDs.
 2. Deep-clones the local ephemeral state into the global BitSet indices.
 3. Preserves foreign UniverseNode references as-is.
 4. Rewires all edge pointers (native-to-native, native-to-foreign) using the established positive IDs.
 5. Strictly invalidates the old EphemeralGraph (throws IllegalStateException on further use).
 6. Returns a new, fully indexed UniverseGraph.

## 6. Creation & Instantiation (The Factory Pattern)
Standard element constructors (new Node(), new Edge()) are strictly forbidden.
 * **Graph Creation:** EphemeralGraph is created via universe.createEphemeralGraph(). UniverseGraph is created *exclusively* via universe.promote().
 * **Element Creation:** EphemeralGraph acts as the sole factory for elements (e.g., graph.createNode()). Attempting this on a UniverseGraph throws an UnsupportedOperationException.

## 7. Query Engine & Traversals (The DSL)
Querying relies on a fluent, domain-specific language bridging bitwise math and Java Collections.
 * **Flyweight Collections:** NodeSet and EdgeSet implement java.util.Set<T>. Like elements, these use the Flyweight pattern. Universe-backed sets only hold a reference to a BitSet and calculate size/contains on the fly.
 * **Zero-Allocation Primitives:** Exposes IntStream idStream() directly wrapping BitSet.stream().
 * **Custom .forEach():** Overrides Iterable.forEach() to bypass Iterator allocation entirely.
 * **Stream Integration:** Supports .stream() and .parallelStream(). Both enforce modCount fail-fast validation. Includes a custom Collector to reduce back into an ElementSet.

## 8. Storage & Serialization
 * **Custom NIO:** Uses java.nio.channels.FileChannel and ByteBuffer to dump raw long[] arrays directly from the Universe to disk.
 * **ID Preservation:** Loading perfectly preserves positive IDs. Ephemeral elements (negative IDs) are ignored during serialization.

## 9. Implementation Directives
Strictly implement the exact method signatures, generics, and return types provided in the Interfaces section below. Ensure the provided Test Suite passes without modification.

# CORE ARCHITECTURE & INTERFACE STABILITY
The foundation of this project relies on key interfaces located in the following files:
- pg-core\src\main\java\io\github\benjholla\pg\TagSet.java
- pg-core\src\main\java\io\github\benjholla\pg\AttributeMap.java
- pg-core\src\main\java\io\github\benjholla\pg\ElementId.java
- pg-core\src\main\java\io\github\benjholla\pg\Node.java
- pg-core\src\main\java\io\github\benjholla\pg\NodeSet.java
- pg-core\src\main\java\io\github\benjholla\pg\Edge.java
- pg-core\src\main\java\io\github\benjholla\pg\EdgeSet.java
- pg-core\src\main\java\io\github\benjholla\pg\Graph.java

**CRITICAL RULE:** The public interfaces in these classes are considered STRICTLY STABLE. You must preserve them. Do not modify, refactor, or deprecate these public interfaces unless there is an exceptional architectural necessity. If you determine a change is absolutely required, you MUST explicitly raise it for discussion and provide a strong justification before proceeding with any modifications.

# License & Dependency Policy

Always preserve the project's ability to be distributed under the MIT License. Before introducing any dependency, verify that its license is compatible with MIT and does not impose obligations that would conflict with the project's licensing goals.

Prefer mature, actively maintained, trustworthy libraries with a strong reputation for security, performance, and long-term stability. Avoid introducing dependencies that appear abandoned, poorly maintained, or unnecessarily risky.

Do not reinvent well-solved problems. If a high-quality library already performs a task better than we reasonably could, prefer integrating it rather than building a custom implementation. New dependencies should provide clear long-term value, reduce maintenance burden, and align with the project's standards for quality, reliability, and speed.

# Repository Hygiene
Clean up after yourself! Before opening a PR/MR, ensure no temporary or incidental development artifacts remain. Remove patch files, diff files, scratch notes, generated temporary outputs, debugging helpers, backup files, and other one-off artifacts unless they provide clear long-term value to the project. Leave the repository in a state that is clean, intentional, and easy for future contributors to navigate. Every committed file should justify its continued existence.