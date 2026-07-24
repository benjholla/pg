package dev.chpg.pg.api;

/**
 * A factory for creating Node instances.
 * <p>
 * <b>What it represents:</b> A builder interface responsible for instantiating concrete {@link Node} instances.
 * <p>
 * <b>Why it exists:</b> To decouple the creation of graph nodes from the concrete graph implementations. It ensures that nodes are created with the correct implementation-specific type and IDs without hardcoding dependencies.
 * <p>
 * <b>When to use it:</b> Use {@code NodeFactory} when you need to create new nodes to add to a graph.
 * <p>
 * <b>Common usage patterns:</b>
 * <ul>
 * <li>Creating a new node via {@code graph.factory().createNode()}.</li>
 * </ul>
 * <p>
 * <b>Important invariants:</b> The factory must create nodes that are compatible with the graph instance that owns the factory.
 * <p>
 * <b>Thread safety:</b> Implementations of this factory should ideally be thread-safe to allow concurrent node creation.
 * <p>
 * <b>Performance characteristics:</b> Node creation should be fast and have minimal allocation overhead.
 */
public interface NodeFactory {

    /**
     * Creates an immutable singleton set containing the specified node.
     *
     * @param node the node to include in the set
     * @return an immutable singleton node set
     */
    NodeSet singleton(Node node);

    /**
     * Instantiates and returns a new Node native to this graph's implementation.
     * Note: This does NOT add the node to the graph. Use addNode() for structural mutation.
     *
     * @return a new node instance
     */
    public Node createNode();

}
