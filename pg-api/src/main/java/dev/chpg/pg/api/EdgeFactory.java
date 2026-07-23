package dev.chpg.pg.api;

/**
 * A factory for creating Edge instances.
 * <p>
 * <b>What it represents:</b> A builder interface responsible for instantiating concrete {@link Edge} instances.
 * <p>
 * <b>Why it exists:</b> To decouple the creation of graph edges from the concrete graph implementations. It ensures that edges are created with the correct implementation-specific type and IDs without hardcoding dependencies.
 * <p>
 * <b>When to use it:</b> Use {@code EdgeFactory} when you need to create new edges to add to a graph.
 * <p>
 * <b>Common usage patterns:</b>
 * <ul>
 * <li>Creating a new edge via {@code graph.factory().createEdge(source, target)}.</li>
 * </ul>
 * <p>
 * <b>Important invariants:</b> The factory must create edges that are compatible with the graph instance that owns the factory.
 * <p>
 * <b>Thread safety:</b> Implementations of this factory should ideally be thread-safe to allow concurrent edge creation.
 * <p>
 * <b>Performance characteristics:</b> Edge creation should be fast and have minimal allocation overhead.
 */
public interface EdgeFactory {

    /**
     * Creates an immutable singleton set containing the specified edge.
     * @param edge the edge
     * @return the resulting edge set
     */
    EdgeSet singleton(Edge edge);

    /**
     * Instantiates and returns a new Edge native to this graph's implementation.
     * @param source the source
     * @param target the target
     * @return the edge
     */
    public Edge createEdge(Node source, Node target);

}
