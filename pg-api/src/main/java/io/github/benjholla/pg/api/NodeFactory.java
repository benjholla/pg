package io.github.benjholla.pg.api;

/**
 * A factory for creating Node instances.
 */
public interface NodeFactory {

    /**
     * Instantiates and returns a new Node native to this graph's implementation.
     * Note: This does NOT add the node to the graph. Use addNode() for structural mutation.
     */
    public Node createNode();

}
