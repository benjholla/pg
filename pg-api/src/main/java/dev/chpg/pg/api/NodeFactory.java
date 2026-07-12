package dev.chpg.pg.api;

/**
 * A factory for creating Node instances.
 */
public interface NodeFactory {

    /**
     * Creates an immutable singleton set containing the specified node.
     */
    NodeSet singleton(Node node);

    /**
     * Instantiates and returns a new Node native to this graph's implementation.
     * Note: This does NOT add the node to the graph. Use addNode() for structural mutation.
     */
    public Node createNode();

}
