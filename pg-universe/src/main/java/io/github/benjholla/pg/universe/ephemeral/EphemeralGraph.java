package io.github.benjholla.pg.universe.ephemeral;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import io.github.benjholla.pg.api.AttributeValue;
import io.github.benjholla.pg.api.Edge;
import io.github.benjholla.pg.api.EdgeSet;
import io.github.benjholla.pg.api.Graph;
import io.github.benjholla.pg.api.Node;
import io.github.benjholla.pg.api.Node.NodeDirection;
import io.github.benjholla.pg.api.NodeSet;

/**
 * EphemeralGraph provides the core storage, adjacency maps, and graph operations.
 */
public class EphemeralGraph implements Graph {

	private static final EdgeSet EMPTY_EDGES = new EphemeralImmutableEdgeSet(new EphemeralEdgeSet());

	private Map<Integer, EphemeralNode> nodes;
	private Map<Integer, EphemeralEdge> edges;
	private Map<Integer, EphemeralEdgeSet> inEdges;
	private Map<Integer, EphemeralEdgeSet> outEdges;

	/**
	 * Constructs a new empty graph
	 */
    @Override
    public Graph createGraph() {
        return new EphemeralGraph();
    }

    @Override
    public Graph createGraph(Node... nodes) {
        return new EphemeralGraph(nodes);
    }

    @Override
    public Graph createGraph(NodeSet nodes) {
        return new EphemeralGraph(nodes);
    }

    @Override
    public Graph createGraph(Edge... edges) {
        return new EphemeralGraph(edges);
    }

    @Override
    public Graph createGraph(EdgeSet edges) {
        return new EphemeralGraph(edges);
    }

    @Override
    public Graph createGraph(NodeSet nodes, EdgeSet edges) {
        return new EphemeralGraph(nodes, edges);
    }

    @Override
    public Graph createGraph(Graph... graphs) {
        return new EphemeralGraph(graphs);
    }

    @Override
    public Graph createGraph(Collection<Graph> graphs) {
        return new EphemeralGraph(graphs);
    }

	public EphemeralGraph() {
		this.nodes = new HashMap<>();
		this.edges = new HashMap<>();
		this.inEdges = new HashMap<>();
		this.outEdges = new HashMap<>();
	}

	/**
     * Constructs a new graph of the given nodes
     */
	public EphemeralGraph(Node... nodes) {
		this();
		Objects.requireNonNull(nodes, "nodes cannot be null");
		for (Node n : nodes) Objects.requireNonNull(n, "nodes elements cannot be null");
        for(Node node : nodes) {
            addNode(node);
        }
    }

    /**
     * Constructs a new graph of the given nodes
     */
	public EphemeralGraph(NodeSet nodes) {
		this();
		Objects.requireNonNull(nodes, "nodes cannot be null");
        addAllNodes(nodes);
    }

	/**
     * Constructs a new graph of the given edges and respective edge nodes
     */
	public EphemeralGraph(Edge... edges) {
		this();
		Objects.requireNonNull(edges, "edges cannot be null");
		for (Edge e : edges) Objects.requireNonNull(e, "edges elements cannot be null");
        for(Edge edge : edges) {
            addEdge(edge);
        }
    }

	/**
     * Constructs a new graph of the given edges and respective edge nodes
     */
	public EphemeralGraph(EdgeSet edges) {
		this();
		Objects.requireNonNull(edges, "edges cannot be null");
        for(Edge edge : edges) {
            addEdge(edge);
        }
    }

    /**
     * Constructs a new graph of the given edges and respective edge nodes
     */
	public EphemeralGraph(NodeSet nodes, EdgeSet edges) {
		this();
		Objects.requireNonNull(nodes, "nodes cannot be null");
		Objects.requireNonNull(edges, "edges cannot be null");
        addAllNodes(nodes);
        addAllEdges(edges);
    }

	/**
     * Constructs a new graph of the nodes and edges collectively contained in the given graphs
     */
	public EphemeralGraph(Graph... graphs) {
		this();
		Objects.requireNonNull(graphs, "graphs cannot be null");
		for (Graph g : graphs) Objects.requireNonNull(g, "graphs elements cannot be null");
        for(Graph graph : graphs) {
            addAllNodes(graph.nodes());
            addAllEdges(graph.edges());
        }
    }

	/**
     * Constructs a new graph of the nodes and edges collectively contained in the given graphs
     */
	public EphemeralGraph(Collection<Graph> graphs) {
		this();
		Objects.requireNonNull(graphs, "graphs cannot be null");
		for (Graph g : graphs) Objects.requireNonNull(g, "graphs elements cannot be null");
        for(Graph graph : graphs) {
            addAllNodes(graph.nodes());
            addAllEdges(graph.edges());
        }
    }


	/**
	 * Gets incoming edges to node
	 * @return The set of incoming edges to the given node
	 */
	protected Optional<EdgeSet> getInEdgesToNode(Node node){
		if (!(node instanceof EphemeralNode en)) return Optional.empty();
		return Optional.ofNullable(inEdges.get(en.id()));
	}

	/**
	 * Gets out-coming edges from node
	 * @return The set of out-coming edges from the given node
	 */
	protected Optional<EdgeSet> getOutEdgesFromNode(Node node){
		if (!(node instanceof EphemeralNode en)) return Optional.empty();
		return Optional.ofNullable(outEdges.get(en.id()));
	}


	@Override
	public boolean containsNode(Node node) {
		// Silent Ignore
		if (!(node instanceof EphemeralNode en)) return false;
		return nodes.containsKey(en.id());
	}

	@Override
	public boolean containsEdge(Edge edge) {
		// Silent Ignore
		if (!(edge instanceof EphemeralEdge ee)) return false;
		return edges.containsKey(ee.id());
	}

	@Override
	public boolean containsAllNodes(Collection<? extends Node> nodes) {
		Objects.requireNonNull(nodes, "nodes cannot be null");
		for (Node node : nodes) {
			Objects.requireNonNull(node, "node set elements cannot be null");
			if (!containsNode(node)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean containsAllEdges(Collection<? extends Edge> edges) {
		Objects.requireNonNull(edges, "edges cannot be null");
		for (Edge edge : edges) {
			Objects.requireNonNull(edge, "edge set elements cannot be null");
			if (!containsEdge(edge)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public Optional<Node> node(int id) {
		return Optional.ofNullable(nodes.get(id));
	}

	@Override
	public Optional<Edge> edge(int id) {
		return Optional.ofNullable(edges.get(id));
	}


	@Override
	public boolean addNode(Node node) {
		Objects.requireNonNull(node, "node cannot be null");
        // 1. Violent Fail boundary
        if (!(node instanceof EphemeralNode en)) {
            throw new IllegalArgumentException("Expected EphemeralNode in EphemeralGraph.");
        }

        // 2. Idempotent check (don't overwrite existing wiring)
        if (nodes.containsKey(en.id())) {
            return false;
        }

        // 3. Synchronize the 4 Pillars
        nodes.put(en.id(), en);
        outEdges.put(en.id(), new EphemeralEdgeSet());
        inEdges.put(en.id(), new EphemeralEdgeSet());
        return true;
	}
	
	@Override
    public boolean addEdge(Edge edge) {
        Objects.requireNonNull(edge, "edge cannot be null");
        // Auto-vivify terminal nodes
        boolean result = false;
        result |= addNode(edge.from());
        result |= addNode(edge.to());
        result |= linkEdge(edge);
        return result;
    }

	@Override
	public boolean linkAllEdges(Collection<? extends Edge> edges) {
		Objects.requireNonNull(edges, "edges cannot be null");
		boolean result = false;
		for (Edge edge : edges) {
			Objects.requireNonNull(edge, "edge set elements cannot be null");
		}
		for (Edge edge : edges) {
			result |= linkEdge(edge);
		}
		return result;
	}

	@Override
    public boolean linkEdge(Edge edge) {
        Objects.requireNonNull(edge, "edge cannot be null");
        // 1. Violent Fail boundary
        if (!(edge instanceof EphemeralEdge ee)) {
            throw new IllegalArgumentException("Expected EphemeralEdge in EphemeralGraph.");
        }

        // 2. Validate Topology Anchors
        // The graph MUST violently fail if the nodes aren't registered,
        // otherwise the adjacency maps will throw NullPointerExceptions.
        if (!nodes.containsKey(ee.from().id()) || !nodes.containsKey(ee.to().id())) {
            throw new IllegalArgumentException("Source or target node is not present in the graph.");
        }

        // 3. Idempotent check
        if (edges.putIfAbsent(ee.id(), ee) != null) {
            return false; // Edge already existed
        }

        // 4. Wire the adjacency maps
        outEdges.get(ee.from().id()).add(ee);
        inEdges.get(ee.to().id()).add(ee);
        return true;
    }

	@Override
	public boolean addAllNodes(Collection<? extends Node> nodes) {
		Objects.requireNonNull(nodes, "nodes cannot be null");
		boolean result = false;
		for(Node node : nodes) {
			Objects.requireNonNull(node, "node set elements cannot be null");
			result |= addNode(node);
		}
		return result;
	}
	
	@Override
    public boolean addAllEdges(Collection<? extends Edge> edges) {
        Objects.requireNonNull(nodes, "edges cannot be null");
        boolean result = false;
        for(Edge edge : edges) {
            Objects.requireNonNull(edge, "edge set elements cannot be null");
            result |= addEdge(edge);
        }
        return result;
    }

	@Override
	public boolean removeNode(Node node) {
        // 1. Silent Ignore
        if (!(node instanceof EphemeralNode en) || !nodes.containsKey(en.id())) {
            return false;
        }

        int targetId = en.id();

        // 2. Cascading Teardown: Scrub Outbound Edges
        for (Edge out : outEdges.get(targetId)) {
            edges.remove(out.id()); // Remove from global registry
            inEdges.get(out.to().id()).remove(out); // Disconnect from neighbor
        }

        // 3. Cascading Teardown: Scrub Inbound Edges
        for (Edge in : inEdges.get(targetId)) {
            edges.remove(in.id()); // Remove from global registry
            outEdges.get(in.from().id()).remove(in); // Disconnect from neighbor
        }

        // 4. Collapse the pillars for this ID
        outEdges.remove(targetId);
        inEdges.remove(targetId);
        nodes.remove(targetId);
        return true;
	}
	
	@Override
    public boolean removeEdge(Edge edge) {
        // 1. Silent Ignore
        if (!(edge instanceof EphemeralEdge ee) || !edges.containsKey(ee.id())) {
            return false;
        }

        // 2. Teardown
        edges.remove(ee.id());
        outEdges.get(ee.from().id()).remove(ee);
        inEdges.get(ee.to().id()).remove(ee);
        return true;
    }
	
	@Override
    public boolean removeAllNodes(Collection<? extends Node> nodes) {
        Objects.requireNonNull(nodes, "nodes cannot be null");
        boolean result = false;
        for(Node node : nodes) {
            Objects.requireNonNull(node, "node set elements cannot be null");
        }
        for(Node node : nodes) {
            result |= removeNode(node);
        }
        return result;
    }

    @Override
    public boolean removeAllEdges(Collection<? extends Edge> edges) {
        Objects.requireNonNull(edges, "edges cannot be null");
        boolean result = false;
        for(Edge edge : edges) {
            Objects.requireNonNull(edge, "edge set elements cannot be null");
        }
        for(Edge edge : edges) {
            result |= removeEdge(edge);
        }
        return result;
    }

    @Override
    public boolean retainAllNodes(Collection<? extends Node> nodesToKeep) {
        Objects.requireNonNull(nodesToKeep, "nodes cannot be null");
        for(Node node : nodesToKeep) {
            Objects.requireNonNull(node, "node set elements cannot be null");
        }
        boolean result = false;
        // Collect nodes to remove to avoid concurrent modification
        Collection<Node> toRemove = new ArrayList<>();
        for (Node node : this.nodes.values()) {
            if (!nodesToKeep.contains(node)) {
                toRemove.add(node);
            }
        }
        for (Node node : toRemove) {
            result |= removeNode(node);
        }
        return result;
    }

    @Override
    public boolean retainAllEdges(Collection<? extends Edge> edgesToKeep) {
        Objects.requireNonNull(edgesToKeep, "edges cannot be null");
        for(Edge edge : edgesToKeep) {
            Objects.requireNonNull(edge, "edge set elements cannot be null");
        }
        boolean result = false;
        Collection<Edge> toRemove = new ArrayList<>();
        for (Edge edge : this.edges.values()) {
            if (!edgesToKeep.contains(edge)) {
                toRemove.add(edge);
            }
        }
        for (Edge edge : toRemove) {
            result |= removeEdge(edge);
        }
        return result;
    }
    
    public void clearEdges() {
        inEdges.clear();
        outEdges.clear();
        edges.clear();
    }
    
    public void clear() {
        clearEdges();
        nodes.clear();
    }

	@Override
	public NodeSet nodes() {
		return new EphemeralUnmodifiableLiveNodeSet(nodes, edges, inEdges, outEdges);
	}


	@Override
	public EdgeSet edges() {
		return new EphemeralUnmodifiableLiveEdgeSet(nodes, edges, inEdges, outEdges);
	}


	@Override
	public boolean isEmpty() {
		return nodes.isEmpty();
	}

	@Override
	public EdgeSet edges(Node node, NodeDirection direction){
		if(direction == NodeDirection.IN){
			return getInEdgesToNode(node).map(EphemeralEdgeSet::new).orElseGet(EphemeralEdgeSet::new);
		} else {
			return getOutEdgesFromNode(node).map(EphemeralEdgeSet::new).orElseGet(EphemeralEdgeSet::new);
		}
	}

	@Override
	public NodeSet limit(NodeDirection direction){
		java.util.Objects.requireNonNull(direction, "NodeDirection cannot be null");
		NodeSet result = new EphemeralNodeSet();
		for(Node node : nodes()){
			EdgeSet connections = edges(node, direction);
			if(connections.isEmpty()){
				result.add(node);
			}
		}
		return result;
	}

	@Override
	public NodeSet leaves(){
		return limit(NodeDirection.OUT);
	}

	@Override
	public NodeSet roots(){
		return limit(NodeDirection.IN);
	}

	@Override
	public NodeSet predecessors(Node... origin){
		return predecessors(new EphemeralNodeSet(origin));
	}

	@Override
	public NodeSet predecessors(Graph origin){
		return predecessors(origin.nodes());
	}

	@Override
	public NodeSet predecessors(NodeSet origin){
		NodeSet result = new EphemeralNodeSet();
		for(Node node : origin){
			getInEdgesToNode(node).ifPresent(inEdges -> {
				for(Edge edge : inEdges){
					result.add(edge.from());
				}
			});
		}
		return result;
	}

	@Override
	public NodeSet successors(Node... origin){
		return successors(new EphemeralNodeSet(origin));
	}

	@Override
	public NodeSet successors(Graph origin){
		return successors(origin.nodes());
	}

	@Override
	public NodeSet successors(NodeSet origin){
		NodeSet result = new EphemeralNodeSet();
		for(Node node : origin){
			getOutEdgesFromNode(node).ifPresent(outEdges -> {
				for(Edge edge : outEdges){
					result.add(edge.to());
				}
			});
		}
		return result;
	}

	@Override
	public Graph forwardStep(Node... origin){
		Objects.requireNonNull(origin, "origin cannot be null");
		for (Node n : origin) Objects.requireNonNull(n, "origin elements cannot be null");
		return forwardStep(new EphemeralNodeSet(origin));
	}

	@Override
	public Graph forwardStep(Graph origin){
		Objects.requireNonNull(origin, "origin cannot be null");
		Graph result = new EphemeralGraph(origin);
		for(Node node : origin.nodes()){
			getOutEdgesFromNode(node).ifPresent(outEdges -> {
				for(Edge edge : outEdges){
					result.addNode(edge.from());
					result.addNode(edge.to());
					result.addEdge(edge);
				}
			});
		}
		return result;
	}

	@Override
	public Graph forwardStep(NodeSet origin){
		Objects.requireNonNull(origin, "origin cannot be null");
		return forwardStep(new EphemeralGraph(origin));
	}

	@Override
	public Graph reverseStep(Node... origin){
		Objects.requireNonNull(origin, "origin cannot be null");
		for (Node n : origin) Objects.requireNonNull(n, "origin elements cannot be null");
		return reverseStep(new EphemeralNodeSet(origin));
	}

	@Override
	public Graph reverseStep(Graph origin){
		Objects.requireNonNull(origin, "origin cannot be null");
		Graph result = new EphemeralGraph(origin);
		for(Node node : origin.nodes()){
			getInEdgesToNode(node).ifPresent(inEdges -> {
				for(Edge edge : inEdges){
					result.addNode(edge.from());
					result.addNode(edge.to());
					result.addEdge(edge);
				}
			});
		}
		return result;
	}

	@Override
	public Graph reverseStep(NodeSet origin){
		Objects.requireNonNull(origin, "origin cannot be null");
		return reverseStep(new EphemeralGraph(origin));
	}

	@Override
	public Graph union(Node... nodes){
		Objects.requireNonNull(nodes, "nodes cannot be null");
		for (Node n : nodes) Objects.requireNonNull(n, "nodes elements cannot be null");
		return union(new EphemeralGraph(nodes));
	}

	@Override
	public Graph union(Edge... edges){
		Objects.requireNonNull(edges, "edges cannot be null");
		for (Edge e : edges) Objects.requireNonNull(e, "edges elements cannot be null");
		return union(new EphemeralGraph(edges));
	}

	@Override
	public Graph union(Graph... graphs){
		Objects.requireNonNull(graphs, "graphs cannot be null");
		for (Graph g : graphs) Objects.requireNonNull(g, "graphs elements cannot be null");
		// union operations commute, so we order all graphs including this graph
		// by largest to smallest so that we start with the largest set and minimize add operations
		ArrayList<Graph> sortedGraphs = new ArrayList<Graph>(Arrays.asList(graphs));
		sortedGraphs.add(this);
		Collections.sort(sortedGraphs, Graph.SIZE_COMPARATOR.reversed());
		Graph initial = sortedGraphs.remove(0);

		Graph union = new EphemeralGraph(initial.nodes(), initial.edges());
		for(Graph graph : sortedGraphs){
			union.addAllNodes(graph.nodes());
			union.addAllEdges(graph.edges());
		}
		return union;
	}

	@Override
	public Graph difference(Node... nodes){
		Objects.requireNonNull(nodes, "nodes cannot be null");
		for (Node n : nodes) Objects.requireNonNull(n, "nodes elements cannot be null");
		return difference(new EphemeralGraph(nodes));
	}

	@Override
	public Graph difference(Edge... edges){
		Objects.requireNonNull(edges, "edges cannot be null");
		for (Edge e : edges) Objects.requireNonNull(e, "edges elements cannot be null");
		return difference(new EphemeralGraph(edges));
	}

	@Override
	public Graph difference(Graph... graphs){
		Objects.requireNonNull(graphs, "graphs cannot be null");
		for (Graph g : graphs) Objects.requireNonNull(g, "graphs elements cannot be null");
		// sorting the graphs to difference from this graph by largest to smallest
		// in order to remove the most information up front
		// note that this ordering does not include this graph because difference
		// operations do not commute (the given graphs are effectively a union)
		ArrayList<Graph> sortedGraphs = new ArrayList<Graph>(Arrays.asList(graphs));
		Collections.sort(sortedGraphs, Graph.SIZE_COMPARATOR.reversed());

		Graph difference = new EphemeralGraph(this.nodes(), this.edges());
		for(Graph graph : sortedGraphs){
			if(difference.isEmpty()) {
				break;
			}
			for (Node node : graph.nodes()) {
				difference.removeNode(node);
			}
			difference.removeAllEdges(graph.edges());
		}
		return difference;
	}

	@Override
	public Graph differenceEdges(Edge... edges){
		Objects.requireNonNull(edges, "edges cannot be null");
		for (Edge e : edges) Objects.requireNonNull(e, "edges elements cannot be null");
		return differenceEdges(new EphemeralGraph(edges));
	}

	@Override
	public Graph differenceEdges(Graph... graphs){
		Objects.requireNonNull(graphs, "graphs cannot be null");
		for (Graph g : graphs) Objects.requireNonNull(g, "graphs elements cannot be null");
		// sorting the graphs to difference from this graph by largest to smallest
		// in order to remove the most information up front
		// note that this ordering does not include this graph because difference
		// operations do not commute (the given graphs are effectively a union)
		ArrayList<Graph> sortedGraphs = new ArrayList<Graph>(Arrays.asList(graphs));
		Collections.sort(sortedGraphs, Graph.SIZE_COMPARATOR.reversed());

		Graph difference = new EphemeralGraph(this.nodes(), this.edges());
		for(Graph graph : sortedGraphs){
			if(difference.edges().isEmpty()) {
				break;
			}
			difference.removeAllEdges(graph.edges());
		}
		return difference;
	}

	@Override
	public Graph intersection(Node... nodes){
		Objects.requireNonNull(nodes, "nodes cannot be null");
		for (Node n : nodes) Objects.requireNonNull(n, "nodes elements cannot be null");
		return intersection(new EphemeralGraph(nodes));
	}

	@Override
	public Graph intersection(Edge... edges){
		Objects.requireNonNull(edges, "edges cannot be null");
		for (Edge e : edges) Objects.requireNonNull(e, "edges elements cannot be null");
		return intersection(new EphemeralGraph(edges));
	}

	@Override
	public Graph intersection(Graph... graphs){
		Objects.requireNonNull(graphs, "graphs cannot be null");
		for (Graph g : graphs) Objects.requireNonNull(g, "graphs elements cannot be null");
		// intersections commute, so we order the given graphs including this graph
		// by the smallest to largest graph in order to start with the smallest set
		// and minimize retain operations
		ArrayList<Graph> sortedGraphs = new ArrayList<Graph>(Arrays.asList(graphs));
		sortedGraphs.add(this);
		Collections.sort(sortedGraphs, Graph.SIZE_COMPARATOR);
		Graph initial = sortedGraphs.remove(0);

		Graph intersection = new EphemeralGraph(initial.nodes(), initial.edges());
		for(Graph graph : sortedGraphs){
			if(intersection.isEmpty()) {
				break;
			}
			intersection.retainAllNodes(graph.nodes());
			intersection.retainAllEdges(graph.edges());
		}
		return intersection;
	}

	@Override
	public Graph betweenStep(Node from, Node to){
		Objects.requireNonNull(from, "from cannot be null");
		Objects.requireNonNull(to, "to cannot be null");
		return betweenStep(new EphemeralNodeSet(from), new EphemeralNodeSet(to));
	}

	@Override
	public Graph betweenStep(Graph from, Graph to){
		Objects.requireNonNull(from, "from cannot be null");
		Objects.requireNonNull(to, "to cannot be null");
		return betweenStep(from.nodes(), to.nodes());
	}

	@Override
	public Graph betweenStep(NodeSet from, NodeSet to){
		Objects.requireNonNull(from, "from cannot be null");
		Objects.requireNonNull(to, "to cannot be null");
		if(from.isEmpty() || to.isEmpty()) {
			return new EphemeralGraph();
		}
		Graph forward = forwardStep(from);
		if(forward.isEmpty()) {
			return new EphemeralGraph();
		}
		Graph reverse = reverseStep(to);
		if(reverse.isEmpty()) {
			return new EphemeralGraph();
		}
		return forward.intersection(reverse);
	}

	@Override
	public Graph between(Node from, Node to) {
		Objects.requireNonNull(from, "from cannot be null");
		Objects.requireNonNull(to, "to cannot be null");
		return between(new EphemeralNodeSet(from), new EphemeralNodeSet(to));
	}

	@Override
	public Graph between(Graph from, Graph to) {
		Objects.requireNonNull(from, "from cannot be null");
		Objects.requireNonNull(to, "to cannot be null");
		return between(from.nodes(), to.nodes());
	}

	@Override
	public Graph between(NodeSet from, NodeSet to) {
		Objects.requireNonNull(from, "from cannot be null");
		Objects.requireNonNull(to, "to cannot be null");
		if(from.isEmpty() || to.isEmpty()) {
			return new EphemeralGraph();
		}
		Graph forward = forward(from);
		if(forward.isEmpty()) {
			return new EphemeralGraph();
		}
		Graph reverse = reverse(to);
		if(reverse.isEmpty()) {
			return new EphemeralGraph();
		}
		return forward.intersection(reverse);
	}

	@Override
	public Graph forward(Node... origin){
		Objects.requireNonNull(origin, "origin cannot be null");
		for (Node n : origin) Objects.requireNonNull(n, "origin elements cannot be null");
		return forward(new EphemeralNodeSet(origin));
	}

	@Override
	public Graph forward(Graph origin){
		Objects.requireNonNull(origin, "origin cannot be null");
		Graph result = new EphemeralGraph(origin);
		NodeSet frontier = new EphemeralNodeSet(origin.nodes());
		while(!frontier.isEmpty()){
			Node next = frontier.one().get();
			frontier.remove(next);
			getOutEdgesFromNode(next).ifPresent(outEdges -> {
				for(Edge edge : outEdges){
					if(result.addNode(edge.to())){
						frontier.add(edge.to());
					}
					result.addEdge(edge);
				}
			});
		}
		return result;
	}

	@Override
	public Graph forward(NodeSet origin){
		Objects.requireNonNull(origin, "origin cannot be null");
		return forward(new EphemeralGraph(origin));
	}

	@Override
	public Graph reverse(Node... origin){
		Objects.requireNonNull(origin, "origin cannot be null");
		for (Node n : origin) Objects.requireNonNull(n, "origin elements cannot be null");
		return reverse(new EphemeralNodeSet(origin));
	}

	@Override
	public Graph reverse(Graph origin){
		Objects.requireNonNull(origin, "origin cannot be null");
		Graph result = new EphemeralGraph(origin);
		NodeSet frontier = new EphemeralNodeSet(origin.nodes());
		while(!frontier.isEmpty()){
			Node next = frontier.one().get();
			frontier.remove(next);
			getInEdgesToNode(next).ifPresent(inEdges -> {
				for(Edge edge : inEdges){
					if(result.addNode(edge.from())){
						frontier.add(edge.from());
					}
					result.addEdge(edge);
				}
			});
		}
		return result;
	}

	@Override
	public Graph reverse(NodeSet origin){
		Objects.requireNonNull(origin, "origin cannot be null");
		return reverse(new EphemeralGraph(origin));
	}

	@Override
	public Graph induce(Edge... edges){
		Objects.requireNonNull(edges, "edges cannot be null");
		for (Edge e : edges) Objects.requireNonNull(e, "edges elements cannot be null");
		return induce(new EphemeralEdgeSet(edges));
	}

	@Override
	public Graph induce(Graph... graphs){
		Objects.requireNonNull(graphs, "graphs cannot be null");
		for (Graph g : graphs) Objects.requireNonNull(g, "graphs elements cannot be null");
		EdgeSet inducibleEdges = new EphemeralEdgeSet();
		for(Graph graph : graphs){
			inducibleEdges.addAll(graph.edges());
		}
		return induce(inducibleEdges);
	}

	@Override
	public Graph induce(EdgeSet edges){
		Objects.requireNonNull(edges, "edges cannot be null");
		Graph result = new EphemeralGraph(this);
		for(Edge edge : edges) {
			if(result.nodes().contains(edge.from()) && result.nodes().contains(edge.to())) {
				result.addEdge(edge);
			}
		}
		return result;
	}

	@Override
	public EdgeSet selectEdges(String attribute){
		return edges().filter(attribute);
	}

	@Override
	public EdgeSet selectEdges(String attribute, AttributeValue... values){
		return edges().filter(attribute, values);
	}

	@Override
	public NodeSet selectNodes(String attribute){
		return nodes().filter(attribute);
	}

	@Override
	public NodeSet selectNodes(String attribute, AttributeValue... values){
		return nodes().filter(attribute, values);
	}

	@Override
	public NodeSet nodes(String... tags){
		return nodesTaggedWithAny(tags);
	}

	@Override
	public NodeSet nodesTaggedWithAny(String... tags){
        NodeSet result = new EphemeralNodeSet();
        for(Node node : nodes()){
            for(String tag : tags){
                if(node.tags().contains(tag)){
                    result.add(node);
                    break;
                }
            }
        }
        return result;
	}

	@Override
	public NodeSet nodesTaggedWithAll(String... tags){
        NodeSet result = new EphemeralNodeSet();
        for(Node node : nodes()){
            boolean add = true;
            for(String tag : tags){
                if(!node.tags().contains(tag)){
                    add = false;
                    break;
                }
            }
            if(add){
                result.add(node);
            }
        }
        return result;
	}

	@Override
	public EdgeSet edges(String... tags){
	    return edgesTaggedWithAny(tags);
	}

	@Override
	public EdgeSet edgesTaggedWithAny(String... tags){
        EdgeSet result = new EphemeralEdgeSet();
        for(Edge edge : edges.values()){
            for(String tag : tags){
                if(edge.tags().contains(tag)){
                    result.add(edge);
                    break;
                }
            }
        }
        return result;
	}

	@Override
	public EdgeSet edgesTaggedWithAll(String... tags){
        EdgeSet result = new EphemeralEdgeSet();
        for(Edge edge : edges.values()){
            boolean add = true;
            for(String tag : tags){
                if(!edge.tags().contains(tag)){
                    add = false;
                    break;
                }
            }
            if(add){
                result.add(edge);
            }
        }
        return result;
	}

	@Override
	public Node createNode() {
		return new EphemeralNode();
	}

	@Override
	public Edge createEdge(Node source, Node target) {
		if (!(source instanceof EphemeralNode)) {
			throw new IllegalArgumentException("Source node is not native to EphemeralGraph.");
		}
		if (!(target instanceof EphemeralNode)) {
			throw new IllegalArgumentException("Target node is not native to EphemeralGraph.");
		}
		return new EphemeralEdge(source, target);
	}

	@Override
	public boolean adjacent(Node source, Node target) {
		if (!(source instanceof EphemeralNode eSource) || !nodes.containsKey(eSource.id())) return false;
		if (!(target instanceof EphemeralNode eTarget) || !nodes.containsKey(eTarget.id())) return false;

		Optional<EdgeSet> out = getOutEdgesFromNode(eSource);
		if (out.isEmpty()) return false;

		for (Edge e : out.get()) {
			if (e.to().equals(eTarget)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public EdgeSet edges(Node source, Node target) {
		if (!(source instanceof EphemeralNode eSource) || !nodes.containsKey(eSource.id())) return EMPTY_EDGES;
		if (!(target instanceof EphemeralNode eTarget) || !nodes.containsKey(eTarget.id())) return EMPTY_EDGES;

		Optional<EdgeSet> out = getOutEdgesFromNode(eSource);
		if (out.isEmpty()) return EMPTY_EDGES;

		EdgeSet result = new EphemeralEdgeSet();
		for (Edge e : out.get()) {
			if (e.to().equals(eTarget)) {
				result.add(e);
			}
		}
		return result.isEmpty() ? EMPTY_EDGES : new EphemeralImmutableEdgeSet(result);
	}

	@Override
	public int degree(Node node, NodeDirection direction) {
		if (!(node instanceof EphemeralNode en) || !nodes.containsKey(en.id())) {
			return 0; // Silent Ignore
		}

		return switch (direction) {
			case OUT -> {
				EphemeralEdgeSet out = outEdges.get(en.id());
				yield out == null ? 0 : out.size();
			}
			case IN -> {
				EphemeralEdgeSet in = inEdges.get(en.id());
				yield in == null ? 0 : in.size();
			}
			case BOTH -> {
				EphemeralEdgeSet out = outEdges.get(en.id());
				EphemeralEdgeSet in = inEdges.get(en.id());
				yield (out == null ? 0 : out.size()) + (in == null ? 0 : in.size());
			}
		};
	}

}
