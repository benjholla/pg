package dev.chpg.pg.api;

/**
 * Represents a directed connection between two {@link Node}s in a property graph.
 * <p>
 * <b>What it represents:</b> A directed relationship or link originating from a source node ({@link #from()}) and terminating at a target node ({@link #to()}).
 * <p>
 * <b>Why it exists:</b> To encode the structural topology and relational data of the graph.
 * <p>
 * <b>When to use it:</b> Use {@code Edge}s to model relationships, data flow, or control flow between distinct entities (nodes).
 * <p>
 * <b>Common usage patterns:</b>
 * <ul>
 * <li>Created via a {@link GraphFactory} by supplying the source and target nodes.</li>
 * <li>Decorated with labels via its {@link TagSet} (e.g., "knows", "calls").</li>
 * <li>Used to drive topological queries (e.g., {@link Graph#forward(Node)}).</li>
 * </ul>
 * <p>
 * <b>Important invariants:</b>
 * <ul>
 * <li>Edges strictly dictate a direction.</li>
 * <li>Edges cannot exist independently of their endpoints. Attempting to add an edge to a graph inherently implies the presence of its {@code from} and {@code to} nodes. Removing either connected node from a graph cascades and removes the incident edge.</li>
 * </ul>
 * <p>
 * <b>Thread safety:</b> As with {@link Node}, thread safety guarantees are delegated to the specific backend implementation. Assume edges are not safe for concurrent mutation unless specified otherwise.
 * <p>
 * <b>Performance characteristics:</b> Edges are optimized for structural traversal. Routing relies entirely on their primitive {@code int} identities to avoid pointer chasing overhead.
 */
public interface Edge extends GraphElement {

    /**
     * Returns the source node from which this directed edge originates.
     *
     * @return the source node
     */
    Node from();

    /**
     * Returns the target node at which this directed edge terminates.
     *
     * @return the target node
     */
    Node to();

    /*
     * ARCHITECTURE NOTE: Bidirectional Edge Support
     * Currently utilizing "query-time" bidirectionality (storing strictly directed
     * edges but supporting undirected traversal via NodeDirection.BOTH).
     * * If structural undirected edges are added to the storage layer in the future,
     * the Edge interface must be expanded to handle ambiguous source/target semantics:
     * - boolean isDirected();
     * - Node opposite(Node anchor); // Required for agnostic traversal hopping
     */
}
