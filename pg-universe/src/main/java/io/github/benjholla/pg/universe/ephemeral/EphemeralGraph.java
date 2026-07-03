package io.github.benjholla.pg.universe.ephemeral;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import io.github.benjholla.pg.api.AttributeValue;
import io.github.benjholla.pg.api.Edge;
import io.github.benjholla.pg.api.EdgeSet;
import io.github.benjholla.pg.api.Graph;
import io.github.benjholla.pg.api.GraphElement;
import io.github.benjholla.pg.api.Node;
import io.github.benjholla.pg.api.Node.NodeDirection;
import io.github.benjholla.pg.api.NodeSet;

/**
 * EphemeralGraph provides the core storage, adjacency maps, and graph operations.
 */
public class EphemeralGraph implements Graph {

	private NodeSet nodes;
	private EdgeSet edges;
	private Map<Node, EdgeSet> inEdgesMap;
	private Map<Node, EdgeSet> outEdgesMap;

	/**
	 * An internal EdgeSet that maintains the inEdgesMap and outEdgesMap.
	 */
	protected class AdjacencyMaintainingEdgeSet extends EphemeralEdgeSet {
        private static final long serialVersionUID = 1L;

        private void addEdgeToMaps(Edge e) {
            EphemeralGuardrails.requireLocalId(e.to().id());
            EphemeralGuardrails.requireLocalId(e.from().id());
			inEdgesMap.computeIfAbsent(e.to(), k -> new EphemeralEdgeSet()).add(e);
			outEdgesMap.computeIfAbsent(e.from(), k -> new EphemeralEdgeSet()).add(e);
		}

		private void removeEdgeFromMaps(Edge e) {
			EdgeSet inSet = inEdgesMap.get(e.to());
			if (inSet != null) {
				inSet.remove(e);
				if (inSet.isEmpty()) inEdgesMap.remove(e.to());
			}
			EdgeSet outSet = outEdgesMap.get(e.from());
			if (outSet != null) {
				outSet.remove(e);
				if (outSet.isEmpty()) outEdgesMap.remove(e.from());
			}
		}

		@Override
		public boolean add(Edge e) {
			boolean added = super.add(e);
			if (added) {
				addEdgeToMaps(e);
			}
			return added;
		}

		@Override
		public boolean remove(Object o) {
			boolean removed = super.remove(o);
			if (removed && o instanceof Edge) {
				removeEdgeFromMaps((Edge) o);
			}
			return removed;
		}

		@Override
		public boolean addAll(Collection<? extends Edge> c) {
			boolean modified = false;
			for (Edge e : c) {
				if (add(e)) modified = true;
			}
			return modified;
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			boolean modified = false;
			for (Object e : c) {
				if (remove(e)) modified = true;
			}
			return modified;
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			boolean modified = false;
			Iterator<Edge> it = super.iterator();
			while (it.hasNext()) {
				Edge e = it.next();
				if (!c.contains(e)) {
					it.remove();
					removeEdgeFromMaps(e);
					modified = true;
				}
			}
			return modified;
		}

		@Override
		public void clear() {
			super.clear();
			inEdgesMap.clear();
			outEdgesMap.clear();
		}

		@Override
		public Iterator<Edge> iterator() {
			Iterator<Edge> superIt = super.iterator();
			return new Iterator<Edge>() {
				private Edge current = null;

				@Override
				public boolean hasNext() {
					return superIt.hasNext();
				}

				@Override
				public Edge next() {
					current = superIt.next();
					return current;
				}

				@Override
				public void remove() {
					superIt.remove();
					if (current != null) {
						removeEdgeFromMaps(current);
					}
				}
			};
		}
	}

	/**
	 * Constructs a new empty graph
	 */
	public EphemeralGraph() {
		this.nodes = new EphemeralNodeSet();
		this.inEdgesMap = new HashMap<>();
		this.outEdgesMap = new HashMap<>();
		this.edges = new AdjacencyMaintainingEdgeSet();
	}

	/**
     * Constructs a new graph of the given nodes
     */
	public EphemeralGraph(Node... nodes) {
		this();
		Objects.requireNonNull(nodes, "nodes cannot be null");
		for (Node n : nodes) Objects.requireNonNull(n, "nodes elements cannot be null");
        for(Node node : nodes) {
            add(node);
        }
    }

    /**
     * Constructs a new graph of the given nodes
     */
	public EphemeralGraph(NodeSet nodes) {
		this();
		Objects.requireNonNull(nodes, "nodes cannot be null");
        addAll(nodes);
    }

	/**
     * Constructs a new graph of the given edges and respective edge nodes
     */
	public EphemeralGraph(Edge... edges) {
		this();
		Objects.requireNonNull(edges, "edges cannot be null");
		for (Edge e : edges) Objects.requireNonNull(e, "edges elements cannot be null");
        for(Edge edge : edges) {
            add(edge);
        }
    }

	/**
     * Constructs a new graph of the given edges and respective edge nodes
     */
	public EphemeralGraph(EdgeSet edges) {
		this();
		Objects.requireNonNull(edges, "edges cannot be null");
        for(Edge edge : edges) {
            add(edge);
        }
    }

    /**
     * Constructs a new graph of the given edges and respective edge nodes
     */
	public EphemeralGraph(NodeSet nodes, EdgeSet edges) {
		this();
		Objects.requireNonNull(nodes, "nodes cannot be null");
		Objects.requireNonNull(edges, "edges cannot be null");
        addAll(nodes);
        addAll(edges);
    }

	/**
     * Constructs a new graph of the nodes and edges collectively contained in the given graphs
     */
	public EphemeralGraph(Graph... graphs) {
		this();
		Objects.requireNonNull(graphs, "graphs cannot be null");
		for (Graph g : graphs) Objects.requireNonNull(g, "graphs elements cannot be null");
        for(Graph graph : graphs) {
            addAll(graph.nodes());
            addAll(graph.edges());
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
            addAll(graph.nodes());
            addAll(graph.edges());
        }
    }


	/**
	 * Gets incoming edges to node
	 * @return The set of incoming edges to the given node
	 */
	protected Optional<EdgeSet> getInEdgesToNode(Node node){
		return Optional.ofNullable(inEdgesMap.get(node));
	}

	/**
	 * Gets out-coming edges from node
	 * @return The set of out-coming edges from the given node
	 */
	protected Optional<EdgeSet> getOutEdgesFromNode(Node node){
		return Optional.ofNullable(outEdgesMap.get(node));
	}

	@Override
	public Optional<GraphElement> getGraphElementById(int id) {
		for(Node node : nodes()) {
			if(node.id() == id) {
			    return Optional.of(node);
			}
		}
		for(Edge edge : edges()) {
			if(edge.id() == id) {
			    return Optional.of(edge);
			}
		}
		return Optional.empty();
	}

	@Override
	public Optional<Node> getNodeById(int id) {
		for(Node node : nodes()) {
			if(node.id() == id) {
				return Optional.of(node);
			}
		}
		return Optional.empty();
	}

	@Override
	public Optional<Edge> getEdgeById(int id) {
		for(Edge edge : edges()) {
			if(edge.id() == id) {
				return Optional.of(edge);
			}
		}
		return Optional.empty();
	}

	@Override
	public boolean add(GraphElement graphElement) {
		Objects.requireNonNull(graphElement, "graphElement cannot be null");
        EphemeralGuardrails.requireLocalId(graphElement.id());
		boolean result = false;
		if(graphElement instanceof Node) {
			Node node = (Node) graphElement;
			result |= this.nodes().add(node);
		} else if(graphElement instanceof Edge) {
			Edge edge = (Edge) graphElement;
			result |= this.edges().add(edge);
			result |= this.nodes().add(edge.from());
			result |= this.nodes().add(edge.to());
		}
		return result;
	}

	@Override
	public boolean addAll(Iterable<? extends GraphElement> graphElements) {
		Objects.requireNonNull(graphElements, "graphElements cannot be null");
		boolean result = false;
		for(GraphElement graphElement : graphElements) {
			Objects.requireNonNull(graphElement, "graphElements elements cannot be null");
			result |= add(graphElement);
		}
		return result;
	}

	@Override
	public boolean remove(GraphElement graphElement) {
		if(graphElement instanceof Edge) {
			Edge edge = (Edge) graphElement;
			return edges().remove(edge);
		} else {
			Node node = (Node) graphElement;
			getInEdgesToNode(node).ifPresent(inEdges -> {
				edges().removeAll(new EphemeralEdgeSet(inEdges));
			});
			getOutEdgesFromNode(node).ifPresent(outEdges -> {
				edges().removeAll(new EphemeralEdgeSet(outEdges));
			});
			return nodes().remove(node);
		}
	}

	@Override
	public NodeSet nodes() {
		return nodes;
	}

	@Override
	public EdgeSet edges() {
		return edges;
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
					result.nodes().add(edge.from());
					result.nodes().add(edge.to());
					result.edges().add(edge);
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
					result.nodes().add(edge.from());
					result.nodes().add(edge.to());
					result.edges().add(edge);
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
		Collections.sort(sortedGraphs, GRAPH_SIZE_COMPARATOR.reversed());
		Graph initial = sortedGraphs.remove(0);

		Graph union = new EphemeralGraph(initial.nodes(), initial.edges());
		for(Graph graph : sortedGraphs){
			union.nodes().addAll(graph.nodes());
			union.edges().addAll(graph.edges());
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
		Collections.sort(sortedGraphs, GRAPH_SIZE_COMPARATOR.reversed());

		Graph difference = new EphemeralGraph(this.nodes(), this.edges());
		for(Graph graph : sortedGraphs){
			if(difference.isEmpty()) {
				break;
			}
			for (Node node : graph.nodes()) {
				difference.remove(node);
			}
			difference.edges().removeAll(graph.edges());
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
		Collections.sort(sortedGraphs, GRAPH_SIZE_COMPARATOR.reversed());

		Graph difference = new EphemeralGraph(this.nodes(), this.edges());
		for(Graph graph : sortedGraphs){
			if(difference.edges().isEmpty()) {
				break;
			}
			difference.edges().removeAll(graph.edges());
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
		Collections.sort(sortedGraphs, GRAPH_SIZE_COMPARATOR);
		Graph initial = sortedGraphs.remove(0);

		Graph intersection = new EphemeralGraph(initial.nodes(), initial.edges());
		for(Graph graph : sortedGraphs){
			if(intersection.isEmpty()) {
				break;
			}
			intersection.nodes().retainAll(graph.nodes());
			intersection.edges().retainAll(graph.edges());
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
					if(result.nodes().add(edge.to())){
						frontier.add(edge.to());
					}
					result.edges().add(edge);
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
					if(result.nodes().add(edge.from())){
						frontier.add(edge.from());
					}
					result.edges().add(edge);
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
				result.edges().add(edge);
			}
		}
		return result;
	}

	@Override
	public EdgeSet selectEdges(String attribute){
		return edges.filter(attribute);
	}

	@Override
	public EdgeSet selectEdges(String attribute, AttributeValue... values){
		return edges.filter(attribute, values);
	}

	@Override
	public NodeSet selectNodes(String attribute){
		return nodes.filter(attribute);
	}

	@Override
	public NodeSet selectNodes(String attribute, AttributeValue... values){
		return nodes.filter(attribute, values);
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
        for(Edge edge : edges){
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
        for(Edge edge : edges){
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

}
