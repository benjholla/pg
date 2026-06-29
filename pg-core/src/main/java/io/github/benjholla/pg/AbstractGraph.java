package io.github.benjholla.pg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import io.github.benjholla.pg.Node.NodeDirection;

public abstract class AbstractGraph implements Graph {

	protected NodeSet nodes;
	protected EdgeSet edges;
	
	/**
	 * Constructs a new empty graph
	 */
	protected AbstractGraph() {
		this.nodes = new NodeSet();
		this.edges = new EdgeSet();
	}
	
	/**
     * Constructs a graph new of the given nodes
     */
	protected AbstractGraph(Node... nodes) {
        for(Node node : nodes) {
            add(node);
        }
    }
    
    /**
     * Constructs a graph new of the given nodes
     */
	protected AbstractGraph(NodeSet nodes) {
        addAll(nodes);
    }
	
	/**
     * Constructs a new graph of the given edges and respective edge nodes
     */
	protected AbstractGraph(Edge... edges) {
        for(Edge edge : edges) {
            add(edge);
        }
    }
	
	/**
     * Constructs a new graph of the given edges and respective edge nodes
     */
	protected AbstractGraph(EdgeSet edges) {
        for(Edge edge : edges) {
            add(edge);
        }
    }
    
    /**
     * Constructs a new graph of the given edges and respective edge nodes
     */
	protected AbstractGraph(NodeSet nodes, EdgeSet edges) {
        addAll(nodes);
        addAll(edges);
    }
    
	/**
     * Constructs a new graph of the nodes and edges collectively contained in the given graphs
     */
	protected AbstractGraph(Graph... graphs) {
        for(Graph graph : graphs) {
            addAll(graph.nodes());
            addAll(graph.edges());
        }
    }
	
	/**
     * Constructs a new graph of the nodes and edges collectively contained in the given graphs
     */
	protected AbstractGraph(Collection<Graph> graphs) {
        for(Graph graph : graphs) {
            addAll(graph.nodes());
            addAll(graph.edges());
        }
    }
    
    /**
     * Creates an empty graph of this graph kind
     * @return
     */
    protected abstract Graph newGraph();
    
    /**
     * Constructs a graph new of the given nodes of this graph kind
     */
    protected abstract Graph newGraph(Node... nodes);
    
    /**
     * Constructs a graph new of the given nodes of this graph kind
     */
    protected abstract Graph newGraph(NodeSet nodes);
    
    /**
     * Constructs a new graph of the given edges and respective edge nodes of this graph kind
     */
    protected abstract Graph newGraph(Edge... edges);
    
    /**
     * Constructs a new graph of the given edges and respective edge nodes of this graph kind
     */
    protected abstract Graph newGraph(EdgeSet edges);
    
    /**
     * Constructs a new graph of the given edges and respective edge nodes of this graph kind
     */
    protected abstract Graph newGraph(NodeSet nodes, EdgeSet edges);
    
    /**
     * Constructs a new graph of the nodes and edges collectively contained in the given graphs of this graph kind
     */
    protected abstract Graph newGraph(Graph... graphs);
    
    /**
     * Constructs a new graph of the nodes and edges collectively contained in the given graphs of this graph kind
     */
    protected abstract Graph newGraph(Collection<Graph> graphs);
	
	/**
	 * Gets incoming edges to node
	 * 
	 * @param node
	 * @return The set of incoming edges to the given node
	 */
	protected EdgeSet getInEdgesToNode(Node node){
		EdgeSet inEdges = new EdgeSet();
		for(Edge edge : edges()){
			if(edge.to().equals(node)){
				inEdges.add(edge);
			}
		}
		return inEdges;
	}
	
	/**
	 * Gets out-coming edges from node
	 * 
	 * @param node
	 * @return The set of out-coming edges from the given node
	 */
	protected EdgeSet getOutEdgesFromNode(Node node){
		EdgeSet outEdges = new EdgeSet();
		for(Edge edge : edges()){
			if(edge.from().equals(node)){
				outEdges.add(edge);
			}
		}
		return outEdges;
	}
	
	@Override
	public Optional<GraphElement> getGraphElementById(ElementId id) {
		for(Node node : nodes()) {
			if(node.getId().equals(id)) {
			    Optional.of(node);
			}
		}
		for(Edge edge : edges()) {
			if(edge.getId().equals(id)) {
			    return Optional.of(edge);
			}
		}
		return Optional.empty();
	}
	
	@Override
	public Optional<Node> getNodeById(ElementId id) {
		for(Node node : nodes()) {
			if(node.getId().equals(id)) {
				return Optional.of(node);
			}
		}
		return Optional.empty();
	}
	
	@Override
	public Optional<Edge> getEdgeById(ElementId id) {
		for(Edge edge : edges()) {
			if(edge.getId().equals(id)) {
				return Optional.of(edge);
			}
		}
		return Optional.empty();
	}
	
	@Override
	public boolean add(GraphElement graphElement) {
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
		boolean result = false;
		for(GraphElement graphElement : graphElements) {
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
			boolean result = false;
			Node node = (Node) graphElement;
			result |= nodes().remove(node);
			Iterator<Edge> edgeIterator = edges().iterator();
			while(edgeIterator.hasNext()) {
				Edge edge = edgeIterator.next();
				if(edge.from().equals(node) || edge.to().equals(node)) {
					edgeIterator.remove();
					result = true;
				}
			}
			return result;
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
		EdgeSet result = new EdgeSet();
		for(Edge edge : edges()){
			if(direction == NodeDirection.IN){
				if(edge.to().equals(node)){
					result.add(edge);
				}
			} else {
				if(edge.from().equals(node)){
					result.add(edge);
				}
			}
		}
		return result;
	}
	
	@Override
	public NodeSet limit(NodeDirection direction){
		NodeSet result = new NodeSet();
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
		return predecessors(new NodeSet(origin));
	}
	
	@Override
	public NodeSet predecessors(Graph origin){
		return predecessors(origin.nodes());
	}
	
	@Override
	public NodeSet predecessors(NodeSet origin){
		NodeSet result = new NodeSet();
		for(Node node : origin){
			EdgeSet inEdges = getInEdgesToNode(node);
			for(Edge edge : inEdges){
				result.add(edge.from());
			}
		}
		return result;
	}
	
	@Override
	public NodeSet successors(Node... origin){
		return successors(new NodeSet(origin));
	}
	
	@Override
	public NodeSet successors(Graph origin){
		return successors(origin.nodes());
	}
	
	@Override
	public NodeSet successors(NodeSet origin){
		NodeSet result = new NodeSet();
		for(Node node : origin){
			EdgeSet outEdges = getOutEdgesFromNode(node);
			for(Edge edge : outEdges){
				result.add(edge.to());
			}
		}
		return result;
	}
	
	@Override
	public Graph forwardStep(Node... origin){
		return forwardStep(new NodeSet(origin));
	}
	
	@Override
	public Graph forwardStep(Graph origin){
		Graph result = newGraph();
		result.addAll(origin.nodes());
		for(Node node : origin.nodes()){
			EdgeSet outEdges = getOutEdgesFromNode(node);
			for(Edge edge : outEdges){
				result.nodes().add(edge.from());
				result.nodes().add(edge.to());
				result.edges().add(edge);
			}
		}
		return result.union(origin);
	}
	
	@Override
	public Graph forwardStep(NodeSet origin){
		return forwardStep(newGraph(origin));
	}
	
	@Override
	public Graph reverseStep(Node... origin){
		return reverseStep(new NodeSet(origin));
	}
	
	@Override
	public Graph reverseStep(Graph origin){
		Graph result = newGraph();
		result.addAll(origin.nodes());
		for(Node node : origin.nodes()){
			EdgeSet inEdges = getInEdgesToNode(node);
			for(Edge edge : inEdges){
				result.nodes().add(edge.from());
				result.nodes().add(edge.to());
				result.edges().add(edge);
			}
		}
		return result.union(origin);
	}
	
	@Override
	public Graph reverseStep(NodeSet origin){
		return reverseStep(newGraph(origin));
	}
	
	@Override
	public Graph union(Node... nodes){
		return union(newGraph(nodes));
	}
	
	@Override
	public Graph union(Edge... edges){
		return union(newGraph(edges));
	}
	
	@Override
	public Graph union(Graph... graphs){
		// union operations commute, so we order all graphs including this graph
		// by largest to smallest so that we start with the largest set and minimize add operations
		ArrayList<Graph> sortedGraphs = new ArrayList<Graph>(Arrays.asList(graphs));
		sortedGraphs.add(this);
		Collections.sort(sortedGraphs, GRAPH_SIZE_COMPARATOR.reversed());
		Graph initial = sortedGraphs.remove(0);
		
		Graph union = newGraph(initial.nodes(), initial.edges());
		for(Graph graph : sortedGraphs){
			union.nodes().addAll(graph.nodes());
			union.edges().addAll(graph.edges());
		}
		return union;
	}
	
	@Override
	public Graph difference(Node... nodes){
		return difference(newGraph(nodes));
	}
	
	@Override
	public Graph difference(Edge... edges){
		return difference(newGraph(edges));
	}
	
	@Override
	public Graph difference(Graph... graphs){
		// sorting the graphs to difference from this graph by largest to smallest
		// in order to remove the most information up front
		// note that this ordering does not include this graph because difference
		// operations do not commute (the given graphs are effectively a union)
		ArrayList<Graph> sortedGraphs = new ArrayList<Graph>(Arrays.asList(graphs));
		Collections.sort(sortedGraphs, GRAPH_SIZE_COMPARATOR.reversed());
		
		Graph difference = newGraph(this.nodes(), this.edges());
		for(Graph graph : sortedGraphs){
			if(difference.isEmpty()) {
				break;
			}
			// traverse the current result graph to discover the edges that should be removed as
			// a result of removing nodes in the given graph
			EdgeSet incomingEdges = difference.reverseStep(graph.nodes()).edges();
			EdgeSet outgoingEdges = difference.forwardStep(graph.nodes()).edges();
			
			// remove nodes from given graph
			difference.nodes().removeAll(graph.nodes());
			
			// remove edges from given graph, including edges incoming and outgoing from removed nodes
			difference.edges().removeAll(graph.edges());
			difference.edges().removeAll(incomingEdges);
			difference.edges().removeAll(outgoingEdges);
		}
		return difference;
	}
	
	@Override
	public Graph differenceEdges(Edge... edges){
		return differenceEdges(newGraph(edges));
	}
	
	@Override
	public Graph differenceEdges(Graph... graphs){
		// sorting the graphs to difference from this graph by largest to smallest
		// in order to remove the most information up front
		// note that this ordering does not include this graph because difference
		// operations do not commute (the given graphs are effectively a union)
		ArrayList<Graph> sortedGraphs = new ArrayList<Graph>(Arrays.asList(graphs));
		Collections.sort(sortedGraphs, GRAPH_SIZE_COMPARATOR.reversed());
		
		Graph difference = newGraph(this.nodes(), this.edges());
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
		return intersection(newGraph(nodes));
	}
	
	@Override
	public Graph intersection(Edge... edges){
		return intersection(newGraph(edges));
	}
	
	@Override
	public Graph intersection(Graph... graphs){
		// intersections commute, so we order the given graphs including this graph 
		// by the smallest to largest graph in order to start with the smallest set
		// and minimize retain operations
		ArrayList<Graph> sortedGraphs = new ArrayList<Graph>(Arrays.asList(graphs));
		sortedGraphs.add(this);
		Collections.sort(sortedGraphs, GRAPH_SIZE_COMPARATOR);
		Graph initial = sortedGraphs.remove(0);
		
		Graph intersection = newGraph(initial.nodes(), initial.edges());
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
		return betweenStep(new NodeSet(from), new NodeSet(to));
	}
	
	@Override
	public Graph betweenStep(Graph from, Graph to){
		return betweenStep(from.nodes(), to.nodes());
	}
	
	@Override
	public Graph betweenStep(NodeSet from, NodeSet to){
		if(from.isEmpty() || to.isEmpty()) {
			return newGraph();
		}
		Graph forward = forwardStep(from);
		if(forward.isEmpty()) {
			return newGraph();
		}
		Graph reverse = reverseStep(to);
		if(reverse.isEmpty()) {
			return newGraph();
		}
		return forward.intersection(reverse);
	}
	
	@Override
	public Graph between(Node from, Node to) {
		return between(new NodeSet(from), new NodeSet(to));
	}
	
	@Override
	public Graph between(Graph from, Graph to) {
		return between(from.nodes(), to.nodes());
	}
	
	@Override
	public Graph between(NodeSet from, NodeSet to) {
		if(from.isEmpty() || to.isEmpty()) {
			return newGraph();
		}
		Graph forward = forward(from);
		if(forward.isEmpty()) {
			return newGraph();
		}
		Graph reverse = reverse(to);
		if(reverse.isEmpty()) {
			return newGraph();
		}
		return forward.intersection(reverse);
	}

	@Override
	public Graph forward(Node... origin){
		return forward(new NodeSet(origin));
	}
	
	@Override
	public Graph forward(Graph origin){
		Graph result = newGraph();
		result.nodes().addAll(origin.nodes());
		NodeSet frontier = new NodeSet(origin.nodes());
		while(!frontier.isEmpty()){
			Node next = frontier.one().get();
			frontier.remove(next);
			for(Edge edge : forwardStep(next).edges()){
				if(result.nodes().add(edge.to())){
					frontier.add(edge.to());
				}
				result.edges().add(edge);
			}
		}
		return result.union(origin);
	}
	
	@Override
	public Graph forward(NodeSet origin){
		return forward(newGraph(origin));
	}
	
	@Override
	public Graph reverse(Node... origin){
		return reverse(new NodeSet(origin));
	}
	
	@Override
	public Graph reverse(Graph origin){
		Graph result = newGraph();
		result.nodes().addAll(origin.nodes());
		NodeSet frontier = new NodeSet(origin.nodes());
		while(!frontier.isEmpty()){
			Node next = frontier.one().get();
			frontier.remove(next);
			for(Edge edge : reverseStep(next).edges()){
				if(result.nodes().add(edge.from())){
					frontier.add(edge.from());
				}
				result.edges().add(edge);
			}
		}
		return result.union(origin);
	}
	
	@Override
	public Graph reverse(NodeSet origin){
		return reverse(newGraph(origin));
	}
	
	@Override
	public Graph induce(Edge... edges){
		return induce(new EdgeSet(edges));
	}
	
	@Override
	public Graph induce(Graph... graphs){
		EdgeSet inducibleEdges = new EdgeSet();
		for(Graph graph : graphs){
			inducibleEdges.addAll(graph.edges());
		}
		return induce(inducibleEdges);
	}
	
	@Override
	public Graph induce(EdgeSet edges){
		Graph result = newGraph(this);
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
	public EdgeSet selectEdges(String attribute, Object... values){
		return edges.filter(attribute, values);
	}
	
	@Override
	public NodeSet selectNodes(String attribute){
		return nodes.filter(attribute);
	}
	
	@Override
	public NodeSet selectNodes(String attribute, Object... values){
		return nodes.filter(attribute, values);
	}
	
	@Override
	public NodeSet nodes(String... tags){
		return nodesTaggedWithAny(tags);
	}
	
	@Override
	public NodeSet nodesTaggedWithAny(String... tags){
	    Set<String> tagSet = Arrays.stream(tags).collect(Collectors.toSet());
        NodeSet result = new NodeSet();
        for(Node node : nodes()){
            for(String tag : tagSet){
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
	    Set<String> tagSet = Arrays.stream(tags).collect(Collectors.toSet());
        NodeSet result = new NodeSet();
        for(Node node : nodes()){
            boolean add = true;
            for(String tag : tagSet){
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
	    Set<String> tagSet = Arrays.stream(tags).collect(Collectors.toSet());
        EdgeSet result = new EdgeSet();
        for(Edge edge : edges){
            for(String tag : tagSet){
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
	    Set<String> tagSet = Arrays.stream(tags).collect(Collectors.toSet());
        EdgeSet result = new EdgeSet();
        for(Edge edge : edges){
            boolean add = true;
            for(String tag : tagSet){
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

