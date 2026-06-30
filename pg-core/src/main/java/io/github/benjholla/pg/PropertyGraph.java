package io.github.benjholla.pg;

import java.util.Collection;

public class PropertyGraph extends AbstractGraph {

    /**
     * Constructs a new empty graph
     */
    public PropertyGraph() {
        super();
    }
    
    /**
     * Constructs a new graph of the given nodes
     */
    public PropertyGraph(Node... nodes) {
        super(nodes);
    }
    
    /**
     * Constructs a new graph of the given nodes
     */
    public PropertyGraph(NodeSet nodes) {
        super(nodes);
    }
    
    /**
     * Constructs a new graph of the given edges and respective edge nodes
     */
    public PropertyGraph(Edge... edges) {
        super(edges);
    }
    
    /**
     * Constructs a new graph of the given edges and respective edge nodes
     */
    public PropertyGraph(EdgeSet edges) {
        super(edges);
    }
    
    /**
     * Constructs a new graph of the given edges and respective edge nodes
     */
    public PropertyGraph(NodeSet nodes, EdgeSet edges) {
        super(nodes, edges);
    }
    
    /**
     * Constructs a new graph of the nodes and edges collectively contained in the given graphs
     */
    public PropertyGraph(Graph... graphs) {
        super(graphs);
    }
    
    /**
     * Constructs a new graph of the nodes and edges collectively contained in the given graphs
     */
    public PropertyGraph(Collection<Graph> graphs) {
        super(graphs);
    }
    
    @Override
    protected PropertyGraph newGraph() {
        return new PropertyGraph();
    }

    @Override
    protected PropertyGraph newGraph(Node... nodes) {
        return new PropertyGraph(nodes);
    }

    @Override
    protected PropertyGraph newGraph(NodeSet nodes) {
        return new PropertyGraph(nodes);
    }

    @Override
    protected PropertyGraph newGraph(Edge... edges) {
        return new PropertyGraph(edges);
    }

    @Override
    protected PropertyGraph newGraph(EdgeSet edges) {
        return new PropertyGraph(edges);
    }

    @Override
    protected PropertyGraph newGraph(NodeSet nodes, EdgeSet edges) {
        return new PropertyGraph(nodes, edges);
    }

    @Override
    protected PropertyGraph newGraph(Graph... graphs) {
        return new PropertyGraph(graphs);
    }

    @Override
    protected PropertyGraph newGraph(Collection<Graph> graphs) {
        return new PropertyGraph(graphs);
    }
	
}
