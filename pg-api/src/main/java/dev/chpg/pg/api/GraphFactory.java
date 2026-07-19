package dev.chpg.pg.api;

import java.util.Collection;

/**
 * A factory for creating Graph instances.
 */
/**
 * Factory for creating graphs.
 */
public interface GraphFactory {

    /**
     * Instantiates and returns a new empty Graph native to this implementation.
     */
    /**
     * Instantiates and returns a new empty Graph native to this implementation.
     * @return the graph
     */
    public Graph createGraph();

    /**
     * Instantiates and returns a new Graph native to this implementation containing the given nodes.
     */
    /**
     * Instantiates and returns a new Graph native to this implementation containing the given nodes.
     * @param nodes the nodes
     * @return the graph
     */
    public Graph createGraph(Node... nodes);

    /**
     * Instantiates and returns a new Graph native to this implementation containing the given nodes.
     */
    /**
     * Instantiates and returns a new Graph native to this implementation containing the given nodes.
     * @param nodes the nodes
     * @return the graph
     */
    public Graph createGraph(NodeSet nodes);

    /**
     * Instantiates and returns a new Graph native to this implementation containing the given edges.
     */
    /**
     * Instantiates and returns a new Graph native to this implementation containing the given edges.
     * @param edges the edges
     * @return the graph
     */
    public Graph createGraph(Edge... edges);

    /**
     * Instantiates and returns a new Graph native to this implementation containing the given edges.
     */
    /**
     * Instantiates and returns a new Graph native to this implementation containing the given edges.
     * @param edges the edges
     * @return the graph
     */
    public Graph createGraph(EdgeSet edges);

    /**
     * Instantiates and returns a new Graph native to this implementation containing the given nodes and edges.
     */
    /**
     * Instantiates and returns a new Graph native to this implementation containing the given nodes and edges.
     * @param nodes the nodes
     * @param edges the edges
     * @return the graph
     */
    public Graph createGraph(NodeSet nodes, EdgeSet edges);

    /**
     * Instantiates and returns a new Graph native to this implementation containing the given graphs.
     */
    /**
     * Instantiates and returns a new Graph native to this implementation containing the given graphs.
     * @param graphs the graphs
     * @return the graph
     */
    public Graph createGraph(Graph... graphs);

    /**
     * Instantiates and returns a new Graph native to this implementation containing the given graphs.
     */
    /**
     * Instantiates and returns a new Graph native to this implementation containing the given graphs.
     * @param graphs the graphs
     * @return the graph
     */
    public Graph createGraph(Collection<Graph> graphs);

}
