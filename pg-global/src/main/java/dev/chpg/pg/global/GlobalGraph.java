package dev.chpg.pg.global;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.EdgeSet;
import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.Node.NodeDirection;
import dev.chpg.pg.api.NodeSet;

/**
 * GlobalGraph provides the core storage, adjacency maps, and graph operations.
 */
public final class GlobalGraph implements Graph, GlobalFactory {

    private static final EdgeSet EMPTY_EDGES = EdgeSet.empty();

    private Map<Integer, GlobalNode> nodes;
    private Map<Integer, GlobalEdge> edges;
    private Map<Integer, GlobalEdgeSet> inEdges;
    private Map<Integer, GlobalEdgeSet> outEdges;

    /**
     * Constructs a new empty graph
     */
    @Override
    public GlobalGraph createGraph() {
        return new GlobalGraph();
    }

    @Override
    public GlobalGraph createGraph(Node... nodes) {
        Objects.requireNonNull(nodes, "nodes cannot be null");
        for (Node n : nodes) { Objects.requireNonNull(n, "nodes elements cannot be null"); }
        return new GlobalGraph(nodes);
    }

    @Override
    public GlobalGraph createGraph(NodeSet nodes) {
        Objects.requireNonNull(nodes, "nodes cannot be null");
        return new GlobalGraph(nodes);
    }

    @Override
    public GlobalGraph createGraph(Edge... edges) {
        Objects.requireNonNull(edges, "edges cannot be null");
        for (Edge e : edges) { Objects.requireNonNull(e, "edges elements cannot be null"); }
        return new GlobalGraph(edges);
    }

    @Override
    public GlobalGraph createGraph(EdgeSet edges) {
        Objects.requireNonNull(edges, "edges cannot be null");
        return new GlobalGraph(edges);
    }

    @Override
    public GlobalGraph createGraph(NodeSet nodes, EdgeSet edges) {
        Objects.requireNonNull(nodes, "nodes cannot be null");
        Objects.requireNonNull(edges, "edges cannot be null");
        return new GlobalGraph(nodes, edges);
    }

    @Override
    public GlobalGraph createGraph(Graph... graphs) {
        Objects.requireNonNull(graphs, "graphs cannot be null");
        for (Graph g : graphs) { Objects.requireNonNull(g, "graphs elements cannot be null"); }
        return new GlobalGraph(graphs);
    }

    @Override
    public GlobalGraph createGraph(Collection<Graph> graphs) {
        Objects.requireNonNull(graphs, "graphs cannot be null");
        for (Graph g : graphs) { Objects.requireNonNull(g, "graphs elements cannot be null"); }
        return new GlobalGraph(graphs);
    }

    public GlobalGraph() {
        this.nodes = new HashMap<>();
        this.edges = new HashMap<>();
        this.inEdges = new HashMap<>();
        this.outEdges = new HashMap<>();
    }

    private GlobalGraph(int nodeCapacity, int edgeCapacity) {
        int nodeMapCapacity = (int) (nodeCapacity / 0.75f) + 1;
        int edgeMapCapacity = (int) (edgeCapacity / 0.75f) + 1;
        this.nodes = new HashMap<>(nodeMapCapacity);
        this.edges = new HashMap<>(edgeMapCapacity);
        this.inEdges = new HashMap<>(nodeMapCapacity);
        this.outEdges = new HashMap<>(nodeMapCapacity);
    }

    public GlobalFactory factory() {
        return this;
    }

    /**
     * Constructs a new graph of the given nodes
     */
    /**
     * Constructs a new graph with initial nodes.
     * @param nodes the nodes
     */
    /**
     * Constructs a new graph with initial nodes.
     * @param nodes the nodes
     */
    public GlobalGraph(Node... nodes) {
        this(nodes.length, 0);
        Objects.requireNonNull(nodes, "nodes cannot be null");
        for (Node n : nodes) { Objects.requireNonNull(n, "nodes elements cannot be null"); }
        for (Node node : nodes) {
            addNode(node);
        }
    }
    
    /**
     * Constructs a new graph of the given nodes
     */
    /**
     * Constructs a new graph with initial nodes.
     * @param nodes the nodes
     */
    /**
     * Constructs a new graph with initial nodes.
     * @param nodes the nodes
     */
    public GlobalGraph(NodeSet nodes) {
        this();
        Objects.requireNonNull(nodes, "nodes cannot be null");
        addAllNodes(nodes);
    }

    /**
     * Constructs a new graph of the given edges and respective edge nodes
     */
    /**
     * Constructs a new graph with initial edges.
     * @param edges the edges
     */
    /**
     * Constructs a new graph with initial edges.
     * @param edges the edges
     */
    public GlobalGraph(Edge... edges) {
        // Over-allocation is Cheap, Rehashing is Expensive
        // In a highly connected graph, the true number of unique nodes will be much lower than edges.length * 2.
        // We will likely over-estimate the required capacity. However, in Java HashMap or HashSet implementations,
        // the "capacity" just dictates the length of the internal bucket array. Over-estimating by a factor of 2 or 3
        // only costs a few kilobytes of empty array slots. Under-estimating, on the other hand, means the Map hits
        // its load factor mid-loop, creates a new, larger bucket array, and painstakingly recalculates the hash
        // and shifts every single existing node into the new buckets.
        this(edges.length * 2, edges.length);
        Objects.requireNonNull(edges, "edges cannot be null");
        for (Edge edge : edges) { Objects.requireNonNull(edge, "edges elements cannot be null"); }
        for (Edge edge : edges) {
            addEdge(edge);
        }
    }

    /**
     * Constructs a new graph of the given edges and respective edge nodes
     */
    /**
     * Constructs a new graph with initial edges.
     * @param edges the edges
     */
    /**
     * Constructs a new graph with initial edges.
     * @param edges the edges
     */
    public GlobalGraph(EdgeSet edges) {
        this();
        Objects.requireNonNull(edges, "edges cannot be null");
        for(Edge edge : edges) {
            addEdge(edge);
        }
    }
    
    /**
     * Constructs a new graph of the given edges and respective edge nodes
     */
    /**
     * Constructs a new graph with initial nodes and edges.
     * @param nodes the nodes
     * @param edges the edges
     */
    /**
     * Constructs a new graph with initial nodes and edges.
     * @param nodes the nodes
     * @param edges the edges
     */
    public GlobalGraph(NodeSet nodes, EdgeSet edges) {
        this();
        Objects.requireNonNull(nodes, "nodes cannot be null");
        Objects.requireNonNull(edges, "edges cannot be null");
        addAllNodes(nodes);
        addAllEdges(edges);
    }
    
    /**
     * Constructs a new graph of the nodes and edges collectively contained in the given graphs
     */
    public GlobalGraph(Graph... graphs) {
        this(sumNodes(graphs), sumEdges(graphs));
        Objects.requireNonNull(graphs, "graphs cannot be null");
        for (Graph g : graphs) { Objects.requireNonNull(g, "graphs elements cannot be null"); }
        for (Graph graph : graphs) {
            addAllNodes(graph.nodes());
            addAllEdges(graph.edges());
        }
    }

    private static int sumNodes(Graph[] graphs) {
        int sum = 0;
        if (graphs != null) {
            for (Graph g : graphs) {
                if (g != null) { sum += g.nodes().size(); }
            }
        }
        return sum;
    }

    private static int sumEdges(Graph[] graphs) {
        int sum = 0;
        if (graphs != null) {
            for (Graph g : graphs) {
                if (g != null) { sum += g.edges().size(); }
            }
        }
        return sum;
    }

    /**
     * Constructs a new graph of the nodes and edges collectively contained in the given graphs
     */
    public GlobalGraph(Collection<Graph> graphs) {
        this();
        Objects.requireNonNull(graphs, "graphs cannot be null");
        for (Graph g : graphs) { Objects.requireNonNull(g, "graphs elements cannot be null"); }
        for(Graph graph : graphs) {
            addAllNodes(graph.nodes());
            addAllEdges(graph.edges());
        }
    }
    

    /**
     * Gets incoming edges to node
     * @return The set of incoming edges to the given node
     * @param node the node
     */
    protected Optional<EdgeSet> getInEdgesToNode(Node node){
        if (!(node instanceof GlobalNode gn)) { return Optional.empty(); }
        return Optional.ofNullable(inEdges.get(gn.id()));
    }

    /**
     * Gets out-coming edges from node
     * @return The set of out-coming edges from the given node
     * @param node the node
     */
    protected Optional<EdgeSet> getOutEdgesFromNode(Node node){
        if (!(node instanceof GlobalNode gn)) { return Optional.empty(); }
        return Optional.ofNullable(outEdges.get(gn.id()));
    }


    @Override
    public boolean containsNode(Node node) {
        // Silent Ignore
        if (!(node instanceof GlobalNode gn)) { return false; }
        return nodes.containsKey(gn.id());
    }

    @Override
    public boolean containsEdge(Edge edge) {
        // Silent Ignore
        if (!(edge instanceof GlobalEdge ge)) { return false; }
        return edges.containsKey(ge.id());
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
        if (!(node instanceof GlobalNode gn)) {
            throw new IllegalArgumentException("Expected GlobalNode in GlobalGraph.");
        }

        // 2. Idempotent check (don't overwrite existing wiring)
        if (nodes.containsKey(gn.id())) {
            return false;
        }

        // 3. Synchronize the 4 Pillars
        nodes.put(gn.id(), gn);
        outEdges.put(gn.id(), new GlobalEdgeSet());
        inEdges.put(gn.id(), new GlobalEdgeSet());
        return true;
    }

    @Override
    public boolean addEdge(Edge edge) {
        Objects.requireNonNull(edge, "edge cannot be null");
        if (!(edge instanceof GlobalEdge)) {
            throw new IllegalArgumentException("Expected GlobalEdge in GlobalGraph.");
        }
        // Auto-vivify terminal nodes safely
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
        if (!(edge instanceof GlobalEdge ge)) {
            throw new IllegalArgumentException("Expected GlobalEdge in GlobalGraph.");
        }

        // 2. Validate Topology Anchors
        // The graph MUST violently fail if the nodes aren't registered,
        // otherwise the adjacency maps will throw NullPointerExceptions.
        if (!nodes.containsKey(ge.from().id()) || !nodes.containsKey(ge.to().id())) {
            throw new IllegalArgumentException("Source or target node is not present in the graph.");
        }

        // 3. Idempotent check
        if (edges.putIfAbsent(ge.id(), ge) != null) {
            return false; // Edge already existed
        }

        // 4. Wire the adjacency maps
        outEdges.get(ge.from().id()).add(ge);
        inEdges.get(ge.to().id()).add(ge);
        return true;
    }

    @Override
    public boolean addAllNodes(Collection<? extends Node> nodes) {
        Objects.requireNonNull(nodes, "nodes cannot be null");
        boolean result = false;
        for(Node node : nodes) {
            Objects.requireNonNull(node, "node set elements cannot be null");
        }
        for(Node node : nodes) {
            result |= addNode(node);
        }
        return result;
    }

    @Override
    public boolean addAllEdges(Collection<? extends Edge> edges) {
        Objects.requireNonNull(edges, "edges cannot be null");
        boolean result = false;
        for(Edge edge : edges) {
            Objects.requireNonNull(edge, "edge set elements cannot be null");
        }
        for(Edge edge : edges) {
            result |= addEdge(edge);
        }
        return result;
    }

    @Override
    public boolean removeNode(Node node) {
        // 1. Silent Ignore
        if (!(node instanceof GlobalNode gn)) {
            return false;
        }

        int targetId = gn.id();

        // 2. Collapse the pillars for this ID
        if (nodes.remove(targetId) == null) {
            return false;
        }

        GlobalEdgeSet outSet = outEdges.remove(targetId);
        GlobalEdgeSet inSet = inEdges.remove(targetId);

        // 3. Cascading Teardown: Scrub Outbound Edges
        if (outSet != null) {
            for (Edge out : outSet) {
                edges.remove(out.id()); // Remove from global registry
                int toId = out.to().id();
                if (toId != targetId) { // Skip self-loops, pillar already collapsed
                    inEdges.get(toId).remove(out); // Disconnect from neighbor
                }
            }
        }

        // 4. Cascading Teardown: Scrub Inbound Edges
        if (inSet != null) {
            for (Edge in : inSet) {
                int fromId = in.from().id();
                if (fromId != targetId) { // Skip self-loops, handled in outbound pass
                    edges.remove(in.id()); // Remove from global registry
                    outEdges.get(fromId).remove(in); // Disconnect from neighbor
                }
            }
        }

        return true;
    }

    @Override
    public boolean removeEdge(Edge edge) {
        // 1. Silent Ignore
        if (!(edge instanceof GlobalEdge ge) || !edges.containsKey(ge.id())) {
            return false;
        }

        // 2. Teardown
        edges.remove(ge.id());
        outEdges.get(ge.from().id()).remove(ge);
        inEdges.get(ge.to().id()).remove(ge);
        return true;
    }

    @Override
    public boolean removeAllNodes(Collection<? extends Node> nodes) {
        Objects.requireNonNull(nodes, "nodes cannot be null");
        boolean result = false;
        for(Node node : nodes) {
            Objects.requireNonNull(node, "node set elements cannot be null");
        }
        for(Node node : new ArrayList<>(nodes)) {
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
        for(Edge edge : new ArrayList<>(edges)) {
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
        return new GlobalUnmodifiableLiveNodeSet(nodes, edges, inEdges, outEdges);
    }


    @Override
    public EdgeSet edges() {
        return new GlobalUnmodifiableLiveEdgeSet(nodes, edges, inEdges, outEdges);
    }


    @Override
    public boolean isEmpty() {
        return nodes.isEmpty();
    }

    @Override
    public EdgeSet edges(Node node, NodeDirection direction){
        if(direction == NodeDirection.IN){
            return getInEdgesToNode(node).map(GlobalEdgeSet::new).orElseGet(GlobalEdgeSet::new);
        } else if(direction == NodeDirection.OUT){
            return getOutEdgesFromNode(node).map(GlobalEdgeSet::new).orElseGet(GlobalEdgeSet::new);
        } else {
            GlobalEdgeSet edges = getInEdgesToNode(node).map(GlobalEdgeSet::new).orElseGet(GlobalEdgeSet::new);
            getOutEdgesFromNode(node).ifPresent(edges::addAll);
            return edges;
        }
    }

    @Override
    public NodeSet roots() {
        GlobalNodeSet result = new GlobalNodeSet();
        for (GlobalNode n : this.nodes.values()) {
            GlobalEdgeSet inbound = this.inEdges.get(n.id());
            if (inbound == null || inbound.isEmpty()) {
                result.add(n);
            }
        }
        return result;
    }

    @Override
    public NodeSet leaves() {
        GlobalNodeSet result = new GlobalNodeSet();
        for (GlobalNode n : this.nodes.values()) {
            GlobalEdgeSet outbound = this.outEdges.get(n.id());
            if (outbound == null || outbound.isEmpty()) {
                result.add(n);
            }
        }
        return result;
    }

    @Override
    public NodeSet isolated() {
        GlobalNodeSet result = new GlobalNodeSet();
        for (GlobalNode n : this.nodes.values()) {
            GlobalEdgeSet inbound = this.inEdges.get(n.id());
            GlobalEdgeSet outbound = this.outEdges.get(n.id());

            boolean noIn = (inbound == null || inbound.isEmpty());
            boolean noOut = (outbound == null || outbound.isEmpty());

            if (noIn && noOut) {
                result.add(n);
            }
        }
        return result;
    }

    @Override
    public NodeSet predecessors(Node... origin){
        Objects.requireNonNull(origin, "origin cannot be null");
        for (Node n : origin) { Objects.requireNonNull(n, "origin elements cannot be null"); }
        return predecessors(new GlobalNodeSet(origin));
    }

    @Override
    public NodeSet predecessors(Graph origin){
        Objects.requireNonNull(origin, "origin cannot be null");
        return predecessors(origin.nodes());
    }

    @Override
    public NodeSet predecessors(NodeSet origin){
        Objects.requireNonNull(origin, "origin cannot be null");
        NodeSet result = new GlobalNodeSet();
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
        Objects.requireNonNull(origin, "origin cannot be null");
        for (Node n : origin) { Objects.requireNonNull(n, "origin elements cannot be null"); }
        return successors(new GlobalNodeSet(origin));
    }

    @Override
    public NodeSet successors(Graph origin){
        Objects.requireNonNull(origin, "origin cannot be null");
        return successors(origin.nodes());
    }

    @Override
    public NodeSet successors(NodeSet origin){
        Objects.requireNonNull(origin, "origin cannot be null");
        NodeSet result = new GlobalNodeSet();
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
        for (Node n : origin) { Objects.requireNonNull(n, "origin elements cannot be null"); }
        return forwardStep(new GlobalNodeSet(origin));
    }

    @Override
    public Graph forwardStep(Graph origin){
        Objects.requireNonNull(origin, "origin cannot be null");
        Graph result = new GlobalGraph(origin);
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
        return forwardStep(new GlobalGraph(origin));
    }

    @Override
    public Graph reverseStep(Node... origin){
        Objects.requireNonNull(origin, "origin cannot be null");
        for (Node n : origin) { Objects.requireNonNull(n, "origin elements cannot be null"); }
        return reverseStep(new GlobalNodeSet(origin));
    }

    @Override
    public Graph reverseStep(Graph origin){
        Objects.requireNonNull(origin, "origin cannot be null");
        Graph result = new GlobalGraph(origin);
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
        return reverseStep(new GlobalGraph(origin));
    }

    @Override
    public Graph union(Node... nodes){
        Objects.requireNonNull(nodes, "nodes cannot be null");
        for (Node n : nodes) { Objects.requireNonNull(n, "nodes elements cannot be null"); }
        return union(new GlobalGraph(nodes));
    }

    @Override
    public Graph union(Edge... edges){
        Objects.requireNonNull(edges, "edges cannot be null");
        for (Edge e : edges) { Objects.requireNonNull(e, "edges elements cannot be null"); }
        return union(new GlobalGraph(edges));
    }


    private int topologicalVolume() {
        return this.nodes().size() + this.edges().size();
    }

    private int topologicalVolume(Graph g) {
        return g.nodes().size() + g.edges().size();
    }

    private boolean isSizeKnown(Graph g) {
        return g.nodes().isSizeKnown() && g.edges().isSizeKnown();
    }
    @Override
    public Graph union(Graph graph){
        Objects.requireNonNull(graph, "graph cannot be null");

        if (this.isSizeKnown(this) && this.isSizeKnown(graph)) {
            if (this.topologicalVolume() < this.topologicalVolume(graph)) {
                return graph.union(this);
            }
        }
        Graph union = new GlobalGraph(this.nodes(), this.edges());
        union.addAllNodes(graph.nodes());
        union.addAllEdges(graph.edges());
        return union;
    }

    @Override
    public Graph difference(Node... nodes){
        Objects.requireNonNull(nodes, "nodes cannot be null");
        for (Node n : nodes) { Objects.requireNonNull(n, "nodes elements cannot be null"); }
        return difference(new GlobalGraph(nodes));
    }

    @Override
    public Graph difference(Edge... edges){
        Objects.requireNonNull(edges, "edges cannot be null");
        for (Edge e : edges) { Objects.requireNonNull(e, "edges elements cannot be null"); }
        return difference(new GlobalGraph(edges));
    }

    @Override
    public Graph difference(Graph graph){
        Objects.requireNonNull(graph, "graph cannot be null");
        Graph difference = new GlobalGraph(this.nodes(), this.edges());
        if(difference.isEmpty()) {
            return difference;
        }
        for (Node node : graph.nodes()) {
            difference.removeNode(node);
        }
        difference.removeAllEdges(graph.edges());
        return difference;
    }

    @Override
    public Graph differenceEdges(Edge... edges){
        Objects.requireNonNull(edges, "edges cannot be null");
        for (Edge e : edges) { Objects.requireNonNull(e, "edges elements cannot be null"); }
        return differenceEdges(new GlobalGraph(edges));
    }

    @Override
    public Graph differenceEdges(Graph graph){
        Objects.requireNonNull(graph, "graph cannot be null");
        Graph difference = new GlobalGraph(this.nodes(), this.edges());
        if(difference.edges().isEmpty()) {
            return difference;
        }
        difference.removeAllEdges(graph.edges());
        return difference;
    }

    @Override
    public Graph intersection(Node... nodes){
        Objects.requireNonNull(nodes, "nodes cannot be null");
        for (Node n : nodes) { Objects.requireNonNull(n, "nodes elements cannot be null"); }
        return intersection(new GlobalGraph(nodes));
    }

    @Override
    public Graph intersection(Edge... edges){
        Objects.requireNonNull(edges, "edges cannot be null");
        for (Edge e : edges) { Objects.requireNonNull(e, "edges elements cannot be null"); }
        return intersection(new GlobalGraph(edges));
    }

    @Override
    public Graph intersection(Graph graph){
        Objects.requireNonNull(graph, "graph cannot be null");

        if (this.isSizeKnown(this) && this.isSizeKnown(graph)) {
            if (this.topologicalVolume() > this.topologicalVolume(graph)) {
                return graph.intersection(this);
            }
        }
        Graph intersection = new GlobalGraph(this.nodes(), this.edges());
        if(intersection.isEmpty()) {
            return intersection;
        }
        intersection.retainAllNodes(graph.nodes());
        intersection.retainAllEdges(graph.edges());
        return intersection;
    }

    @Override
    public Graph betweenStep(Node from, Node to){
        Objects.requireNonNull(from, "from cannot be null");
        Objects.requireNonNull(to, "to cannot be null");
        return betweenStep(new GlobalNodeSet(from), new GlobalNodeSet(to));
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
            return new GlobalGraph();
        }
        Graph forward = forwardStep(from);
        if(forward.isEmpty()) {
            return new GlobalGraph();
        }
        Graph reverse = reverseStep(to);
        if(reverse.isEmpty()) {
            return new GlobalGraph();
        }
        return forward.intersection(reverse);
    }

    @Override
    public Graph between(Node from, Node to) {
        Objects.requireNonNull(from, "from cannot be null");
        Objects.requireNonNull(to, "to cannot be null");
        return between(new GlobalNodeSet(from), new GlobalNodeSet(to));
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
            return new GlobalGraph();
        }
        Graph forward = forward(from);
        if(forward.isEmpty()) {
            return new GlobalGraph();
        }
        Graph reverse = reverse(to);
        if(reverse.isEmpty()) {
            return new GlobalGraph();
        }
        return forward.intersection(reverse);
    }

    @Override
    public Graph forward(Node... origin){
        Objects.requireNonNull(origin, "origin cannot be null");
        for (Node n : origin) { Objects.requireNonNull(n, "origin elements cannot be null"); }
        return forward(new GlobalNodeSet(origin));
    }

    @Override
    public Graph forward(Graph origin){
        Objects.requireNonNull(origin, "origin cannot be null");
        Graph result = new GlobalGraph(origin);
        NodeSet frontier = new GlobalNodeSet(origin.nodes());
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
        return forward(new GlobalGraph(origin));
    }

    @Override
    public Graph reverse(Node... origin){
        Objects.requireNonNull(origin, "origin cannot be null");
        for (Node n : origin) { Objects.requireNonNull(n, "origin elements cannot be null"); }
        return reverse(new GlobalNodeSet(origin));
    }

    @Override
    public Graph reverse(Graph origin){
        Objects.requireNonNull(origin, "origin cannot be null");
        Graph result = new GlobalGraph(origin);
        NodeSet frontier = new GlobalNodeSet(origin.nodes());
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
        return reverse(new GlobalGraph(origin));
    }

    @Override
    public Graph induce(Edge... edges){
        Objects.requireNonNull(edges, "edges cannot be null");
        for (Edge e : edges) { Objects.requireNonNull(e, "edges elements cannot be null"); }
        return induce(new GlobalEdgeSet(edges));
    }

    @Override
    public Graph induce(Graph graph){
        Objects.requireNonNull(graph, "graph cannot be null");
        EdgeSet inducibleEdges = new GlobalEdgeSet();
        inducibleEdges.addAll(graph.edges());
        return induce(inducibleEdges);
    }

    @Override
    public Graph induce(EdgeSet edges){
        Objects.requireNonNull(edges, "edges cannot be null");
        Graph result = new GlobalGraph(this);
        for(Edge edge : edges) {
            if(result.nodes().contains(edge.from()) && result.nodes().contains(edge.to())) {
                result.addEdge(edge);
            }
        }
        return result;
    }


    @Override
    public NodeSet singleton(Node node) {
        return new GlobalImmutableSingletonNodeSet((GlobalNode) node);
    }

    @Override
    public GlobalNode createNode() {
        return new GlobalNode();
    }

    @Override
    public EdgeSet singleton(Edge edge) {
        return new GlobalImmutableSingletonEdgeSet((GlobalEdge) edge);
    }

    @Override
    public GlobalEdge createEdge(Node source, Node target) {
        if (!(source instanceof GlobalNode)) {
            throw new IllegalArgumentException("Source node is not native to GlobalGraph.");
        }
        if (!(target instanceof GlobalNode)) {
            throw new IllegalArgumentException("Target node is not native to GlobalGraph.");
        }
        return new GlobalEdge(source, target);
    }

    @Override
    public boolean adjacent(Node source, Node target) {
        if (!(source instanceof GlobalNode hSource) || !nodes.containsKey(hSource.id())) { return false; }
        if (!(target instanceof GlobalNode hTarget) || !nodes.containsKey(hTarget.id())) { return false; }

        Optional<EdgeSet> out = getOutEdgesFromNode(hSource);
        if (out.isEmpty()) { return false; }

        for (Edge e : out.get()) {
            if (e.to().equals(hTarget)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public EdgeSet edges(Node source, Node target) {
        if (!(source instanceof GlobalNode hSource) || !nodes.containsKey(hSource.id())) { return EMPTY_EDGES; }
        if (!(target instanceof GlobalNode hTarget) || !nodes.containsKey(hTarget.id())) { return EMPTY_EDGES; }

        Optional<EdgeSet> out = getOutEdgesFromNode(hSource);
        if (out.isEmpty()) { return EMPTY_EDGES; }

        EdgeSet result = new GlobalEdgeSet();
        for (Edge e : out.get()) {
            if (e.to().equals(hTarget)) {
                result.add(e);
            }
        }
        return result.isEmpty() ? EMPTY_EDGES : (result.size() == 1 ? new GlobalImmutableSingletonEdgeSet((GlobalEdge) result.iterator().next()) : new GlobalImmutableEdgeSet(result));
    }

    @Override
    public int degree(Node node, NodeDirection direction) {
        if (!(node instanceof GlobalNode gn) || !nodes.containsKey(gn.id())) {
            return 0; // Silent Ignore
        }

        return switch (direction) {
            case OUT -> {
                GlobalEdgeSet out = outEdges.get(gn.id());
                yield out == null ? 0 : out.size();
            }
            case IN -> {
                GlobalEdgeSet in = inEdges.get(gn.id());
                yield in == null ? 0 : in.size();
            }
            case BOTH -> {
                GlobalEdgeSet out = outEdges.get(gn.id());
                GlobalEdgeSet in = inEdges.get(gn.id());
                yield (out == null ? 0 : out.size()) + (in == null ? 0 : in.size());
            }
        };
    }

}
