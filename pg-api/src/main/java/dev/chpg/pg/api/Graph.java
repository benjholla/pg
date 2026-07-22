package dev.chpg.pg.api;

import java.util.Collection;
import java.util.Optional;

import dev.chpg.pg.api.Node.NodeDirection;

/**
 * Represents a mathematical directed property graph.
 * <p>
 * <b>What it represents:</b> A container and query engine managing a collection
 * of {@link Node}s and {@link Edge}s representing topological and property
 * data.
 * <p>
 * <b>Why it exists:</b> To provide a set-theoretic algebra for manipulating
 * property graphs. Rather than mutating a single monolithic graph, this library
 * focuses on producing new subgraphs via algebraic operations (e.g., union,
 * intersection).
 * <p>
 * <b>When to use it:</b> Use {@code Graph} when you need to construct,
 * manipulate, query, or traverse connected data using set theory and relational
 * algebra.
 * <p>
 * <b>Common usage patterns:</b>
 * <ul>
 * <li>Creating subgraphs via traversals (e.g.,
 * {@code graph.forward(node)}).</li>
 * <li>Combining or filtering results using set logic (e.g.,
 * {@code graph1.union(graph2)}).</li>
 * </ul>
 * <p>
 * <b>Important invariants:</b>
 * <ul>
 * <li><b>Cascading Edge Removal:</b> An edge cannot exist without its
 * endpoints. Removing a node implicitly and cascades to remove all incident
 * edges.</li>
 * <li><b>Element Identity:</b> Graph topology relies on the primitive
 * {@code int} identity of {@link GraphElement}s for O(1) operations. Elements
 * must be consistent in identity to be evaluated correctly.</li>
 * </ul>
 * <p>
 * <b>Thread safety:</b> The interface itself defines no thread safety
 * guarantees. Refer to specific implementations (e.g., {@code GlobalGraph},
 * {@code EphemeralGraph}) for thread safety characteristics.
 * <p>
 * <b>Performance characteristics:</b> Operations like traversal and set algebra
 * are heavily optimized based on primitive IDs. Query outputs are often
 * deferred pipelines (zero-allocation) until strictly evaluated.
 * 
 */
public interface Graph {
    /**
     * Returns the node denoted by the given id if one exists
     * 
     * @param id the ID
     */
    public Optional<Node> node(int id);

    /**
     * Returns the edge denoted by the given id if one exists
     * 
     * @param id the ID
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
     * @param edge the edge
     */
    public boolean addEdge(Edge edge);

    /**
     * Add an edge to the graph. Will fail if the terminal nodes are not present.
     *
     * @return Returns true if the graph changed as a result of the operation
     * @param edge the edge
     */
    public boolean linkEdge(Edge edge);

    /**
     * Add edges to the graph. Will fail if the terminal nodes are not present.
     *
     * @return Returns true if the graph changed as a result of the operation
     * @param edges the edges
     */
    public boolean linkAllEdges(Collection<? extends Edge> edges);

    /**
     * Add nodes to the graph
     * 
     * @return Returns true if the graph changed as a result of the operation
     * @param nodes the nodes
     */
    public boolean addAllNodes(Collection<? extends Node> nodes);

    /**
     * Add edges to the graph
     * 
     * @return Returns true if the graph changed as a result of the operation
     * @param edges the edges
     */
    public boolean addAllEdges(Collection<? extends Edge> edges);

    /**
     * Remove a node from the graph if the given node exists in this graph
     * 
     */
    public boolean removeNode(Node node);

    /**
     * Remove a edge from the graph if the given node exists in this graph
     * 
     * @return Returns true if the graph changed as a result of the operation
     * @param edge the edge
     */
    public boolean removeEdge(Edge edge);

    /**
     * Remove all nodes from the graph if the given nodes exist in this graph
     * 
     * @param nodes the nodes
     */
    public boolean removeAllNodes(Collection<? extends Node> nodes);

    /**
     * Remove all edges from the graph if the given edges exist in this graph
     * 
     * @return Returns true if the graph changed as a result of the operation
     * @param edges the edges
     */
    public boolean removeAllEdges(Collection<? extends Edge> edges);

    /**
     * Retains only the nodes in this graph that are contained in the specified
     * collection (optional operation). In other words, removes from this graph all
     * of its nodes that are not contained in the specified collection. If the
     * specified collection is also a set, this operation effectively modifies this
     * set so that its value is the <i>intersection</i> of the two sets.
     * 
     * @return Returns true if the graph changed as a result of the operation
     * @param nodes the nodes
     */
    public boolean retainAllNodes(Collection<? extends Node> nodes);

    /**
     * Retains only the edges in this graph that are contained in the specified
     * collection (optional operation). In other words, removes from this graph all
     * of its edges that are not contained in the specified collection. If the
     * specified collection is also a set, this operation effectively modifies this
     * set so that its value is the <i>intersection</i> of the two sets.
     * 
     * @return Returns true if the graph changed as a result of the operation
     * @param edges the edges
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
     * Gets the node's predecessor or successor edges in this graph
     * 
     * @param node      the node
     * @param direction the direction
     */
    public EdgeSet edges(Node node, NodeDirection direction);

    /**
     * Selects the nodes of this graph that have no successors (out-degree == 0).
     */
    public NodeSet leaves();

    /**
     * Selects the nodes of this graph that have no predecessors (in-degree == 0).
     */
    public NodeSet roots();

    /**
     * Selects the nodes of this graph that have no incident edges (degree == 0).
     */
    public NodeSet isolated();

    /**
     * Gets the predecessor nodes of the given node for this graph's edges
     *
     * @return The set of nodes reachable from incoming edges to the given nodes
     * @param origin the origin element
     */
    public NodeSet predecessors(Node origin);

    /**
     * Gets the predecessor nodes of the given node for this graph's edges
     *
     * @return The set of nodes reachable from incoming edges to the given nodes
     * @param origin the origin element
     */
    public NodeSet predecessors(Graph origin);

    /**
     * Gets the predecessor nodes of the given node for this graph's edges
     *
     * @return The set of nodes reachable from incoming edges to the given nodes
     * @param origin the origin element
     */
    public NodeSet predecessors(NodeSet origin);

    /**
     * Gets the successor nodes of the given node for this graph's edges
     *
     * @return The set of nodes reachable from outgoing edges from the given nodes
     * @param origin the origin element
     */
    public NodeSet successors(Node origin);

    /**
     * Gets the successor nodes of the given node for this graph's edges
     *
     * @return The set of nodes reachable from outgoing edges from the given nodes
     * @param origin the origin element
     */
    public NodeSet successors(Graph origin);

    /**
     * Gets the successor nodes of the given node for this graph's edges
     *
     * @return The set of nodes reachable from outgoing edges from the given nodes
     * @param origin the origin element
     */
    public NodeSet successors(NodeSet origin);

    /**
     * From this graph, selects the subgraph reachable from the given nodes along a
     * path length of 1 in the forward direction.
     *
     * The final result includes the given nodes, the traversed edges, and the
     * reachable nodes.
     * 
     * @param origin the origin element
     */
    public Graph forwardStep(Node origin);

    /**
     * From this graph, selects the subgraph reachable from the given nodes along a
     * path length of 1 in the forward direction.
     *
     * The final result includes the given nodes, the traversed edges, and the
     * reachable nodes.
     * 
     * @param origin the origin element
     */
    public Graph forwardStep(Graph origin);

    /**
     * From this graph, selects the subgraph reachable from the given nodes along a
     * path length of 1 in the forward direction.
     *
     * The final result includes the given nodes, the traversed edges, and the
     * reachable nodes.
     * 
     * @param origin the origin element
     */
    public Graph forwardStep(NodeSet origin);

    /**
     * From this graph, selects the subgraph reachable from the given nodes along a
     * path length of 1 in the reverse direction.
     *
     * The final result includes the given nodes, the traversed edges, and the
     * reachable nodes.
     * 
     * @param origin the origin element
     */
    public Graph reverseStep(Node origin);

    /**
     * From this graph, selects the subgraph reachable from the given nodes along a
     * path length of 1 in the reverse direction.
     *
     * The final result includes the given nodes, the traversed edges, and the
     * reachable nodes.
     * 
     * @param origin the origin element
     */
    public Graph reverseStep(Graph origin);

    /**
     * From this graph, selects the subgraph reachable from the given nodes along a
     * path length of 1 in the reverse direction.
     *
     * The final result includes the given nodes, the traversed edges, and the
     * reachable nodes.
     * 
     * @param origin the origin element
     */
    public Graph reverseStep(NodeSet origin);

    /**
     * Yields the union of this graph and a new graph formed by the given nodes.
     * 
     * @param node the node
     */
    public Graph union(Node node);

    /**
     * Yields the union of this graph and a new graph formed by the given edges.
     * 
     * @param edge the edge
     */
    public Graph union(Edge edge);

    /**
     * Yields the union of this graph and the given graph. That is, the resulting
     * graph's nodes are the union of all nodes, and likewise for edges.
     * 
     * @param graph the graphs
     */
    public Graph union(Graph graph);

    /**
     * Select this graph, excluding the given nodes. Note that, because an edge is
     * only in a graph if it's nodes are in a graph, removing a node will
     * necessarily remove the edges it connects as well.
     * 
     * @param node the node
     */
    public Graph difference(Node node);

    /**
     * Select this graph, excluding the given edges. Note that, because an edge is
     * only in a graph if it's nodes are in a graph, removing an edge will
     * necessarily remove the nodes it connects as well. Removing either node would
     * remove the edge as well.
     *
     * This behavior may seem counter-intuitive if one is thinking in terms of
     * removing a single edge from a graph. Consider the graphs: - g1: a -> b -> c -
     * g2: a -> b g1.difference(g2) yields the graph containing only node c: because
     * b is removed, so b -> c is also removed. In general, this operation is useful
     * for removing nodes from a graph, but may not be as useful for operating on
     * edges.
     * 
     * @param edge the edge
     */
    public Graph difference(Edge edge);

    /**
     * Select this graph, excluding the graphs g. Note that, because an edge is only
     * in a graph if it's nodes are in a graph, removing an edge will necessarily
     * remove the nodes it connects as well. Removing either node would remove the
     * edge as well.
     *
     * This behavior may seem counter-intuitive if one is thinking in terms of
     * removing a single edge from a graph. Consider the graphs: - g1: a -> b -> c -
     * g2: a -> b g1.difference(g2) yields the graph containing only node c: because
     * b is removed, so b -> c is also removed. In general, this operation is useful
     * for removing nodes from a graph, but may not be as useful for operating on
     * edges.
     *
     * @param graph the graphs
     */
    public Graph difference(Graph graph);

    /**
     * Select this graph, excluding the given edges.
     * 
     * @param edge the edge
     */
    public Graph differenceEdges(Edge edge);

    /**
     * Select this graph, excluding the edges from the given graphs.
     * 
     * @param graph the graphs
     */
    public Graph differenceEdges(Graph graph);

    /**
     * Yields the intersection of this graph and a new graph formed by the given
     * nodes. That is, the resulting graph's nodes are the intersection of all node
     * sets, and likewise for edges.
     * 
     * @param node the node
     */
    public Graph intersection(Node node);

    /**
     * Yields the intersection of this graph and a new graph formed by the given
     * edges. That is, the resulting graph's nodes are the intersection of all node
     * sets, and likewise for edges.
     * 
     * @param edge the edge
     */
    public Graph intersection(Edge edge);

    /**
     * Yields the intersection of this graph and the given graph. That is, the
     * resulting graph's nodes are the intersection of all node sets, and likewise
     * for edges.
     * 
     * @param graph the graphs
     */
    public Graph intersection(Graph graph);

    /**
     * From this graph, selects the subgraph such that the given nodes in to are
     * reachable from the nodes in from in a single step
     * 
     * @param from the source element
     * @param to   the target element
     */
    public Graph betweenStep(Node from, Node to);

    /**
     * From this graph, selects the subgraph such that the given nodes in to are
     * reachable from the nodes in from in a single step
     * 
     * @param from the source element
     * @param to   the target element
     */
    public Graph betweenStep(Graph from, Graph to);

    /**
     * From this graph, selects the subgraph such that the given nodes in to are
     * reachable from the nodes in from in a single step
     * 
     * @param from the source element
     * @param to   the target element
     */
    public Graph betweenStep(NodeSet from, NodeSet to);

    /**
     * From this graph, selects the subgraph such that the given nodes in to are
     * reachable from the nodes in from using forward traversal.
     * 
     * Logically equivalent to graph.forward(from).intersection(graph.reverse(to)) .
     * 
     * @param from the source element
     * @param to   the target element
     */
    public Graph between(Node from, Node to);

    /**
     * From this graph, selects the subgraph such that the given nodes in to are
     * reachable from the nodes in from using forward traversal.
     * 
     * Logically equivalent to graph.forward(from).intersection(graph.reverse(to)) .
     * 
     * @param from the source element
     * @param to   the target element
     */
    public Graph between(Graph from, Graph to);

    /**
     * From this graph, selects the subgraph such that the given nodes in to are
     * reachable from the nodes in from using forward traversal.
     * 
     * Logically equivalent to graph.forward(from).intersection(graph.reverse(to)) .
     * 
     * @param from the source element
     * @param to   the target element
     */
    public Graph between(NodeSet from, NodeSet to);

    /**
     * From this graph, selects the subgraph reachable from the given nodes using
     * forward transitive traversal.
     * 
     * @param origin the origin element
     */
    public Graph forward(Node origin);

    /**
     * From this graph, selects the subgraph reachable from the given nodes using
     * forward transitive traversal.
     * 
     * @param origin the origin element
     */
    public Graph forward(Graph origin);

    /**
     * From this graph, selects the subgraph reachable from the given nodes using
     * forward transitive traversal.
     * 
     * @param origin the origin element
     */
    public Graph forward(NodeSet origin);

    /**
     * From this graph, selects the subgraph reachable from the given nodes using
     * reverse transitive traversal.
     * 
     * @param origin the origin element
     */
    public Graph reverse(Node origin);

    /**
     * From this graph, selects the subgraph reachable from the given nodes using
     * reverse transitive traversal.
     * 
     * @param origin the origin element
     */
    public Graph reverse(Graph origin);

    /**
     * From this graph, selects the subgraph reachable from the given nodes using
     * reverse transitive traversal.
     * 
     * @param origin the origin element
     */
    public Graph reverse(NodeSet origin);

    /**
     * Yields the induced graph formed from the nodes in the current graph and all
     * of the edges in the given graph that connect pairs of nodes in the current
     * graph.
     * 
     * @param edge the edge
     */
    public Graph induce(Edge edge);

    /**
     * Yields the induced graph formed from the nodes in the current graph and all
     * of the edges in the given graph that connect pairs of nodes in the current
     * graph.
     * 
     * @param graph the graphs
     */
    public Graph induce(Graph graph);

    /**
     * Yields the induced graph formed from the nodes in the current graph and all
     * of the edges in the given graph that connect pairs of nodes in the current
     * graph.
     * 
     * @param edges the edges
     */
    public Graph induce(EdgeSet edges);

    /**
     * Returns true if there is at least one edge originating at the source and
     * terminating at the target.
     * 
     * @param source the source node
     * @param target the target node
     */
    public boolean adjacent(Node source, Node target);

    /**
     * Returns the set of edges that originate at the source and terminate at the
     * target.
     * 
     * @param source the source node
     * @param target the target node
     */
    public EdgeSet edges(Node source, Node target);

    /**
     * Returns the number of edges connected to this node in the specified
     * direction.
     * 
     * @param node      the node
     * @param direction the direction
     */
    public int degree(Node node, NodeDirection direction);

    /*
     * NOTE: Graph-Level Attributes Graph-level attributes are temporarily deferred
     * to prevent the graph from becoming a global state bucket. This API will be
     * activated strictly when required for universal topological contexts.
     *
     * public AttributeMap attributes();
     */

}
