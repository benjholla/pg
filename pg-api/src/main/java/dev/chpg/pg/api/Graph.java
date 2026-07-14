package dev.chpg.pg.api;

import java.util.Collection;
import java.util.Optional;

import dev.chpg.pg.api.Node.NodeDirection;

/**
 * Represents a mathematical directed property graph.
 * <p>
 * <b>What it represents:</b> A container and query engine managing a collection of {@link Node}s and {@link Edge}s representing topological and property data.
 * <p>
 * <b>Why it exists:</b> To provide a set-theoretic algebra for manipulating property graphs. Rather than mutating a single monolithic graph, this library focuses on producing new subgraphs via algebraic operations (e.g., union, intersection).
 * <p>
 * <b>When to use it:</b> Use {@code Graph} when you need to construct, manipulate, query, or traverse connected data using set theory and relational algebra.
 * <p>
 * <b>Common usage patterns:</b>
 * <ul>
 * <li>Creating subgraphs via traversals (e.g., {@code graph.forward(node)}).</li>
 * <li>Combining or filtering results using set logic (e.g., {@code graph1.union(graph2)}).</li>
 * </ul>
 * <p>
 * <b>Important invariants:</b>
 * <ul>
 * <li><b>Cascading Edge Removal:</b> An edge cannot exist without its endpoints. Removing a node implicitly and cascades to remove all incident edges.</li>
 * <li><b>Element Identity:</b> Graph topology relies on the primitive {@code int} identity of {@link GraphElement}s for O(1) operations. Elements must be consistent in identity to be evaluated correctly.</li>
 * </ul>
 * <p>
 * <b>Thread safety:</b> The interface itself defines no thread safety guarantees. Refer to specific implementations (e.g., {@code GlobalGraph}, {@code EphemeralGraph}) for thread safety characteristics.
 * <p>
 * <b>Performance characteristics:</b> Operations like traversal and set algebra are heavily optimized based on primitive IDs. Query outputs are often deferred pipelines (zero-allocation) until strictly evaluated.
 */
public interface Graph {
	
    public static final java.util.Comparator<Graph> SIZE_COMPARATOR = new java.util.Comparator<Graph>() {
        @Override
        public int compare(Graph g1, Graph g2) {
            int nodes = Integer.compare(g1.nodes().size(), g2.nodes().size());
            if (nodes != 0) {
                return nodes;
            } else {
                return Integer.compare(g1.edges().size(), g2.edges().size());
            }
        }
    };
    
	/**
	 * Returns true if the graph contains the specified node.
	 */
	public boolean containsNode(Node node);

	/**
	 * Returns true if the graph contains the specified edge.
	 */
	public boolean containsEdge(Edge edge);

	/**
	 * Returns true if the graph contains all of the specified nodes.
	 */
	public boolean containsAllNodes(Collection<? extends Node> nodes);

	/**
	 * Returns true if the graph contains all of the specified edges.
	 */
	public boolean containsAllEdges(Collection<? extends Edge> edges);

	/**
	 * Returns the node denoted by the given id if one exists
	 */
	public Optional<Node> node(int id);
	
	/**
	 * Returns the edge denoted by the given id if one exists
	 */
	public Optional<Edge> edge(int id);
	
	/**
	 * Add a node to the graph
	 * 
	 * @return Returns true if the graph changed as a result of the operation
	 */
	public boolean addNode(Node node);
	
	/**
     * Add a edge to the graph
     * 
     * @return Returns true if the graph changed as a result of the operation
     */
    public boolean addEdge(Edge edge);
	/**
	 * Add an edge to the graph. Will fail if the terminal nodes are not present.
	 *
	 * @return Returns true if the graph changed as a result of the operation
	 */
	public boolean linkEdge(Edge edge);
	
	/**
	 * Add edges to the graph. Will fail if the terminal nodes are not present.
	 *
	 * @return Returns true if the graph changed as a result of the operation
	 */
	public boolean linkAllEdges(Collection<? extends Edge> edges);

    /**
     * Add nodes to the graph
     * 
     * @return Returns true if the graph changed as a result of the operation
     */
	public boolean addAllNodes(Collection<? extends Node> nodes);
	
    /**
     * Add edges to the graph
     * 
     * @return Returns true if the graph changed as a result of the operation
     */
    public boolean addAllEdges(Collection<? extends Edge> edges);
    
    /**
     * Remove a node from the graph if the given node exists in this graph
     */
    public boolean removeNode(Node node);
    
    /**
     * Remove a edge from the graph if the given node exists in this graph
     * 
     * @return Returns true if the graph changed as a result of the operation
     */
    public boolean removeEdge(Edge edge);
    
    /**
     * Remove all nodes from the graph if the given nodes exist in this graph
     */
    public boolean removeAllNodes(Collection<? extends Node> nodes);
    
    /**
     * Remove all edges from the graph if the given edges exist in this graph
     * 
     * @return Returns true if the graph changed as a result of the operation
     */
    public boolean removeAllEdges(Collection<? extends Edge> edges);
    
    /**
     * Retains only the nodes in this graph that are contained in the
     * specified collection (optional operation).  In other words, removes
     * from this graph all of its nodes that are not contained in the
     * specified collection.  If the specified collection is also a set, this
     * operation effectively modifies this set so that its value is the
     * <i>intersection</i> of the two sets.
     * 
     * @return Returns true if the graph changed as a result of the operation
     */
    public boolean retainAllNodes(Collection<? extends Node> nodes);
    
    /**
     * Retains only the edges in this graph that are contained in the
     * specified collection (optional operation).  In other words, removes
     * from this graph all of its edges that are not contained in the
     * specified collection.  If the specified collection is also a set, this
     * operation effectively modifies this set so that its value is the
     * <i>intersection</i> of the two sets.
     * 
     * @return Returns true if the graph changed as a result of the operation
     */
    public boolean retainAllEdges(Collection<? extends Edge> edges);
    
    /**
     * Removes all of the edge elements from this graph (leaving only nodes if they
     * exist).
     */
    public void clearEdges();
    
    /**
     * Removes all of the node and edge elements from this graph. The graph will be
     * empty after this call returns.
     */
    public void clear();
    
	/**
	 * Return an immutable set of nodes in the graph
	 */
	public NodeSet nodes();

	/**
     * Return an immutable set of edges in the graph
     */
	public EdgeSet edges();
	
	/**
	 * Returns true if the graph empty (has no nodes)
	 */
	public boolean isEmpty();
	
	/**
	 * Gets the node's predecessor or successor edges in this graph
	 */
	public EdgeSet edges(Node node, NodeDirection direction);
	
	/**
	 * Returns the nodes in the graph without edges from the given direction
	 */
	public NodeSet limit(NodeDirection direction);
	
	/**
	 * Selects the nodes of this graph that have no successors
	 * 
	 * Convenience for limit(NodeDirection.OUT)
	 */
	public NodeSet leaves();
	
	/**
	 * Selects the nodes of this graph that have no predecessors
	 * 
	 * Convenience for limit(NodeDirection.IN)
	 */
	public NodeSet roots();
	
	/**
	 * Gets the predecessor nodes of the given node for this graph's edges
	 * 
	 * @return The set of nodes reachable from incoming edges to the given nodes
	 */
	public NodeSet predecessors(Node... origin);
	
	/**
	 * Gets the predecessor nodes of the given node for this graph's edges
	 * 
	 * @return The set of nodes reachable from incoming edges to the given nodes
	 */
	public NodeSet predecessors(Graph origin);
	
	/**
	 * Gets the predecessor nodes of the given node for this graph's edges
	 * 
	 * @return The set of nodes reachable from incoming edges to the given nodes
	 */
	public NodeSet predecessors(NodeSet origin);
	
	/**
	 * Gets the successor nodes of the given node for this graph's edges
	 * 
	 * @return The set of nodes reachable from outgoing edges from the given nodes
	 */
	public NodeSet successors(Node... origin);
	
	/**
	 * Gets the successor nodes of the given node for this graph's edges
	 * 
	 * @return The set of nodes reachable from outgoing edges from the given nodes
	 */
	public NodeSet successors(Graph origin);
	
	/**
	 * Gets the successor nodes of the given node for this graph's edges
	 * 
	 * @return The set of nodes reachable from outgoing edges from the given nodes
	 */
	public NodeSet successors(NodeSet origin);
	
	/**
	 * From this graph, selects the subgraph reachable from the given nodes
	 * along a path length of 1 in the forward direction.
	 * 
	 * The final result includes the given nodes, the traversed edges, and the
	 * reachable nodes.
	 */
	public Graph forwardStep(Node... origin);
	
	/**
	 * From this graph, selects the subgraph reachable from the given nodes
	 * along a path length of 1 in the forward direction.
	 * 
	 * The final result includes the given nodes, the traversed edges, and the
	 * reachable nodes.
	 */
	public Graph forwardStep(Graph origin);
	
	/**
	 * From this graph, selects the subgraph reachable from the given nodes
	 * along a path length of 1 in the forward direction.
	 * 
	 * The final result includes the given nodes, the traversed edges, and the
	 * reachable nodes.
	 */
	public Graph forwardStep(NodeSet origin);
	
	/**
	 * From this graph, selects the subgraph reachable from the given nodes
	 * along a path length of 1 in the reverse direction.
	 * 
	 * The final result includes the given nodes, the traversed edges, and the
	 * reachable nodes.
	 */
	public Graph reverseStep(Node... origin);
	
	/**
	 * From this graph, selects the subgraph reachable from the given nodes
	 * along a path length of 1 in the reverse direction.
	 * 
	 * The final result includes the given nodes, the traversed edges, and the
	 * reachable nodes.
	 */
	public Graph reverseStep(Graph origin);
	
	/**
	 * From this graph, selects the subgraph reachable from the given nodes
	 * along a path length of 1 in the reverse direction.
	 * 
	 * The final result includes the given nodes, the traversed edges, and the
	 * reachable nodes.
	 */
	public Graph reverseStep(NodeSet origin);
	
	/**
	 * Yields the union of this graph and a new graph formed by the given nodes.
	 */
	public Graph union(Node... nodes);
	
	/**
	 * Yields the union of this graph and a new graph formed by the given edges.
	 */
	public Graph union(Edge... edges);
	
	/**
	 * Yields the union of this graph and the given graphs. That is, the
	 * resulting graph's nodes are the union of all nodes, and likewise for
	 * edges.
	 */
	public Graph union(Graph... graphs);
	
	/**
	 * Select this graph, excluding the given nodes. Note that, because
	 * an edge is only in a graph if it's nodes are in a graph, removing a node
	 * will necessarily remove the edges it connects as well.
	 */
	public Graph difference(Node... nodes);
	
	/**
	 * Select this graph, excluding the given edges. Note that, because
	 * an edge is only in a graph if it's nodes are in a graph, removing an edge
	 * will necessarily remove the nodes it connects as well. Removing either
	 * node would remove the edge as well.
	 * 
	 * This behavior may seem counter-intuitive if one is thinking in terms of
	 * removing a single edge from a graph. Consider the graphs: - g1: a -> b ->
	 * c - g2: a -> b g1.difference(g2) yields the graph containing only node c:
	 * because b is removed, so b -> c is also removed. In general, this
	 * operation is useful for removing nodes from a graph, but may not be as
	 * useful for operating on edges.
	 */
	public Graph difference(Edge... edges);
	
	/**
	 * Select this graph, excluding the graphs g. Note that, because
	 * an edge is only in a graph if it's nodes are in a graph, removing an edge
	 * will necessarily remove the nodes it connects as well. Removing either
	 * node would remove the edge as well.
	 * 
	 * This behavior may seem counter-intuitive if one is thinking in terms of
	 * removing a single edge from a graph. Consider the graphs: - g1: a -> b ->
	 * c - g2: a -> b g1.difference(g2) yields the graph containing only node c:
	 * because b is removed, so b -> c is also removed. In general, this
	 * operation is useful for removing nodes from a graph, but may not be as
	 * useful for operating on edges.
	 * 
	 */
	public Graph difference(Graph... graphs);
	
	/**
	 * Select this graph, excluding the given edges.
	 */
	public Graph differenceEdges(Edge... edges);
	
    /**
     * Select this graph, excluding the edges from the given graphs.
     * 
     */
    public Graph differenceEdges(Graph... graphs);

    /**
     * Yields the intersection of this graph and a new graph formed by the given
     * nodes. That is, the resulting graph's nodes are the intersection of all node
     * sets, and likewise for edges.
     */
    public Graph intersection(Node... nodes);

    /**
     * Yields the intersection of this graph and a new graph formed by the given
     * edges. That is, the resulting graph's nodes are the intersection of all node
     * sets, and likewise for edges.
     * 
     */
    public Graph intersection(Edge... edges);

    /**
     * Yields the intersection of this graph and the given graphs. That is, the
     * resulting graph's nodes are the intersection of all node sets, and likewise
     * for edges.
     */
    public Graph intersection(Graph... graphs);

    /**
     * From this graph, selects the subgraph such that the given nodes in to are
     * reachable from the nodes in from in a single step
     */
    public Graph betweenStep(Node from, Node to);

    /**
     * From this graph, selects the subgraph such that the given nodes in to are
     * reachable from the nodes in from in a single step
     * 
     */
    public Graph betweenStep(Graph from, Graph to);

    /**
     * From this graph, selects the subgraph such that the given nodes in to are
     * reachable from the nodes in from in a single step
     */
    public Graph betweenStep(NodeSet from, NodeSet to);

    /**
     * From this graph, selects the subgraph such that the given nodes in to are
     * reachable from the nodes in from using forward traversal.
     * 
     * Logically equivalent to graph.forward(from).intersection(graph.reverse(to)) .
     */
    public Graph between(Node from, Node to);

    /**
     * From this graph, selects the subgraph such that the given nodes in to are
     * reachable from the nodes in from using forward traversal.
     * 
     * Logically equivalent to graph.forward(from).intersection(graph.reverse(to)) .
     */
    public Graph between(Graph from, Graph to);

    /**
     * From this graph, selects the subgraph such that the given nodes in to are
     * reachable from the nodes in from using forward traversal.
     * 
     * Logically equivalent to graph.forward(from).intersection(graph.reverse(to)) .
     */
    public Graph between(NodeSet from, NodeSet to);

    /**
     * From this graph, selects the subgraph reachable from the given nodes using
     * forward transitive traversal.
     */
    public Graph forward(Node... origin);

    /**
     * From this graph, selects the subgraph reachable from the given nodes using
     * forward transitive traversal.
     */
    public Graph forward(Graph origin);

    /**
     * From this graph, selects the subgraph reachable from the given nodes using
     * forward transitive traversal.
     */
    public Graph forward(NodeSet origin);

    /**
     * From this graph, selects the subgraph reachable from the given nodes using
     * reverse transitive traversal.
     */
    public Graph reverse(Node... origin);

    /**
     * From this graph, selects the subgraph reachable from the given nodes using
     * reverse transitive traversal.
     */
    public Graph reverse(Graph origin);

    /**
     * From this graph, selects the subgraph reachable from the given nodes using
     * reverse transitive traversal.
     */
    public Graph reverse(NodeSet origin);

    /**
     * Yields the induced graph formed from the nodes in the current graph and all
     * of the edges in the given graph that connect pairs of nodes in the current
     * graph.
     */
    public Graph induce(Edge... edges);

    /**
     * Yields the induced graph formed from the nodes in the current graph and all
     * of the edges in the given graph that connect pairs of nodes in the current
     * graph.
     */
    public Graph induce(Graph... graphs);

    /**
     * Yields the induced graph formed from the nodes in the current graph and all
     * of the edges in the given graph that connect pairs of nodes in the current
     * graph.
     */
    public Graph induce(EdgeSet edges);
/**
     * Returns true if there is at least one edge originating at the source and terminating at the target.
     */
    public boolean adjacent(Node source, Node target);

    /**
     * Returns the set of edges that originate at the source and terminate at the target.
     */
    public EdgeSet edges(Node source, Node target);

    /**
     * Returns the number of edges connected to this node in the specified direction.
     */
    public int degree(Node node, NodeDirection direction);

    /*
     * NOTE: Graph-Level Attributes
     * Graph-level attributes are temporarily deferred to prevent the graph from
     * becoming a global state bucket. This API will be activated strictly when
     * required for universal topological contexts (e.g., semantic coordinate systems
     * for geospatial projections).
     *
     * public AttributeMap attributes();
     */

}
