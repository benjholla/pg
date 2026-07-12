package dev.chpg.pg.api;

/**
 * A factory for creating Edge instances.
 */
public interface EdgeFactory {

    /**
     * Creates an immutable singleton set containing the specified edge.
     */
    EdgeSet singleton(Edge edge);

    /**
     * Instantiates and returns a new Edge native to this graph's implementation.
     */
    public Edge createEdge(Node source, Node target);

}
