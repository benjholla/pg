package dev.chpg.pg.api;

/**
 * A factory for creating Node instances.
 */
/**
 * Factory for creating nodes.
 */
public interface NodeFactory {

    /**
     * Creates an immutable singleton set containing the specified node.
     */
    /**
     * Creates an immutable singleton set containing the specified node.
     * @param node the node
     * @return the singleton set
     */
    /**
     * Creates an immutable singleton set containing the specified node.
     * @param node the node
     * @return the singleton set
     */
    NodeSet singleton(Node node);

    /**
     * Instantiates and returns a new Node native to this graph's implementation.
     * Note: This does NOT add the node to the graph. Use addNode() for structural mutation.
     */
    /**
     * Instantiates and returns a new empty Node native to this graph's implementation.
     * @return the node
     */
    public Node createNode();

}
