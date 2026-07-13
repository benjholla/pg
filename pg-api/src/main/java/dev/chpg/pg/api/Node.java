package dev.chpg.pg.api;

/**
 * Represents a vertex within a property graph.
 * <p>
 * A {@code Node} serves as a fundamental building block of the graph, connecting to other nodes via
 * directed {@link Edge}s. Like all {@link GraphElement}s, it possesses a primitive {@code int} identity,
 * a {@link TagSet} for labeling, and an {@link AttributeMap} for property storage.
 * <p>
 * <b>Usage:</b> Nodes are created via a {@link GraphFactory} and added to a {@link Graph}.
 * Filtering or querying nodes is typically done using functional operators on a {@link NodeSet}.
 * <p>
 * <b>Thread Safety:</b> The interfaces within {@code pg-api} do not define thread safety guarantees.
 * Thread safety is determined by the specific concrete implementation (e.g., {@code GlobalGraph} vs. {@code EphemeralGraph}).
 * Assume nodes are not safe for concurrent mutation unless explicitly documented by the backing engine.
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
    enum NodeDirection { IN, OUT, BOTH; }
}
