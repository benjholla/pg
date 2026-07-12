package dev.chpg.pg.api;

import java.util.Collection;

/**
 * A factory for creating Graph instances.
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
     * Instantiates and returns a new Graph native to this implementation containing the given graphs.
     */
    public Graph createGraph(Graph... graphs);

    /**
     * Instantiates and returns a new Graph native to this implementation containing the given graphs.
     */
    public Graph createGraph(Collection<Graph> graphs);

}
