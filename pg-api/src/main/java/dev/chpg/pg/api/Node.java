package dev.chpg.pg.api;

/**
 * Represents a vertex within a property graph.
 * <p>
 * <b>What it represents:</b> A foundational entity in a property graph, representing a single point, concept, or data item that can be connected by edges.
 * <p>
 * <b>Why it exists:</b> To provide a structural basis for graph data.
 * <p>
 * <b>When to use it:</b> Use {@code Node}s to represent distinct entities in your domain model that participate in relationships (edges).
 * <p>
 * <b>Common usage patterns:</b>
 * <ul>
 * <li>Created via a {@link GraphFactory} and subsequently added to a {@link Graph}.</li>
 * <li>Decorated with boolean markers via its {@link TagSet} (e.g., {@code node.tags().add("Person")}).</li>
 * <li>Decorated with key-value data via its {@link AttributeMap} (e.g., {@code node.attributes().put("name", "Alice")}).</li>
 * </ul>
 * <p>
 * <b>Important invariants:</b> Like all {@link GraphElement}s, it possesses a primitive {@code int} identity. Identity is strictly defined by the combination of its implementation type and this primitive ID.
 * <p>
 * <b>Thread safety:</b> The interfaces within {@code pg-api} do not define thread safety guarantees. Assume nodes are not safe for concurrent mutation unless explicitly documented by the backing implementation (e.g., {@code GlobalGraph} vs. {@code EphemeralGraph}).
 * <p>
 * <b>Performance characteristics:</b> Node instances themselves are often lightweight flyweights or strictly primitive wrappers to reduce heap pressure. Property access is optimized based on the backend storage engine.
 */
public interface Node extends GraphElement {

    /**
     * Specifies the traversal direction relative to a node.
     * <ul>
     * <li>{@link #IN}: Traverses incoming edges (edges where this node is the {@link Edge#to()} destination).</li>
     * <li>{@link #OUT}: Traverses outgoing edges (edges where this node is the {@link Edge#from()} source).</li>
     * <li>{@link #BOTH}: Traverses all incident edges regardless of direction.</li>
     * </ul>
     */
    /** Specifies the direction of traversal or edge resolution. */
    enum NodeDirection {
        /** Inbound edges */
        IN,
        /** Outbound edges */
        OUT,
        /** Both inbound and outbound edges */
        BOTH
    }
}
