package io.github.benjholla.pg.api;

import java.util.Collection;
import java.util.Optional;

import io.github.benjholla.pg.api.Node.NodeDirection;

/**
 * Represents a mathematical directed property graph.
 * <p>
 * A {@code Graph} acts as the primary container and query engine for {@link Node}s and {@link Edge}s.
 * This library embraces a <b>set-theoretic</b> approach to graph operations. Rather than modifying
 * a single monolithic graph during analysis, operations like {@link #union(Graph...)},
 * {@link #intersection(Graph...)}, and traversals like {@link #forward(Node...)} typically yield
 * <i>new</i> graphs (subgraphs) representing the result of the query.
 * <p>
 * <b>Important Characteristics:</b>
 * <ul>
 * <li><b>Cascading Edge Removal:</b> Because an edge cannot exist without its endpoints, removing a
 * node from the graph automatically removes any incident edges.
 * <i>Note: This domain-specific behavior means graph set difference operations do not strictly adhere
 * to all standard set-theoretic algebraic laws.</i></li>
 * <li><b>Element Identity:</b> Graphs manage topology and rely on the primitive {@code int} identity
 * of {@link GraphElement}s for fast O(1) routing and adjacency lookups.</li>
 * </ul>
 */
public interface Graph {
	
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
     * Remove a node from the graph
     */
    public boolean removeNode(Node node);
    
    /**
     * Remove a edge from the graph
     * 
     *  @return Returns true if the graph changed as a result of the operation
     */
    public boolean removeEdge(Edge edge);

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
	 * c - g2: a -> b g1.remove(g2) yields the graph containing only node c:
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
	 * c - g2: a -> b g1.remove(g2) yields the graph containing only node c:
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
     * A convenience method for nodes(String... tags)
     */
    public NodeSet nodes(String... tags);

    /**
     * Returns the set of nodes from this graph that are tagged with all of the
     * given tags
     */
    public NodeSet nodesTaggedWithAny(String... tags);

    /**
     * Returns the set of nodes from this graph that are tagged with any of the
     * given tags
     */
    public NodeSet nodesTaggedWithAll(String... tags);

    /**
     * A convenience method for edges(String... tags)
     */
    public EdgeSet edges(String... tags);

    /**
     * Returns the set of edges from this graph that are tagged with any of the
     * given tags
     */
    public EdgeSet edgesTaggedWithAny(String... tags);

    /**
     * Returns the set of edges from this graph that are tagged with all of the
     * given tags
     */
    public EdgeSet edgesTaggedWithAll(String... tags);

    /**
     * Select subgraph containing edges that have the given attribute key defined,
     * with any value.
     */
    public EdgeSet selectEdges(String attribute);

    /**
     * Select subgraph contain edges that have the given attribute key with any
     * value specified in the given values.
     */
    public EdgeSet selectEdges(String attribute, AttributeValue... values);

    /**
     * Select subgraph containing nodes that have a given key defined, with any
     * value.
     */
    public NodeSet selectNodes(String attribute);

    /**
     * Select subgraph containing nodes that have a given key with any value
     * specified in the given values.
     */
    public NodeSet selectNodes(String attribute, AttributeValue... values);
	
}
