package dev.chpg.pg.api;


/**
 * A factory for creating Graph instances.
 * <p>
 * <b>What it represents:</b> A builder interface responsible for instantiating concrete {@link Graph} instances.
 * <p>
 * <b>Why it exists:</b> To decouple the creation of graphs from the concrete graph implementations. It ensures that graphs are created with the correct implementation-specific type without hardcoding dependencies.
 * <p>
 * <b>When to use it:</b> Use {@code GraphFactory} when you need to create new graphs or subgraphs.
 * <p>
 * <b>Common usage patterns:</b>
 * <ul>
 * <li>Creating a new empty graph via {@code factory.createGraph()}.</li>
 * <li>Creating a subgraph from a set of nodes and edges via {@code factory.createGraph(nodes, edges)}.</li>
 * </ul>
 * <p>
 * <b>Important invariants:</b> The factory must create graphs that are compatible with the implementation that owns the factory.
 * <p>
 * <b>Thread safety:</b> Implementations of this factory should ideally be thread-safe to allow concurrent graph creation.
 * <p>
 * <b>Performance characteristics:</b> Graph creation should be fast and have minimal allocation overhead.
 */
public interface GraphFactory {

    /**
     * Instantiates and returns a new empty Graph native to this implementation.
     */
    public Graph createGraph();

    /**
     * Instantiates and returns a new Graph native to this implementation containing the given nodes.
     */
    public Graph createGraph(Node... nodes);

    /**
     * Instantiates and returns a new Graph native to this implementation containing the given nodes.
     */
    public Graph createGraph(NodeSet nodes);

    /**
     * Instantiates and returns a new Graph native to this implementation containing the given edges.
     */
    public Graph createGraph(Edge... edges);

    /**
     * Instantiates and returns a new Graph native to this implementation containing the given edges.
     */
    public Graph createGraph(EdgeSet edges);

    /**
     * Instantiates and returns a new Graph native to this implementation containing the given nodes and edges.
     */
    public Graph createGraph(NodeSet nodes, EdgeSet edges);

    /**
     * Instantiates and returns a new Graph native to this implementation containing the given graph.
     */
    public Graph createGraph(Graph graph);

}
