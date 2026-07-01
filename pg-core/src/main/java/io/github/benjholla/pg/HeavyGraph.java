package io.github.benjholla.pg;

import java.util.Collection;

public class HeavyGraph extends AbstractGraph {

    /**
     * Constructs a new empty graph
     */
    public HeavyGraph() {
        super();
    }
    
    /**
     * Constructs a new graph of the given nodes
     */
    public HeavyGraph(Node... nodes) {
        super(nodes);
    }
    
    /**
     * Constructs a new graph of the given nodes
     */
    public HeavyGraph(NodeSet nodes) {
        super(nodes);
    }
    
    /**
     * Constructs a new graph of the given edges and respective edge nodes
     */
    public HeavyGraph(Edge... edges) {
        super(edges);
    }
    
    /**
     * Constructs a new graph of the given edges and respective edge nodes
     */
    public HeavyGraph(EdgeSet edges) {
        super(edges);
    }
    
    /**
     * Constructs a new graph of the given edges and respective edge nodes
     */
    public HeavyGraph(NodeSet nodes, EdgeSet edges) {
        super(nodes, edges);
    }
    
    /**
     * Constructs a new graph of the nodes and edges collectively contained in the given graphs
     */
    public HeavyGraph(Graph... graphs) {
        super(graphs);
    }
    
    /**
     * Constructs a new graph of the nodes and edges collectively contained in the given graphs
     */
    public HeavyGraph(Collection<Graph> graphs) {
        super(graphs);
    }
    
    @Override
    protected HeavyGraph newGraph() {
        return new HeavyGraph();
    }

    @Override
    protected HeavyGraph newGraph(Node... nodes) {
        return new HeavyGraph(nodes);
    }

    @Override
    protected HeavyGraph newGraph(NodeSet nodes) {
        return new HeavyGraph(nodes);
    }

    @Override
    protected HeavyGraph newGraph(Edge... edges) {
        return new HeavyGraph(edges);
    }

    @Override
    protected HeavyGraph newGraph(EdgeSet edges) {
        return new HeavyGraph(edges);
    }

    @Override
    protected HeavyGraph newGraph(NodeSet nodes, EdgeSet edges) {
        return new HeavyGraph(nodes, edges);
    }

    @Override
    protected HeavyGraph newGraph(Graph... graphs) {
        return new HeavyGraph(graphs);
    }

    @Override
    protected HeavyGraph newGraph(Collection<Graph> graphs) {
        return new HeavyGraph(graphs);
    }
	
}
