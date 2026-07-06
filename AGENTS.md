# System Instruction / Architectural Onboarding Brief
You are an expert Java systems engineer tasked with implementing a custom, high-performance directed graph library (Target JDK 17). Follow this architectural brief, the provided interfaces, and the test suite with absolute strictness. Do not hallucinate APIs or dependencies.

## 1. Project Overview, Philosophy, & The CHPG Vision
This library handles the static analysis of massive datasets (e.g., millions of nodes representing ASTs or CFGs). Standard Java graph implementations suffer from severe object-header bloat and pointer-chasing overhead, causing JVM heap exhaustion at scale.
To solve this, our library uses a **Central Registry Pattern** backed by primitive integer mapping and bitwise arithmetic, ensuring maximum performance and near-zero allocation overhead.

The library fills a critical void between heavy database drivers (like TinkerPop) and purely academic algorithmic libraries (like JGraphT). It is a highly specialized, precision engine—an uncompromising, memory-efficient staging and analysis engine. It successfully decouples the mathematical contract from the mechanical storage: downstream consumers interact with a purely set-theoretic interface (`pg-api`), completely oblivious to the fact that under the hood, engines are executing queries using highly defensive, zero-allocation primitive integer maps. The architecture incorporates strict pipeline defenses (e.g., violently rejecting missing anchors or foreign types and avoiding auto-vivification in the core pipeline) to guarantee that if a graph instantiates successfully, its topology is mathematically sound, preventing silent data corruption in complex polyglot environments.

**The Long-Term Vision (CHPG & Semantic Projections):** This "Flat Graph" library is the foundational storage engine intended to support advanced program analysis, specifically the discovery and exploitation of "natural projections" (naming conventions, architectural boundaries) as semantic coordinate systems. To support this, the core graph must remain strictly decoupled from any schemas, tag hierarchies, or containment rules. A future "Compound Hierarchical Property Graph" (CHPG) will act as a Semantic Wrapper around this flat graph. The flat graph handles raw state and speed; the future wrapper handles logic and meaning.

**Future-Proofing:** The fluent query API can be implemented with multiple graphs. If we later write a completely new backend (perhaps a `DatabaseGraph` that translates these API calls into SQL or Cypher queries in real-time), our existing library of algorithms will not require a single line of code to be changed.

## 2. The Multi-Module Ecosystem
To support both everyday development and massive-scale analysis, the project is divided into four strict modules:
 * **pg-api:** The pure interface layer (Graph, Node, Edge, ElementSet, and the sealed AttributeValue). Contains zero implementation logic and defines identity purely as int id() without prescribing how to assign ids.
 * **pg-heavy:** Depends on pg-api. A heavyweight reference implementation using adjacency lists and standard Java collections.
 * **pg-universe:** Depends on pg-api. The high-scale bitwise engine, Central Registry, Flyweights, and the transactional EphemeralGraph. Manages its own dual-polarity ID generation.
 * **pg-io:** Depends on pg-api. The interoperability hub providing universal importers/exporters. It translates pure graph state into formats like JSON or DOT, and acts as the strict boundary where external presentation logic (like visualization highlighting) is married to the graph data.

## 3. The Core Architecture: The Universe
The pg-universe module is centralized. We do not use standalone Graph objects that act as heavy containers.
 * **The Universe (Scoped Registry):** The absolute source of truth. Instantiated normally (new Universe()), allowing multiple isolated universes per JVM.
 * **The Dual-Polarity IdGenerator:** An internal engine owned by the Universe. Issues universally unique integer IDs (positive for persistent, negative for ephemeral). It is strictly hidden from pg-api.
 * **Global Inverted Indices (BitSets):** The Universe maintains global indices for tags and attributes using java.util.BitSet.
 * **Fail-Fast Concurrency (modCount):** The Universe maintains a universeModCount integer. Every structural or state mutation increments this counter. Traversals must snapshot this count and throw a ConcurrentModificationException if it changes.

## 4. Elements, Identity, & The Flyweight Pattern
 * **Interfaces over Base Classes:** Node and Edge are strictly interfaces extending GraphElement.
 * **Two Flavors (Memory Layouts):** Implementations are split to guarantee zero memory bloat during read operations.
   * **Heavyweights (EphemeralNode / EphemeralEdge):** Hold local HashMaps and HashSets for state tracking.
   * **Flyweights (UniverseNode / UniverseEdge):** Created dynamically during the read-phase. Ultra-lean wrappers containing solely final int id and final Universe universe. Zero local collections.
 * **Primitive Identity Enforcement:** The identity of any element is strictly a primitive int. **Do not wrap the ID in a record, class, or ElementId object.** This is mandatory to prevent object-header bloat and to allow zero-allocation IntStream processing.
 * **ID Polarity as State:** The sign bit of the primitive int acts as the state discriminator. Positive IDs are Persistent/Universe elements. Negative IDs are Transactional/Ephemeral elements.
 * **Strict Attribute Safety:** The AttributeValue contract is a sealed interface strictly permitting only String, primitive wrappers, and byte[].
 * **Polymorphic Smart Proxies:** Elements expose proxy collections (node.tags(), node.attributes()).
   * For Ephemeral elements, these return locally-backed collections.
   * For Universe elements, these return smart proxies that command the Universe to update global BitSet indices.

## 5. Operational Modes & The "Hybrid" Sandbox
Graph operations inherently conflict: writes are fast in localized memory, queries are fast in global indices. We split the lifecycle based on **ID Polarity**.
| Feature | EphemeralGraph (Write-Optimized) | UniverseGraph (Read-Optimized) |
|---|---|---|
| **Use Case** | Parsing, local scratchpad, hybrid staging. | Static analysis, set math, traversal. |
| **ID Polarity** | Assigns **Negative IDs** (starts at -1). | Assigns **Positive IDs** (starts at 1). |
| **Storage** | Hybrid: Local maps + References to Universe. | Global BitSet indices inside the Universe. |
| **Indexing** | Bypasses BitSet indices for local elements. | Fully indexed. |
| **Structural Mutation** | Allowed (on Native Ephemeral elements). | **Forbidden.** Modifying collections throws UnsupportedOperationException. |
| **State Mutation** | Allowed on Native (local) & Foreign (global). | **Allowed.** Updates via .tags() immediately update global BitSet indices. |
**The Hybrid EphemeralGraph:** The write-phase graph is a viewport. It can hold native EphemeralNodes (negative IDs) and act as a sandbox. It can also securely hold references to foreign UniverseNodes (positive IDs) to allow linking new local data to existing persistent data.

## 6. Transactional Promotion
When universe.promote(ephemeralGraph) is called, the Universe:
 1. Translates native Ephemeral nodes to new positive IDs.
 2. Deep-clones the local ephemeral state into the global BitSet indices.
 3. Preserves foreign UniverseNode references as-is.
 4. Rewires all edge pointers using the established positive IDs.
 5. Strictly invalidates the old EphemeralGraph.
 6. Returns a new, fully indexed UniverseGraph.

## 7. Query Engine & Traversals (The DSL)
Querying relies on a fluent, domain-specific language bridging bitwise math and Java Collections.
 * **Flyweight Collections:** NodeSet and EdgeSet implement java.util.Set<T>. Universe-backed sets only hold a reference to a BitSet and calculate size()/contains() on the fly.
 * **Zero-Allocation Primitives:** Exposes IntStream idStream() directly wrapping BitSet.stream() to allow map-reduce logic without instantiating element objects.
 * **Custom .forEach():** Overrides Iterable.forEach() to bypass Iterator allocation entirely.
 * **Stream Integration:** Supports .stream() and .parallelStream(). Enforces modCount validation.

## 8. Serialization & The pgv Visualization Pipeline
 * **Zero-Copy Snapshots (pg-universe):** The internal binary serializer lives inside pg-universe. It uses java.nio.channels.FileChannel to dump raw long[] arrays directly from the Universe BitSets to disk. Positive IDs are preserved; ephemeral IDs are dropped.
 * **Interactive Visualization (pgv & pg-io):** The library supports a separate TypeScript visualizer (pgv) intended for embedding in Jupyter Notebooks, VSCode, and web apps.
   * The pure graph elements (pg-api) have strictly **zero knowledge** of UI, colors, or presentation logic.
   * The domain-specific analysis code running the queries defines a "Highlight Scheme" (e.g., CSS-like rules mapping tags/attributes to colors and shapes).
   * The pg-io module accepts both the graph projection and the styling configuration parameter, exporting a combined JSON payload that the external pgv frontend uses to render the interactive map.

## 9. Implementation Directives
Strictly implement the exact method signatures, generics, and return types provided in the Interfaces section below. Ensure the provided Test Suite passes without modification.

# CORE ARCHITECTURE & INTERFACE STABILITY
The foundation of this project relies on key interfaces located in the following files:
- pg-core\src\main\java\io\github\benjholla\pg\TagSet.java
- pg-core\src\main\java\io\github\benjholla\pg\AttributeMap.java
- pg-core\src\main\java\io\github\benjholla\pg\AttributeValue.java
- pg-core\src\main\java\io\github\benjholla\pg\Node.java
- pg-core\src\main\java\io\github\benjholla\pg\NodeSet.java
- pg-core\src\main\java\io\github\benjholla\pg\Edge.java
- pg-core\src\main\java\io\github\benjholla\pg\EdgeSet.java
- pg-core\src\main\java\io\github\benjholla\pg\Graph.java

**CRITICAL RULE:** The public interfaces in these classes are considered STRICTLY STABLE. You must preserve them. Do not modify, refactor, or deprecate these public interfaces unless there is an exceptional architectural necessity. If you determine a change is absolutely required, you MUST explicitly raise it for discussion and provide a strong justification before proceeding with any modifications or justify why the test(s) are wrong.

# License & Dependency Policy

Always preserve the project's ability to be distributed under the MIT License. Before introducing any dependency, verify that its license is compatible with MIT and does not impose obligations that would conflict with the project's licensing goals.

Prefer mature, actively maintained, trustworthy libraries with a strong reputation for security, performance, and long-term stability. Avoid introducing dependencies that appear abandoned, poorly maintained, or unnecessarily risky.

Do not reinvent well-solved problems. If a high-quality library already performs a task better than we reasonably could, prefer integrating it rather than building a custom implementation. New dependencies should provide clear long-term value, reduce maintenance burden, and align with the project's standards for quality, reliability, and speed.

# Repository Hygiene
Clean up after yourself! Before opening a PR/MR, ensure no temporary or incidental development artifacts remain. Remove patch files, diff files, scratch notes, generated temporary outputs, debugging helpers, backup files, and other one-off artifacts unless they provide clear long-term value to the project. Leave the repository in a state that is clean, intentional, and easy for future contributors to navigate. Every committed file should justify its continued existence.

---

### ⚙️ SYSTEM ARCHITECTURE & BOUNDARY CONSTRAINTS FOR pg GRAPH ECOSYSTEM
**1. Architecture & Cross-Contamination Prevention**
The engine uses primitive ID-based routing, not standard object references. The generic Node and Edge interfaces from pg-api are shared across both the pg-heavy (lightweight, globally unique IDs) and pg-universe (bitwise, locally scoped IDs) implementations.
 * **Strict Type Borders:** You must use explicit instanceof checks (e.g., node instanceof HeavyNode) to actively reject foreign implementations before extracting primitive IDs for internal maps or arrays.
 * **Avoid Generics:** Do not introduce generics to pg-api to solve type safety. Keep the API monomorphic and rely on runtime type checks. Modern JVM polymorphic inline caches (PICs) will optimize these checks away in hot loops.
**2. Standardizing Collections (Composition over Inheritance)**
When implementing custom interfaces like NodeSet or EdgeSet, you must use **composition** (wrapping a strictly-typed java.util.HashSet or java.util.BitSet) rather than extending standard collections.
 * **Mutations (Fail-Fast):** Additive methods (add, addAll) must actively enforce the type boundary using instanceof and throw an IllegalArgumentException immediately if a foreign type is detected.
 * **Subtractions (Silent Ignore):** Query and removal methods (contains, remove, removeAll) must passively catch or handle foreign types and safely return false without crashing, aligning with standard Java collection semantics.
**3. Equality and Hashing Mechanics**
 * **Elements:** Identity for concrete nodes and edges is defined strictly by Type and Primitive ID. Implement equals() using a fast-path reference check, a strict instanceof type check, and finally a primitive this.id() == that.id() check. Hash codes must rely purely on the primitive ID (e.g., Integer.hashCode(id)).
 * **Sets:** Custom sets must delegate equality and hashing directly to their internal collections to respect the mathematical contract of java.util.Set (where empty sets of different types evaluate to true).
 * **Wrapper Optimization:** All custom sets must intercept self-comparisons with a fast-path if (this == o) return true; before delegating. Failure to do so will cause the internal JDK logic to fail its own fast-path and degrade to an O(N) element-by-element iteration.
**4. Zero-Allocation Traversals**
Graph traversals generate immense Garbage Collection (GC) pressure via nested loops.
 * **Direct Iterator Delegation:** When implementing iterator(), do not instantiate anonymous wrapper classes. Because the boundary checks on .add() mathematically guarantee internal type safety, you must use a double-cast (e.g., @SuppressWarnings("unchecked") return (Iterator<Node>) (Iterator<?>) internalSet.iterator();) to hand the developer the JDK's internal iterator directly, ensuring zero object allocation in hot loops.

### ⚙️ SYSTEM ARCHITECTURE & BOUNDARY CONSTRAINTS FOR GRAPH IMPLEMENTATIONS (UPDATED)
This brief governs the internal data structures, ID-based routing, and boundary validation philosophy for concrete Graph implementations (e.g., HeavyGraph, EphemeralGraph) within the pg ecosystem.
#### 1. Internal Structure & Primitive Routing (The ID Mandate)
Concrete graph engines discard object wrappers and route entirely via primitive integer IDs to achieve O(1) performance and prepare for serialization (e.g., pg-io).
 * **The 4-Pillar Adjacency Structure (e.g., HeavyGraph):** Internally, the graph must map primitive IDs to their corresponding elements. Do not use generic Node or Edge objects as keys.
   * Map<Integer, HeavyNode> nodes (Global Node Registry)
   * Map<Integer, HeavyEdge> edges (Global Edge Registry)
   * Map<Integer, HeavyEdgeSet> outEdges (Outbound Adjacency)
   * Map<Integer, HeavyEdgeSet> inEdges (Inbound Adjacency)
 * **Primitive Extraction:** The engine must actively validate an element's type *before* extracting its .id() to prevent silent routing failures or ID collisions.
#### 2. The API Philosophy: Command-Query Separation
Graph boundaries follow a strict rule to ensure standard Java collection compatibility while protecting internal adjacency lists: **Reads are safe; Writes and Anchors are strict.**
 * **Subtractions & Queries (Silent Ignore):** Operations that ask *if* something exists or ask to *remove* something (e.g., removeNode, removeEdge, contains, intersect, difference). If a foreign or incompatible node is provided, the graph must safely return false or an empty collection. Do not throw exceptions.
 * **Additive Mutations (Violent Fail):** Operations that inject state into the graph (e.g., addEdge). If a foreign element is provided, the graph must immediately throw an IllegalArgumentException. Do not attempt to cast or recover.
 * **Traversal Anchors (Violent Fail):** Operations that anchor a topological search (e.g., forwardStep, outEdges, neighbors). The anchor element is a precondition, not a query parameter. Supplying a foreign anchor must immediately throw an IllegalArgumentException to prevent logic masking in downstream algorithms.
#### 3. Differentiated Engine Guardrails
Validation strictness scales based on the ID generation strategy of the specific engine module.
 * **pg-heavy (Type Validation Only):** Because HeavyGraph utilizes a globally unique static singleton for ID generation, cross-instance collisions are impossible. Methods only require a strict type check (e.g., instanceof HeavyNode) to reject cross-engine contamination.
 * **pg-universe (Type + Instance Validation):** Because EphemeralGraph generates locally scoped negative IDs (starting at -1), different instances will issue identical IDs. Methods must strictly validate type (instanceof EphemeralNode) **and** instance ownership (node.graph() == this) to prevent cross-universe adjacency corruption.
#### 4. Encapsulation & Shielded Views
The Graph is the absolute source of truth for topology and must heavily encapsulate its integer-backed maps.
 * **No Raw Leaks:** When satisfying API contracts like graph.nodes() or returning the result of a traversal, the graph must never return its internal java.util.Collection or java.util.Map.values().
 * **Safe Wrapping:** Internal values must be dynamically wrapped and returned as the module's strictly bounded custom sets (e.g., HeavyNodeSet, HeavyEdgeSet). This preserves the pg-api interface while preventing API consumers from using standard Java collection methods to bypass validation and corrupt the
graph's internal state.
