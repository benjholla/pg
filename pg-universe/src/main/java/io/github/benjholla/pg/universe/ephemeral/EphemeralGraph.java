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

	private Map<Integer, EphemeralNode> nodes;
	private Map<Integer, EphemeralEdge> edges;
	private Map<Integer, EphemeralEdgeSet> inEdgesMap;
	private Map<Integer, EphemeralEdgeSet> outEdgesMap;

	/**
	 * Constructs a new empty graph
	 */
	public EphemeralGraph() {
		this.nodes = new HashMap<>();
		this.inEdgesMap = new HashMap<>();
		this.outEdgesMap = new HashMap<>();
		this.edges = new HashMap<>();
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
		return Optional.ofNullable(inEdgesMap.get(node.id()));
	}

	/**
	 * Gets out-coming edges from node
	 * @return The set of out-coming edges from the given node
	 */
	protected Optional<EdgeSet> getOutEdgesFromNode(Node node){
		return Optional.ofNullable(outEdgesMap.get(node.id()));
	}

	@Override
	public boolean add(GraphElement graphElement) {
		Objects.requireNonNull(graphElement, "graphElement cannot be null");
		boolean result = false;
		if(graphElement instanceof EphemeralNode) {
			EphemeralNode node = (EphemeralNode) graphElement;
			if(!nodes.containsKey(node.id())) {
				nodes.put(node.id(), node);
				result = true;
			}
		} else if(graphElement instanceof EphemeralEdge) {
			EphemeralEdge edge = (EphemeralEdge) graphElement;
			EphemeralNode from = (EphemeralNode) edge.from();
			EphemeralNode to = (EphemeralNode) edge.to();
			if(!nodes.containsKey(from.id())) { nodes.put(from.id(), from); result = true; }
			if(!nodes.containsKey(to.id())) { nodes.put(to.id(), to); result = true; }
			if(!edges.containsKey(edge.id())) {
				edges.put(edge.id(), edge);
				inEdgesMap.computeIfAbsent(to.id(), k -> new EphemeralEdgeSet()).add(edge);
				outEdgesMap.computeIfAbsent(from.id(), k -> new EphemeralEdgeSet()).add(edge);
				result = true;
			}
		}
		return result;
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
	public boolean addAll(Iterable<? extends GraphElement> graphElements) {
		throw new UnsupportedOperationException("TODO: implement");
	}

	@Override
	public boolean remove(GraphElement graphElement) {
		Objects.requireNonNull(graphElement, "graphElement cannot be null");
		if(graphElement instanceof EphemeralEdge) {
			EphemeralEdge edge = (EphemeralEdge) graphElement;
			EphemeralEdge removed = edges.remove(edge.id());
			if(removed != null) {
				EphemeralEdgeSet inSet = inEdgesMap.get(edge.to().id());
				if(inSet != null) inSet.remove(edge);
				EphemeralEdgeSet outSet = outEdgesMap.get(edge.from().id());
				if(outSet != null) outSet.remove(edge);
				return true;
			}
		} else if (graphElement instanceof EphemeralNode) {
			EphemeralNode node = (EphemeralNode) graphElement;
			EphemeralNode removed = nodes.remove(node.id());
			if(removed != null) {
				EphemeralEdgeSet inSet = inEdgesMap.remove(node.id());
				if(inSet != null) {
					for(Edge e : inSet) {
						edges.remove(e.id());
						EphemeralEdgeSet outSet = outEdgesMap.get(e.from().id());
						if(outSet != null) outSet.remove(e);
					}
				}
				EphemeralEdgeSet outSet = outEdgesMap.remove(node.id());
				if(outSet != null) {
					for(Edge e : outSet) {
						edges.remove(e.id());
						EphemeralEdgeSet inSet2 = inEdgesMap.get(e.to().id());
						if(inSet2 != null) inSet2.remove(e);
					}
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public NodeSet nodes() {
		return new UnmodifiableEphemeralNodeSet(nodes.values());
	}

	@Override
	public EdgeSet edges() {
		return new UnmodifiableEphemeralEdgeSet(edges.values());
	}

	@Override
	public EdgeSet edges(Node node, NodeDirection direction){
		throw new UnsupportedOperationException("TODO: implement");
	}

	@Override
	public NodeSet limit(NodeDirection direction){
		throw new UnsupportedOperationException("TODO: implement");
	}

	@Override
	public NodeSet leaves(){
		throw new UnsupportedOperationException("TODO: implement");
	}

	@Override
	public NodeSet roots(){
		throw new UnsupportedOperationException("TODO: implement");
	}

	@Override
	public NodeSet predecessors(Node... origin){
		throw new UnsupportedOperationException("TODO: implement");
	}

	@Override
	public NodeSet predecessors(Graph origin){
		throw new UnsupportedOperationException("TODO: implement");
	}

	@Override
	public NodeSet predecessors(NodeSet origin){
		throw new UnsupportedOperationException("TODO: implement");
	}

	@Override
	public NodeSet successors(Node... origin){
		throw new UnsupportedOperationException("TODO: implement");
	}

	@Override
	public NodeSet successors(Graph origin){
		throw new UnsupportedOperationException("TODO: implement");
	}

	@Override
	public NodeSet successors(NodeSet origin){
		throw new UnsupportedOperationException("TODO: implement");
	}

	@Override
	public Graph forwardStep(Node... origin){
		throw new UnsupportedOperationException("TODO: implement");
	}

	@Override
	public Graph forwardStep(Graph origin){
		throw new UnsupportedOperationException("TODO: implement");
	}

	@Override
	public Graph forwardStep(NodeSet origin){
		throw new UnsupportedOperationException("TODO: implement");
	}

	@Override
	public Graph reverseStep(Node... origin){
		throw new UnsupportedOperationException("TODO: implement");
	}

	@Override
	public Graph reverseStep(Graph origin){
		throw new UnsupportedOperationException("TODO: implement");
	}

	@Override
	public Graph reverseStep(NodeSet origin){
		throw new UnsupportedOperationException("TODO: implement");
	}

	@Override
	public Graph union(Node... nodes){
		throw new UnsupportedOperationException("TODO: implement");
	}

	@Override
	public Graph union(Edge... edges){
		throw new UnsupportedOperationException("TODO: implement");
	}

	@Override
	public Graph union(Graph... graphs){
		throw new UnsupportedOperationException("TODO: implement");
	}

	@Override
	public Graph difference(Node... nodes){
		throw new UnsupportedOperationException("TODO: implement");
	}

	@Override
	public Graph difference(Edge... edges){
		throw new UnsupportedOperationException("TODO: implement");
	}

	@Override
	public Graph difference(Graph... graphs){
		throw new UnsupportedOperationException("TODO: implement");
	}

	@Override
	public Graph differenceEdges(Edge... edges){
		throw new UnsupportedOperationException("TODO: implement");
	}

	@Override
	public Graph differenceEdges(Graph... graphs){
		throw new UnsupportedOperationException("TODO: implement");
	}

	@Override
	public Graph intersection(Node... nodes){
		throw new UnsupportedOperationException("TODO: implement");
	}

	@Override
	public Graph intersection(Edge... edges){
		throw new UnsupportedOperationException("TODO: implement");
	}

	@Override
	public Graph intersection(Graph... graphs){
		throw new UnsupportedOperationException("TODO: implement");
	}

	@Override
	public Graph betweenStep(Node from, Node to){
		throw new UnsupportedOperationException("TODO: implement");
	}

	@Override
	public Graph betweenStep(Graph from, Graph to){
		throw new UnsupportedOperationException("TODO: implement");
	}

	@Override
	public Graph betweenStep(NodeSet from, NodeSet to){
		throw new UnsupportedOperationException("TODO: implement");
	}

	@Override
	public Graph between(Node from, Node to) {
		throw new UnsupportedOperationException("TODO: implement");
	}

	@Override
	public Graph between(Graph from, Graph to) {
		throw new UnsupportedOperationException("TODO: implement");
	}

	@Override
	public Graph between(NodeSet from, NodeSet to) {
		throw new UnsupportedOperationException("TODO: implement");
	}

	@Override
	public Graph forward(Node... origin){
		throw new UnsupportedOperationException("TODO: implement");
	}

	@Override
	public Graph forward(Graph origin){
		throw new UnsupportedOperationException("TODO: implement");
	}

	@Override
	public Graph forward(NodeSet origin){
		throw new UnsupportedOperationException("TODO: implement");
	}

	@Override
	public Graph reverse(Node... origin){
		throw new UnsupportedOperationException("TODO: implement");
	}

	@Override
	public Graph reverse(Graph origin){
		throw new UnsupportedOperationException("TODO: implement");
	}

	@Override
	public Graph reverse(NodeSet origin){
		throw new UnsupportedOperationException("TODO: implement");
	}

	@Override
	public Graph induce(Edge... edges){
		throw new UnsupportedOperationException("TODO: implement");
	}

	@Override
	public Graph induce(Graph... graphs){
		throw new UnsupportedOperationException("TODO: implement");
	}

	@Override
	public Graph induce(EdgeSet edges){
		throw new UnsupportedOperationException("TODO: implement");
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
	public boolean isEmpty() {
		return nodes.isEmpty();
	}
}
