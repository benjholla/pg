package dev.chpg.pg.api;

/**
 * Represents a directed connection between two {@link Node}s in a property graph.
 * <p>
 * An {@code Edge} strictly dictates a direction, originating at the {@link #from()} node and terminating
 * at the {@link #to()} node. Like all {@link GraphElement}s, it possesses a primitive {@code int} identity,
 * a {@link TagSet} for labeling (e.g., relationship type), and an {@link AttributeMap} for property storage.
 * <p>
 * <b>Important Invariant:</b> Edges cannot exist independently of their endpoints. Attempting to add an edge
 * to a graph implicitly requires (or will add) its corresponding {@code from} and {@code to} nodes. Removing
 * either connected node will cascade and result in the removal of the edge.
 * <p>
 * <b>Usage:</b> Edges are created via a {@link GraphFactory} by supplying the source and target nodes.
 * They represent the structural topology used during transitive traversals (e.g., {@link Graph#forward(Node...)}).
 * <p>
 * <b>Thread Safety:</b> As with {@link Node}, thread safety guarantees are delegated to the specific
 * backend implementation. Assume edges are not safe for concurrent mutation unless specified otherwise.
 */
public interface Edge extends GraphElement {

    /**
     * Returns the source node from which this directed edge originates.
     */
    Node from();

    /**
     * Returns the target node at which this directed edge terminates.
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
