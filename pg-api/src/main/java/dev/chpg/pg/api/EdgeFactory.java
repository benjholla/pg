package dev.chpg.pg.api;

/**
 * A factory for creating Edge instances.
 */
/**
 * Factory for creating edges.
 */
public interface EdgeFactory {

    /**
     * Creates an immutable singleton set containing the specified edge.
     */
    /**
     * Creates an immutable singleton set containing the specified edge.
     * @param edge the edge
     * @return the singleton set
     */
    /**
     * Creates an immutable singleton set containing the specified edge.
     * @param edge the edge
     * @return the singleton set
     */
    EdgeSet singleton(Edge edge);

    /**
     * Instantiates and returns a new Edge native to this graph's implementation.
     */
    /**
     * Instantiates and returns a new Edge native to this graph's implementation.
     * @param source the source node
     * @param target the target node
     * @return the edge
     */
    public Edge createEdge(Node source, Node target);

}
