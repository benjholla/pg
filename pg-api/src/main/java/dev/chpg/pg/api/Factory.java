package dev.chpg.pg.api;

/**
 * A comprehensive factory interface that combines node, edge, and graph creation capabilities.
 * <p>
 * <b>What it represents:</b> A unified builder interface responsible for instantiating all native components (Nodes, Edges, Graphs) of a specific backend implementation.
 * <p>
 * <b>Why it exists:</b> To provide a single, cohesive entry point for element instantiation, ensuring that all created components are natively compatible with each other and share the same underlying architecture.
 * <p>
 * <b>When to use it:</b> Use {@code Factory} as the primary API for bootstrapping new graph structures, nodes, or edges when you need a unified builder.
 * <p>
 * <b>Common usage patterns:</b>
 * <ul>
 * <li>Creating nodes and edges to populate a graph (e.g., {@code factory.createNode()}).</li>
 * <li>Creating subgraphs or empty graphs (e.g., {@code factory.createGraph()}).</li>
 * </ul>
 * <p>
 * <b>Important invariants:</b> A {@code Factory} must strictly yield instances that are compatible with the specific implementation that provided the factory. For example, an EphemeralFactory must yield EphemeralNodes.
 * <p>
 * <b>Thread safety:</b> Implementations of this factory should ideally be thread-safe to allow concurrent element creation, though strict guarantees depend on the specific backing implementation.
 * <p>
 * <b>Performance characteristics:</b> Creation methods are expected to be highly optimized and allocation-efficient, serving as the core engine's primary instantiation pathway.
 */
public interface Factory extends NodeFactory, EdgeFactory, GraphFactory {

}
