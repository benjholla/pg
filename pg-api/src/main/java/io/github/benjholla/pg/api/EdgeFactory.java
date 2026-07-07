package io.github.benjholla.pg.api;

/**
 * A factory for creating Edge instances.
 */
public interface EdgeFactory {

    /**
     * Instantiates and returns a new Edge native to this graph's implementation.
     */
    public Edge createEdge(Node source, Node target);

}
