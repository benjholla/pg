package dev.chpg.pg.multiverse.ephemeral;

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
 * EphemeralGraph provides the core storage, adjacency maps, and graph operations.
 */
public final class EphemeralGraph implements Graph, EphemeralFactory {

    private static final EdgeSet EMPTY_EDGES = EdgeSet.empty();

    private Map<Integer, EphemeralNode> nodes;
    private Map<Integer, EphemeralEdge> edges;
    private Map<Integer, EphemeralEdgeSet> inEdges;
    private Map<Integer, EphemeralEdgeSet> outEdges;
    private final EphemeralIdGenerator idGenerator;

    /**
     * Constructs a new empty graph
     */
    @Override
    public EphemeralGraph createGraph() {
        return new EphemeralGraph(this.idGenerator);
    }

    @Override
    public EphemeralGraph createGraph(Node... nodes) {
        Objects.requireNonNull(nodes, "nodes cannot be null");
        for (Node n : nodes) { Objects.requireNonNull(n, "nodes elements cannot be null"); }
        return new EphemeralGraph(this.idGenerator, nodes);
    }

    @Override
    public EphemeralGraph createGraph(NodeSet nodes) {
        Objects.requireNonNull(nodes, "nodes cannot be null");
        return new EphemeralGraph(this.idGenerator, nodes);
    }

    @Override
    public EphemeralGraph createGraph(Edge... edges) {
        Objects.requireNonNull(edges, "edges cannot be null");
        for (Edge e : edges) { Objects.requireNonNull(e, "edges elements cannot be null"); }
        return new EphemeralGraph(this.idGenerator, edges);
    }

    @Override
    public EphemeralGraph createGraph(EdgeSet edges) {
        Objects.requireNonNull(edges, "edges cannot be null");
        return new EphemeralGraph(this.idGenerator, edges);
    }

    @Override
    public EphemeralGraph createGraph(NodeSet nodes, EdgeSet edges) {
        Objects.requireNonNull(nodes, "nodes cannot be null");
        Objects.requireNonNull(edges, "edges cannot be null");
        return new EphemeralGraph(this.idGenerator, nodes, edges);
    }

    @Override
    public EphemeralGraph createGraph(Graph graph) {
        Objects.requireNonNull(graph, "graph cannot be null");
        validateLineage(graph);
        return new EphemeralGraph(this.idGenerator, graph.nodes(), graph.edges());
    }

    /**
     * Constructs a new empty graph.
     */
    public EphemeralGraph() {
        this.nodes = new HashMap<>();
        this.edges = new HashMap<>();
        this.inEdges = new HashMap<>();
        this.outEdges = new HashMap<>();
        this.idGenerator = new EphemeralIdGenerator();
    }

    private EphemeralGraph(EphemeralIdGenerator idGenerator) {
        this.nodes = new HashMap<>();
        this.edges = new HashMap<>();
        this.inEdges = new HashMap<>();
        this.outEdges = new HashMap<>();
        this.idGenerator = idGenerator;
    }

    private EphemeralGraph(EphemeralIdGenerator idGenerator, int nodeCapacity, int edgeCapacity) {
        int nodeMapCapacity = (int) (nodeCapacity / 0.75f) + 1;
        int edgeMapCapacity = (int) (edgeCapacity / 0.75f) + 1;
        this.nodes = new HashMap<>(nodeMapCapacity);
        this.edges = new HashMap<>(edgeMapCapacity);
        this.inEdges = new HashMap<>(nodeMapCapacity);
        this.outEdges = new HashMap<>(nodeMapCapacity);
        this.idGenerator = idGenerator;
    }


    /**
     * Ensures that all incoming graphs belong to the exact same Ephemeral ID sandbox.
     * Blocks cross-engine algebra and cross-sandbox contamination.
     */
    private void validateLineage(Graph... others) {
        for (Graph other : others) {
            // 1. Block Cross-Engine Contamination
            if (!(other instanceof EphemeralGraph ephemeralOther)) {
                throw new IllegalArgumentException(
                    "Engine mismatch: EphemeralGraph can only perform set algebra with other EphemeralGraphs. " +
                    "Received: " + other.getClass().getSimpleName()
                );
            }

            // 2. Block Cross-Sandbox Contamination
            if (this.idGenerator != ephemeralOther.idGenerator) {
                throw new IllegalArgumentException(
                    "Sandbox mismatch: Cannot perform operations across independent EphemeralGraph instances. " +
                    "The graphs do not share the same EphemeralIdGenerator lineage."
                );
            }
        }
    }

    /**
     * Gets the graph factory.
     * @return the factory
     */
    public EphemeralFactory factory() {
        return this;
    }

    /**
     * Constructs a new graph of the given nodes
     */
    private EphemeralGraph(EphemeralIdGenerator idGenerator, Node... nodes) {
        this(idGenerator, nodes.length, 0);
        Objects.requireNonNull(nodes, "nodes cannot be null");
        for (Node n : nodes) { Objects.requireNonNull(n, "nodes elements cannot be null"); }
        for (Node node : nodes) {
            addNode(node);
        }
    }

    /**
     * Constructs a new graph of the given nodes
     */
    private EphemeralGraph(EphemeralIdGenerator idGenerator, NodeSet nodes) {
        this(idGenerator);
        Objects.requireNonNull(nodes, "nodes cannot be null");
        addAllNodes(nodes);
    }

    /**
     * Constructs a new graph of the given edges and respective edge nodes
     */
    private EphemeralGraph(EphemeralIdGenerator idGenerator, Edge... edges) {
        // Over-allocation is Cheap, Rehashing is Expensive
        // In a highly connected graph, the true number of unique nodes will be much lower than edges.length * 2.
        // We will likely over-estimate the required capacity. However, in Java HashMap or HashSet implementations,
        // the "capacity" just dictates the length of the internal bucket array. Over-estimating by a factor of 2 or 3
        // only costs a few kilobytes of empty array slots. Under-estimating, on the other hand, means the Map hits
        // its load factor mid-loop, creates a new, larger bucket array, and painstakingly recalculates the hash
        // and shifts every single existing node into the new buckets.
        this(idGenerator, edges.length * 2, edges.length);
        Objects.requireNonNull(edges, "edges cannot be null");
        for (Edge e : edges) { Objects.requireNonNull(e, "edges elements cannot be null"); }
        for (Edge edge : edges) {
            addEdge(edge);
        }
    }

    /**
     * Constructs a new graph of the given edges and respective edge nodes
     */
    private EphemeralGraph(EphemeralIdGenerator idGenerator, EdgeSet edges) {
        this(idGenerator);
        Objects.requireNonNull(edges, "edges cannot be null");
        for(Edge edge : edges) {
            addEdge(edge);
        }
    }

    /**
     * Constructs a new graph of the given edges and respective edge nodes
     */
    private EphemeralGraph(EphemeralIdGenerator idGenerator, NodeSet nodes, EdgeSet edges) {
        this(idGenerator);
        Objects.requireNonNull(nodes, "nodes cannot be null");
        Objects.requireNonNull(edges, "edges cannot be null");
        addAllNodes(nodes);
        addAllEdges(edges);
    }

    /**
     * Gets incoming edges to node
     * @return The set of incoming edges to the given node
     * @param node the node
     */
    protected Optional<EdgeSet> getInEdgesToNode(Node node){
        if (!(node instanceof EphemeralNode en)) { return Optional.empty(); }
        return Optional.ofNullable(inEdges.get(en.id()));
    }

    /**
     * Gets out-coming edges from node
     * @return The set of out-coming edges from the given node
     * @param node the node
     */
    protected Optional<EdgeSet> getOutEdgesFromNode(Node node){
        if (!(node instanceof EphemeralNode en)) { return Optional.empty(); }
        return Optional.ofNullable(outEdges.get(en.id()));
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
        if (en.id() >= 0) {
            throw new IllegalArgumentException("Node ID domain must be negative.");
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
        if (ee.id() >= 0) {
            throw new IllegalArgumentException("Edge ID domain must be negative.");
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
        if (!(node instanceof EphemeralNode en)) {
            return false;
        }

        int targetId = en.id();

        // 2. Collapse the pillars for this ID
        if (nodes.remove(targetId) == null) {
            return false;
        }

        EphemeralEdgeSet outSet = outEdges.remove(targetId);
        EphemeralEdgeSet inSet = inEdges.remove(targetId);

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
        return new EphemeralUnmodifiableLiveNodeSet(nodes, edges, inEdges, outEdges);
    }


    @Override
    public EdgeSet edges() {
        return new EphemeralUnmodifiableLiveEdgeSet(nodes, edges, inEdges, outEdges);
    }

    @Override
    public EdgeSet edges(Node node, NodeDirection direction){
        if(direction == NodeDirection.IN){
            return getInEdgesToNode(node).map(EphemeralEdgeSet::new).orElseGet(EphemeralEdgeSet::new);
        } else if(direction == NodeDirection.OUT){
            return getOutEdgesFromNode(node).map(EphemeralEdgeSet::new).orElseGet(EphemeralEdgeSet::new);
        } else {
            EphemeralEdgeSet edges = getInEdgesToNode(node).map(EphemeralEdgeSet::new).orElseGet(EphemeralEdgeSet::new);
            getOutEdgesFromNode(node).ifPresent(edges::addAll);
            return edges;
        }
    }

    @Override
    public NodeSet roots() {
        EphemeralNodeSet result = new EphemeralNodeSet();
        for (EphemeralNode n : this.nodes.values()) {
            EphemeralEdgeSet inbound = this.inEdges.get(n.id());
            if (inbound == null || inbound.isEmpty()) {
                result.add(n);
            }
        }
        return result;
    }

    @Override
    public NodeSet leaves() {
        EphemeralNodeSet result = new EphemeralNodeSet();
        for (EphemeralNode n : this.nodes.values()) {
            EphemeralEdgeSet outbound = this.outEdges.get(n.id());
            if (outbound == null || outbound.isEmpty()) {
                result.add(n);
            }
        }
        return result;
    }

    @Override
    public NodeSet isolated() {
        EphemeralNodeSet result = new EphemeralNodeSet();
        for (EphemeralNode n : this.nodes.values()) {
            EphemeralEdgeSet inbound = this.inEdges.get(n.id());
            EphemeralEdgeSet outbound = this.outEdges.get(n.id());

            boolean noIn = (inbound == null || inbound.isEmpty());
            boolean noOut = (outbound == null || outbound.isEmpty());

            if (noIn && noOut) {
                result.add(n);
            }
        }
        return result;
    }

    @Override
    public NodeSet predecessors(Node origin){
        Objects.requireNonNull(origin, "origin cannot be null");
        return predecessors(new EphemeralNodeSet(origin));
    }

    @Override
    public NodeSet predecessors(Graph origin){
        Objects.requireNonNull(origin, "origin cannot be null");
        return predecessors(origin.nodes());
    }

    @Override
    public NodeSet predecessors(NodeSet origin){
        Objects.requireNonNull(origin, "origin cannot be null");
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
    public NodeSet successors(Node origin){
        Objects.requireNonNull(origin, "origin cannot be null");
        return successors(new EphemeralNodeSet(origin));
    }

    @Override
    public NodeSet successors(Graph origin){
        Objects.requireNonNull(origin, "origin cannot be null");
        return successors(origin.nodes());
    }

    @Override
    public NodeSet successors(NodeSet origin){
        Objects.requireNonNull(origin, "origin cannot be null");
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
    public Graph forwardStep(Node origin){
        Objects.requireNonNull(origin, "origin cannot be null");
        return forwardStep(new EphemeralNodeSet(origin));
    }

    @Override
    public Graph forwardStep(Graph origin){
        Objects.requireNonNull(origin, "origin cannot be null");
        validateLineage(origin);
        Graph result = createGraph(origin);
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
        return forwardStep(new EphemeralGraph(this.idGenerator, origin));
    }

    @Override
    public Graph reverseStep(Node origin){
        Objects.requireNonNull(origin, "origin cannot be null");
        return reverseStep(new EphemeralNodeSet(origin));
    }

    @Override
    public Graph reverseStep(Graph origin){
        Objects.requireNonNull(origin, "origin cannot be null");
        validateLineage(origin);
        Graph result = createGraph(origin);
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
        return reverseStep(new EphemeralGraph(this.idGenerator, origin));
    }

    @Override
    public Graph union(Node node){
        Objects.requireNonNull(node, "node cannot be null");
        return union(new EphemeralGraph(this.idGenerator, node));
    }

    @Override
    public Graph union(Edge edge){
        Objects.requireNonNull(edge, "edge cannot be null");
        return union(new EphemeralGraph(this.idGenerator, edge));
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
        validateLineage(graph);

        if (this.isSizeKnown(this) && this.isSizeKnown(graph)) {
            if (this.topologicalVolume() < this.topologicalVolume(graph)) {
                return graph.union(this);
            }
        }
        Graph union = new EphemeralGraph(this.idGenerator, this.nodes(), this.edges());
        union.addAllNodes(graph.nodes());
        union.addAllEdges(graph.edges());
        return union;
    }

    @Override
    public Graph difference(Node node){
        Objects.requireNonNull(node, "node cannot be null");
        return difference(new EphemeralGraph(this.idGenerator, node));
    }

    @Override
    public Graph difference(Edge edge){
        Objects.requireNonNull(edge, "edge cannot be null");
        return difference(new EphemeralGraph(this.idGenerator, edge));
    }

    @Override
    public Graph difference(Graph graph){
        Objects.requireNonNull(graph, "graph cannot be null");
        validateLineage(graph);
        Graph difference = new EphemeralGraph(this.idGenerator, this.nodes(), this.edges());
        if(difference.nodes().isEmpty()) {
            return difference;
        }
        for (Node node : graph.nodes()) {
            difference.removeNode(node);
        }
        difference.removeAllEdges(graph.edges());
        return difference;
    }

    @Override
    public Graph differenceEdges(Edge edge){
        Objects.requireNonNull(edge, "edge cannot be null");
        return differenceEdges(new EphemeralGraph(this.idGenerator, edge));
    }

    @Override
    public Graph differenceEdges(Graph graph){
        Objects.requireNonNull(graph, "graph cannot be null");
        validateLineage(graph);
        Graph difference = new EphemeralGraph(this.idGenerator, this.nodes(), this.edges());
        if(difference.edges().isEmpty()) {
            return difference;
        }
        difference.removeAllEdges(graph.edges());
    return difference;
    }

    @Override
    public Graph intersection(Node node){
        Objects.requireNonNull(node, "node cannot be null");
        return intersection(new EphemeralGraph(this.idGenerator, node));
    }

    @Override
    public Graph intersection(Edge edge){
        Objects.requireNonNull(edge, "edge cannot be null");
        return intersection(new EphemeralGraph(this.idGenerator, edge));
    }

    @Override
    public Graph intersection(Graph graph){
        Objects.requireNonNull(graph, "graph cannot be null");
        validateLineage(graph);

        if (this.isSizeKnown(this) && this.isSizeKnown(graph)) {
            if (this.topologicalVolume() > this.topologicalVolume(graph)) {
                return graph.intersection(this);
            }
        }
        Graph intersection = new EphemeralGraph(this.idGenerator, this.nodes(), this.edges());
        if(intersection.nodes().isEmpty()) {
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
        return betweenStep(new EphemeralNodeSet(from), new EphemeralNodeSet(to));
    }

    @Override
    public Graph betweenStep(Graph from, Graph to){
        Objects.requireNonNull(from, "from cannot be null");
        Objects.requireNonNull(to, "to cannot be null");
        validateLineage(from, to);
        return betweenStep(from.nodes(), to.nodes());
    }

    @Override
    public Graph betweenStep(NodeSet from, NodeSet to){
        Objects.requireNonNull(from, "from cannot be null");
        Objects.requireNonNull(to, "to cannot be null");
        if(from.isEmpty() || to.isEmpty()) {
            return new EphemeralGraph(this.idGenerator);
        }
        Graph forward = forwardStep(from);
        if(forward.nodes().isEmpty()) {
            return new EphemeralGraph(this.idGenerator);
        }
        Graph reverse = reverseStep(to);
        if(reverse.nodes().isEmpty()) {
            return new EphemeralGraph(this.idGenerator);
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
        validateLineage(from, to);
        return between(from.nodes(), to.nodes());
    }

    @Override
    public Graph between(NodeSet from, NodeSet to) {
        Objects.requireNonNull(from, "from cannot be null");
        Objects.requireNonNull(to, "to cannot be null");
        if(from.isEmpty() || to.isEmpty()) {
            return new EphemeralGraph(this.idGenerator);
        }
        Graph forward = forward(from);
        if(forward.nodes().isEmpty()) {
            return new EphemeralGraph(this.idGenerator);
        }
        Graph reverse = reverse(to);
        if(reverse.nodes().isEmpty()) {
            return new EphemeralGraph(this.idGenerator);
        }
        return forward.intersection(reverse);
    }

    @Override
    public Graph forward(Node origin){
        Objects.requireNonNull(origin, "origin cannot be null");
        return forward(new EphemeralNodeSet(origin));
    }

    @Override
    public Graph forward(Graph origin){
        Objects.requireNonNull(origin, "origin cannot be null");
        validateLineage(origin);
        Graph result = createGraph(origin);
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
        return forward(new EphemeralGraph(this.idGenerator, origin));
    }

    @Override
    public Graph reverse(Node origin){
        Objects.requireNonNull(origin, "origin cannot be null");
        return reverse(new EphemeralNodeSet(origin));
    }

    @Override
    public Graph reverse(Graph origin){
        Objects.requireNonNull(origin, "origin cannot be null");
        validateLineage(origin);
        Graph result = createGraph(origin);
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
        return reverse(new EphemeralGraph(this.idGenerator, origin));
    }

    @Override
    public Graph induce(Edge edge){
        Objects.requireNonNull(edge, "edge cannot be null");
        return induce(new EphemeralEdgeSet(edge));
    }

    @Override
    public Graph induce(Graph graph){
        Objects.requireNonNull(graph, "graph cannot be null");
        EdgeSet inducibleEdges = new EphemeralEdgeSet();
        inducibleEdges.addAll(graph.edges());
        return induce(inducibleEdges);
    }

    @Override
    public Graph induce(EdgeSet edges){
        Objects.requireNonNull(edges, "edges cannot be null");
        Graph result = createGraph(this);
        for(Edge edge : edges) {
            if(result.nodes().contains(edge.from()) && result.nodes().contains(edge.to())) {
                result.addEdge(edge);
            }
        }
        return result;
    }


    @Override
    public NodeSet singleton(Node node) {
        return new EphemeralImmutableSingletonNodeSet((EphemeralNode) node);
    }

    @Override
    public EphemeralNode createNode() {
        return new EphemeralNode(idGenerator.createNodeId());
    }

    @Override
    public EdgeSet singleton(Edge edge) {
        return new EphemeralImmutableSingletonEdgeSet((EphemeralEdge) edge);
    }

    @Override
    public EphemeralEdge createEdge(Node source, Node target) {
        if (!(source instanceof EphemeralNode)) {
            throw new IllegalArgumentException("Source node is not native to EphemeralGraph.");
        }
        if (!(target instanceof EphemeralNode)) {
            throw new IllegalArgumentException("Target node is not native to EphemeralGraph.");
        }
        return new EphemeralEdge(idGenerator.createEdgeId(), source, target);
    }

    @Override
    public boolean adjacent(Node source, Node target) {
        if (!(source instanceof EphemeralNode eSource) || !nodes.containsKey(eSource.id())) { return false; }
        if (!(target instanceof EphemeralNode eTarget) || !nodes.containsKey(eTarget.id())) { return false; }

        Optional<EdgeSet> out = getOutEdgesFromNode(eSource);
        if (out.isEmpty()) { return false; }

        for (Edge e : out.get()) {
            if (e.to().equals(eTarget)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public EdgeSet edges(Node source, Node target) {
        if (!(source instanceof EphemeralNode eSource) || !nodes.containsKey(eSource.id())) { return EMPTY_EDGES; }
        if (!(target instanceof EphemeralNode eTarget) || !nodes.containsKey(eTarget.id())) { return EMPTY_EDGES; }

        Optional<EdgeSet> out = getOutEdgesFromNode(eSource);
        if (out.isEmpty()) { return EMPTY_EDGES; }

        EdgeSet result = new EphemeralEdgeSet();
        for (Edge e : out.get()) {
            if (e.to().equals(eTarget)) {
                result.add(e);
            }
        }
        return result.isEmpty() ? EMPTY_EDGES : (result.size() == 1 ? new EphemeralImmutableSingletonEdgeSet((EphemeralEdge) result.iterator().next()) : new EphemeralImmutableEdgeSet(result));
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
