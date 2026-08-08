package dev.chpg.pg.multiverse.universe;

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.EdgeSet;
import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Direction;
import dev.chpg.pg.api.Node;

import dev.chpg.pg.api.NodeSet;

import java.util.BitSet;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

/**
 * A bitwise viewport into a Universe.
 * <p>
 * Unlike an EphemeralGraph, this container holds absolutely zero domain objects in memory.
 * It manages two tightly-packed BitSets acting as masks over the centralized Universe engine.
 */
import dev.chpg.pg.api.Factory;

public final class UniverseGraph implements Graph, UniverseView {

    private final Universe universe;

    @Override
    public Factory factory() {
        throw new UnsupportedOperationException("UniverseGraph is a read-only projection and does not support creating new local elements. Use EphemeralGraph instead.");
    }
    private final BitSet activeNodes;
    private final BitSet activeEdges;

    /**
     * Constructs a new, empty viewport over the given Universe.
     *
     * @param universe the Universe instance to wrap
     */
    public UniverseGraph(Universe universe) {
        this.universe = Objects.requireNonNull(universe, "Universe cannot be null");
        this.activeNodes = new BitSet();
        this.activeEdges = new BitSet();
    }

    /**
     * Constructs a viewport backed by existing masks.
     *
     * @param universe the Universe instance to wrap
     * @param activeNodes the active nodes BitSet
     * @param activeEdges the active edges BitSet
     */
    public UniverseGraph(Universe universe, BitSet activeNodes, BitSet activeEdges) {
        this.universe = Objects.requireNonNull(universe, "Universe cannot be null");
        this.activeNodes = Objects.requireNonNull(activeNodes, "Node mask cannot be null");
        this.activeEdges = Objects.requireNonNull(activeEdges, "Edge mask cannot be null");
    }

    @Override
    public Universe universe() {
        return this.universe;
    }

    // =========================================================================
    // BASIC LOOKUPS & EXTRACTORS
    // =========================================================================

    @Override
    public Optional<Node> node(int id) {
        if (id >= 0 && this.activeNodes.get(id)) {
            // Note: Assumes UniverseNode flyweight exists
            return Optional.of(new UniverseNode(this.universe, id));
        }
        return Optional.empty();
    }

    @Override
    public Optional<Edge> edge(int id) {
        if (id >= 0 && this.activeEdges.get(id)) {
            // Note: Assumes UniverseEdge flyweight exists
            return Optional.of(new UniverseEdge(this.universe, id));
        }
        return Optional.empty();
    }

    @Override
    public NodeSet nodes() {
        return new UniverseNodeSet(this.universe, (BitSet) this.activeNodes.clone());
    }

    @Override
    public EdgeSet edges() {
        return new UniverseEdgeSet(this.universe, (BitSet) this.activeEdges.clone());
    }

    // =========================================================================
    // MUTATIONS & LIFECYCLE
    // =========================================================================

    @Override
    public boolean addNode(Node node) {
        if (!(node instanceof UniverseNode un) || un.universe() != this.universe) {
            throw new IllegalArgumentException("Node must belong to this Universe.");
        }
        if (this.activeNodes.get(un.id())) { return false; }

        this.activeNodes.set(un.id());
        return true;
    }

    @Override
    public boolean addEdge(Edge edge) {
        if (!(edge instanceof UniverseEdge ue) || ue.universe() != this.universe) {
            throw new IllegalArgumentException("Edge must belong to this Universe.");
        }
        boolean changed = false;
        // Auto-vivify terminal nodes
        changed |= addNode(ue.from());
        changed |= addNode(ue.to());
        changed |= linkEdge(ue);
        return changed;
    }

    @Override
    public boolean linkEdge(Edge edge) {
        if (!(edge instanceof UniverseEdge ue) || ue.universe() != this.universe) {
            throw new IllegalArgumentException("Edge must belong to this Universe.");
        }

        int sourceId = this.universe.edgeSource(ue.id());
        int targetId = this.universe.edgeTarget(ue.id());

        if (!this.activeNodes.get(sourceId) || !this.activeNodes.get(targetId)) {
            throw new IllegalArgumentException("Source or target node is not present in the graph viewport.");
        }

        if (this.activeEdges.get(ue.id())) { return false; }

        this.activeEdges.set(ue.id());
        return true;
    }

    @Override
    public boolean removeNode(Node node) {
        if (!(node instanceof UniverseNode un) || un.universe() != this.universe) { return false; }
        if (!this.activeNodes.get(un.id())) { return false; }

        // 1. Unmask the node
        this.activeNodes.clear(un.id());

        // 2. Cascade: Unmask all incident edges using the Engine Adjacency Matrices
        // Since Universe doesn't maintain incident edge arrays directly, we must scan the active edges
        // and remove those connected to the node being removed.
        for (int edgeId = this.activeEdges.nextSetBit(0); edgeId >= 0; edgeId = this.activeEdges.nextSetBit(edgeId + 1)) {
            if (this.universe.edgeSource(edgeId) == un.id() || this.universe.edgeTarget(edgeId) == un.id()) {
                this.activeEdges.clear(edgeId);
            }
        }

        return true;
    }

    @Override
    public boolean removeEdge(Edge edge) {
        if (!(edge instanceof UniverseEdge ue) || ue.universe() != this.universe) { return false; }
        if (!this.activeEdges.get(ue.id())) { return false; }

        this.activeEdges.clear(ue.id());
        return true;
    }

    // =========================================================================
    // BULK MUTATIONS
    // =========================================================================

    @Override
    public boolean addAllNodes(Collection<? extends Node> nodes) {
        boolean changed = false;
        for (Node n : nodes) { changed |= addNode(n); }
        return changed;
    }

    @Override
    public boolean addAllEdges(Collection<? extends Edge> edges) {
        boolean changed = false;
        for (Edge e : edges) { changed |= addEdge(e); }
        return changed;
    }

    @Override
    public boolean linkAllEdges(Collection<? extends Edge> edges) {
        boolean changed = false;
        for (Edge e : edges) { changed |= linkEdge(e); }
        return changed;
    }

    @Override
    public boolean removeAllNodes(Collection<? extends Node> nodes) {
        boolean changed = false;
        for (Node n : nodes) { changed |= removeNode(n); }
        return changed;
    }

    @Override
    public boolean removeAllEdges(Collection<? extends Edge> edges) {
        boolean changed = false;
        for (Edge e : edges) { changed |= removeEdge(e); }
        return changed;
    }

    @Override
    public boolean retainAllNodes(Collection<? extends Node> nodes) {
        boolean changed = false;
        BitSet keep = new BitSet();

        for (Node n : nodes) {
            if (n instanceof UniverseNode un && un.universe() == this.universe) {
                keep.set(un.id());
            }
        }

        BitSet toRemove = (BitSet) this.activeNodes.clone();
        toRemove.andNot(keep);

        for (int i = toRemove.nextSetBit(0); i >= 0; i = toRemove.nextSetBit(i + 1)) {
            // Leverage the single remove method to correctly process the cascading edges
            changed |= removeNode(new UniverseNode(this.universe, i));
        }
        return changed;
    }

    @Override
    public boolean retainAllEdges(Collection<? extends Edge> edges) {
        boolean changed = false;
        BitSet keep = new BitSet();

        for (Edge e : edges) {
            if (e instanceof UniverseEdge ue && ue.universe() == this.universe) {
                keep.set(ue.id());
            }
        }

        BitSet toRemove = (BitSet) this.activeEdges.clone();
        toRemove.andNot(keep);

        for (int i = toRemove.nextSetBit(0); i >= 0; i = toRemove.nextSetBit(i + 1)) {
            this.activeEdges.clear(i);
            changed = true;
        }
        return changed;
    }

    @Override
    public void clearEdges() {
        this.activeEdges.clear();
    }

    @Override
    public void clear() {
        this.activeNodes.clear();
        this.activeEdges.clear();
    }

    // =========================================================================
    // LINEAGE & INVARIANT UTILITIES
    // =========================================================================

    private void checkTopologyState(long expectedModCount) {
        if (this.universe.modCount() != expectedModCount) {
            throw new java.util.ConcurrentModificationException(
                "Universe topology was mutated during graph traversal."
            );
        }
    }

    private UniverseGraph validateLineage(Graph other) {
        Objects.requireNonNull(other, "Graph cannot be null");
        if (!(other instanceof UniverseGraph ug)) {
            throw new IllegalArgumentException("Cross-engine algebra blocked. Expected UniverseGraph.");
        }
        if (this.universe != ug.universe()) {
            throw new IllegalArgumentException("Sandbox mismatch. Graphs belong to different Universes.");
        }
        return ug;
    }

    private UniverseNode validateLineage(Node node) {
        Objects.requireNonNull(node, "Node cannot be null");
        if (!(node instanceof UniverseNode un) || un.universe() != this.universe) {
            throw new IllegalArgumentException("Node must belong to this Universe.");
        }
        return un;
    }

    private UniverseEdge validateLineage(Edge edge) {
        Objects.requireNonNull(edge, "Edge cannot be null");
        if (!(edge instanceof UniverseEdge ue) || ue.universe() != this.universe) {
            throw new IllegalArgumentException("Edge must belong to this Universe.");
        }
        return ue;
    }

    /**
     * Validates that every active edge still has both its source and target nodes active.
     * Clears any orphaned edges to enforce structural graph invariants.
     */
    private void scrubOrphanedEdges() {
        for (int edgeId = this.activeEdges.nextSetBit(0); edgeId >= 0; edgeId = this.activeEdges.nextSetBit(edgeId + 1)) {
            int sourceId = this.universe.edgeSource(edgeId);
            int targetId = this.universe.edgeTarget(edgeId);

            if (!this.activeNodes.get(sourceId) || !this.activeNodes.get(targetId)) {
                this.activeEdges.clear(edgeId);
            }
        }
    }

    // =========================================================================
    // O(1) SET ALGEBRA
    // =========================================================================

    @Override
    public Graph union(Graph graph) {
        UniverseGraph other = validateLineage(graph);
        BitSet newNodes = (BitSet) this.activeNodes.clone();
        newNodes.or(other.activeNodes);

        BitSet newEdges = (BitSet) this.activeEdges.clone();
        newEdges.or(other.activeEdges);

        return new UniverseGraph(this.universe, newNodes, newEdges);
    }

    @Override
    public Graph union(Node node) {
        UniverseNode un = validateLineage(node);
        BitSet newNodes = (BitSet) this.activeNodes.clone();
        newNodes.set(un.id());

        BitSet newEdges = (BitSet) this.activeEdges.clone();
        return new UniverseGraph(this.universe, newNodes, newEdges);
    }

    @Override
    public Graph union(Edge edge) {
        UniverseEdge ue = validateLineage(edge);
        BitSet newNodes = (BitSet) this.activeNodes.clone();
        newNodes.set(this.universe.edgeSource(ue.id()));
        newNodes.set(this.universe.edgeTarget(ue.id()));

        BitSet newEdges = (BitSet) this.activeEdges.clone();
        newEdges.set(ue.id());

        return new UniverseGraph(this.universe, newNodes, newEdges);
    }

    @Override
    public Graph difference(Graph graph) {
        UniverseGraph other = validateLineage(graph);
        BitSet newNodes = (BitSet) this.activeNodes.clone();
        newNodes.andNot(other.activeNodes);

        BitSet newEdges = (BitSet) this.activeEdges.clone();
        newEdges.andNot(other.activeEdges);

        UniverseGraph result = new UniverseGraph(this.universe, newNodes, newEdges);
        result.scrubOrphanedEdges(); // Cascade delete edges missing new endpoints
        return result;
    }

    @Override
    public Graph difference(Node node) {
        UniverseNode un = validateLineage(node);
        BitSet newNodes = (BitSet) this.activeNodes.clone();
        newNodes.clear(un.id());

        BitSet newEdges = (BitSet) this.activeEdges.clone();

        UniverseGraph result = new UniverseGraph(this.universe, newNodes, newEdges);
        result.scrubOrphanedEdges();
        return result;
    }

    @Override
    public Graph difference(Edge edge) {
        // API Contract: removing an edge via `difference` explicitly removes its endpoint nodes.
        UniverseEdge ue = validateLineage(edge);
        BitSet newNodes = (BitSet) this.activeNodes.clone();
        newNodes.clear(this.universe.edgeSource(ue.id()));
        newNodes.clear(this.universe.edgeTarget(ue.id()));

        BitSet newEdges = (BitSet) this.activeEdges.clone();
        newEdges.clear(ue.id());

        UniverseGraph result = new UniverseGraph(this.universe, newNodes, newEdges);
        result.scrubOrphanedEdges();
        return result;
    }

    @Override
    public Graph differenceEdges(Graph graph) {
        UniverseGraph other = validateLineage(graph);
        BitSet newNodes = (BitSet) this.activeNodes.clone(); // Nodes untouched

        BitSet newEdges = (BitSet) this.activeEdges.clone();
        newEdges.andNot(other.activeEdges);

        return new UniverseGraph(this.universe, newNodes, newEdges);
    }

    @Override
    public Graph differenceEdges(Edge edge) {
        UniverseEdge ue = validateLineage(edge);
        BitSet newNodes = (BitSet) this.activeNodes.clone(); // Nodes untouched

        BitSet newEdges = (BitSet) this.activeEdges.clone();
        newEdges.clear(ue.id());

        return new UniverseGraph(this.universe, newNodes, newEdges);
    }

    @Override
    public Graph intersection(Graph graph) {
        UniverseGraph other = validateLineage(graph);
        BitSet newNodes = (BitSet) this.activeNodes.clone();
        newNodes.and(other.activeNodes);

        BitSet newEdges = (BitSet) this.activeEdges.clone();
        newEdges.and(other.activeEdges);

        UniverseGraph result = new UniverseGraph(this.universe, newNodes, newEdges);
        result.scrubOrphanedEdges();
        return result;
    }

    @Override
    public Graph intersection(Node node) {
        UniverseNode un = validateLineage(node);
        BitSet newNodes = new BitSet(); // Empty start
        if (this.activeNodes.get(un.id())) {
            newNodes.set(un.id());
        }

        // Intersecting with a 1-Node/0-Edge graph always yields 0 edges
        return new UniverseGraph(this.universe, newNodes, new BitSet());
    }

    @Override
    public Graph intersection(Edge edge) {
        UniverseEdge ue = validateLineage(edge);
        BitSet newNodes = new BitSet();
        int sourceId = this.universe.edgeSource(ue.id());
        int targetId = this.universe.edgeTarget(ue.id());

        if (this.activeNodes.get(sourceId)) {
            newNodes.set(sourceId);
        }
        if (this.activeNodes.get(targetId)) {
            newNodes.set(targetId);
        }

        BitSet newEdges = new BitSet();
        if (this.activeEdges.get(ue.id())) {
            newEdges.set(ue.id());
        }

        UniverseGraph result = new UniverseGraph(this.universe, newNodes, newEdges);
        result.scrubOrphanedEdges(); // In case only 1 node matched, the edge must be dropped
        return result;
    }

    // =========================================================================
    // EDGE FILTERING & NODE TOPOLOGY
    // =========================================================================

    @Override
    public EdgeSet edges(Node node, Direction direction) {
        UniverseNode un = validateLineage(node);
        long expectedModCount = this.universe.modCount();
        BitSet result = new BitSet();

        if (!this.activeNodes.get(un.id())) {
            return new UniverseEdgeSet(this.universe, result); // Return empty set
        }

        if (direction == Direction.OUT || direction == Direction.BOTH) {
            int[] out = this.universe.outboundEdges(un.id());
            if (out != null) {
                for (int edgeId : out) {
                    if (this.activeEdges.get(edgeId)) { result.set(edgeId); }
                }
            }
        }

        if (direction == Direction.IN || direction == Direction.BOTH) {
            int[] in = this.universe.inboundEdges(un.id());
            if (in != null) {
                for (int edgeId : in) {
                    if (this.activeEdges.get(edgeId)) { result.set(edgeId); }
                }
            }
        }

        checkTopologyState(expectedModCount);
        return new UniverseEdgeSet(this.universe, result);
    }

    private boolean hasActiveEdge(int[] universeEdges) {
        if (universeEdges == null) { return false; }
        for (int edgeId : universeEdges) {
            if (this.activeEdges.get(edgeId)) { return true; }
        }
        return false;
    }

    @Override
    public NodeSet leaves() {
        long expectedModCount = this.universe.modCount();
        BitSet result = new BitSet();
        // A leaf is an active node with NO active outbound edges
        for (int nodeId = this.activeNodes.nextSetBit(0); nodeId >= 0; nodeId = this.activeNodes.nextSetBit(nodeId + 1)) {
            if (!hasActiveEdge(this.universe.outboundEdges(nodeId))) {
                result.set(nodeId);
            }
        }
        checkTopologyState(expectedModCount);
        return new UniverseNodeSet(this.universe, result);
    }

    @Override
    public NodeSet roots() {
        long expectedModCount = this.universe.modCount();
        BitSet result = new BitSet();
        // A root is an active node with NO active inbound edges
        for (int nodeId = this.activeNodes.nextSetBit(0); nodeId >= 0; nodeId = this.activeNodes.nextSetBit(nodeId + 1)) {
            if (!hasActiveEdge(this.universe.inboundEdges(nodeId))) {
                result.set(nodeId);
            }
        }
        checkTopologyState(expectedModCount);
        return new UniverseNodeSet(this.universe, result);
    }

    @Override
    public NodeSet isolated() {
        long expectedModCount = this.universe.modCount();
        BitSet result = new BitSet();
        // An isolated node has NO active inbound AND NO active outbound edges
        for (int nodeId = this.activeNodes.nextSetBit(0); nodeId >= 0; nodeId = this.activeNodes.nextSetBit(nodeId + 1)) {
            boolean hasIn = hasActiveEdge(this.universe.inboundEdges(nodeId));
            boolean hasOut = hasActiveEdge(this.universe.outboundEdges(nodeId));

            if (!hasIn && !hasOut) {
                result.set(nodeId);
            }
        }
        checkTopologyState(expectedModCount);
        return new UniverseNodeSet(this.universe, result);
    }

    @Override
    public boolean adjacent(Node source, Node target) {
        UniverseNode uSource = validateLineage(source);
        UniverseNode uTarget = validateLineage(target);

        if (!this.activeNodes.get(uSource.id()) || !this.activeNodes.get(uTarget.id())) {
            return false;
        }

        long expectedModCount = this.universe.modCount();
        int[] out = this.universe.outboundEdges(uSource.id());
        if (out == null) { return false; }

        for (int edgeId : out) {
            // Check if the edge is active in this viewport, AND its target matches
            if (this.activeEdges.get(edgeId) && this.universe.edgeTarget(edgeId) == uTarget.id()) {
                checkTopologyState(expectedModCount);
                return true;
            }
        }
        checkTopologyState(expectedModCount);
        return false;
    }

    @Override
    public EdgeSet edges(Node source, Node target) {
        UniverseNode uSource = validateLineage(source);
        UniverseNode uTarget = validateLineage(target);

        if (!this.activeNodes.get(uSource.id()) || !this.activeNodes.get(uTarget.id())) {
            return EdgeSet.empty();
        }

        long expectedModCount = this.universe.modCount();
        int[] out = this.universe.outboundEdges(uSource.id());

        if (out == null || out.length == 0) {
            return EdgeSet.empty();
        }

        BitSet result = new BitSet();
        for (int edgeId : out) {
            if (this.activeEdges.get(edgeId) && this.universe.edgeTarget(edgeId) == uTarget.id()) {
                result.set(edgeId);
            }
        }
        checkTopologyState(expectedModCount);

        if (result.isEmpty()) {
            return EdgeSet.empty();
        } else if (result.cardinality() == 1) {
            return new UniverseImmutableSingletonEdgeSet(new UniverseEdge(this.universe, result.nextSetBit(0)));
        }

        return new UniverseEdgeSet(this.universe, result);
    }

    private int countActiveEdges(int[] universeEdges) {
        if (universeEdges == null) { return 0; }
        int count = 0;
        for (int edgeId : universeEdges) {
            if (this.activeEdges.get(edgeId)) { count++; }
        }
        return count;
    }

    @Override
    public int degree(Node node, Direction direction) {
        UniverseNode un = validateLineage(node);
        if (!this.activeNodes.get(un.id())) { return 0; }

        long expectedModCount = this.universe.modCount();
        int count = 0;
        if (direction == Direction.OUT || direction == Direction.BOTH) {
            count += countActiveEdges(this.universe.outboundEdges(un.id()));
        }
        if (direction == Direction.IN || direction == Direction.BOTH) {
            count += countActiveEdges(this.universe.inboundEdges(un.id()));
        }
        checkTopologyState(expectedModCount);
        return count;
    }

    // =========================================================================
    // PREDECESSORS & SUCCESSORS (1-STEP NEIGHBORHOOD)
    // =========================================================================

    @Override
    public NodeSet predecessors(Node origin) {
        Objects.requireNonNull(origin, "Node cannot be null");
        return predecessors(new UniverseNodeSet(this.universe, singleNodeBit(origin)));
    }

    @Override
    public NodeSet predecessors(Graph origin) {
        Objects.requireNonNull(origin, "Graph cannot be null");
        return predecessors(origin.nodes());
    }

    @Override
    public NodeSet predecessors(NodeSet origin) {
        Objects.requireNonNull(origin, "NodeSet cannot be null");
        long expectedModCount = this.universe.modCount();
        BitSet result = new BitSet();
        for (Node n : origin) {
            Objects.requireNonNull(n, "Node cannot be null");
            if (!(n instanceof UniverseNode un) || un.universe() != this.universe) {
                continue;
            }
            if (!this.activeNodes.get(un.id())) {
                continue;
            }

            int[] in = this.universe.inboundEdges(un.id());
            if (in != null) {
                for (int edgeId : in) {
                    if (this.activeEdges.get(edgeId)) {
                        result.set(this.universe.edgeSource(edgeId));
                    }
                }
            }
        }
        checkTopologyState(expectedModCount);
        return new UniverseNodeSet(this.universe, result);
    }

    @Override
    public NodeSet successors(Node origin) {
        Objects.requireNonNull(origin, "Node cannot be null");
        return successors(new UniverseNodeSet(this.universe, singleNodeBit(origin)));
    }

    @Override
    public NodeSet successors(Graph origin) {
        Objects.requireNonNull(origin, "Graph cannot be null");
        return successors(origin.nodes());
    }

    @Override
    public NodeSet successors(NodeSet origin) {
        Objects.requireNonNull(origin, "NodeSet cannot be null");
        long expectedModCount = this.universe.modCount();
        BitSet result = new BitSet();
        for (Node n : origin) {
            Objects.requireNonNull(n, "Node cannot be null");
            if (!(n instanceof UniverseNode un) || un.universe() != this.universe) {
                continue;
            }
            if (!this.activeNodes.get(un.id())) {
                continue;
            }

            int[] out = this.universe.outboundEdges(un.id());
            if (out != null) {
                for (int edgeId : out) {
                    if (this.activeEdges.get(edgeId)) {
                        result.set(this.universe.edgeTarget(edgeId));
                    }
                }
            }
        }
        checkTopologyState(expectedModCount);
        return new UniverseNodeSet(this.universe, result);
    }

    // =========================================================================
    // STEP TRAVERSALS (SUBGRAPH YIELDING)
    // =========================================================================

    private BitSet singleNodeBit(Node node) {
        Objects.requireNonNull(node, "Node cannot be null");
        BitSet b = new BitSet();
        if (node instanceof UniverseNode un && un.universe() == this.universe && this.activeNodes.get(un.id())) {
            b.set(un.id());
        }
        return b;
    }

    @Override
    public Graph forwardStep(Node origin) {
        Objects.requireNonNull(origin, "Node cannot be null");
        return forwardStep(new UniverseNodeSet(this.universe, singleNodeBit(origin)));
    }

    @Override
    public Graph forwardStep(Graph origin) {
        Objects.requireNonNull(origin, "Graph cannot be null");
        return forwardStep(origin.nodes());
    }

    @Override
    public Graph forwardStep(NodeSet origin) {
        Objects.requireNonNull(origin, "NodeSet cannot be null");
        long expectedModCount = this.universe.modCount();
        BitSet newNodes = new BitSet();
        BitSet newEdges = new BitSet();

        for (Node n : origin) {
            Objects.requireNonNull(n, "Node cannot be null");
            if (n instanceof UniverseNode un && un.universe() == this.universe && this.activeNodes.get(un.id())) {
                newNodes.set(un.id());

                int[] out = this.universe.outboundEdges(un.id());
                if (out != null) {
                    for (int edgeId : out) {
                        if (this.activeEdges.get(edgeId)) {
                            newEdges.set(edgeId);
                            newNodes.set(this.universe.edgeTarget(edgeId));
                        }
                    }
                }
            }
        }
        checkTopologyState(expectedModCount);
        return new UniverseGraph(this.universe, newNodes, newEdges);
    }

    @Override
    public Graph reverseStep(Node origin) {
        Objects.requireNonNull(origin, "Node cannot be null");
        return reverseStep(new UniverseNodeSet(this.universe, singleNodeBit(origin)));
    }

    @Override
    public Graph reverseStep(Graph origin) {
        Objects.requireNonNull(origin, "Graph cannot be null");
        return reverseStep(origin.nodes());
    }

    @Override
    public Graph reverseStep(NodeSet origin) {
        Objects.requireNonNull(origin, "NodeSet cannot be null");
        long expectedModCount = this.universe.modCount();
        BitSet newNodes = new BitSet();
        BitSet newEdges = new BitSet();

        for (Node n : origin) {
            Objects.requireNonNull(n, "Node cannot be null");
            if (n instanceof UniverseNode un && un.universe() == this.universe && this.activeNodes.get(un.id())) {
                newNodes.set(un.id());

                int[] in = this.universe.inboundEdges(un.id());
                if (in != null) {
                    for (int edgeId : in) {
                        if (this.activeEdges.get(edgeId)) {
                            newEdges.set(edgeId);
                            newNodes.set(this.universe.edgeSource(edgeId));
                        }
                    }
                }
            }
        }
        checkTopologyState(expectedModCount);
        return new UniverseGraph(this.universe, newNodes, newEdges);
    }

    @Override
    public Graph betweenStep(Node from, Node to) {
        Objects.requireNonNull(from, "Node 'from' cannot be null");
        Objects.requireNonNull(to, "Node 'to' cannot be null");
        return forwardStep(from).intersection(reverseStep(to));
    }

    @Override
    public Graph betweenStep(Graph from, Graph to) {
        Objects.requireNonNull(from, "Graph 'from' cannot be null");
        Objects.requireNonNull(to, "Graph 'to' cannot be null");
        return forwardStep(from).intersection(reverseStep(to));
    }

    @Override
    public Graph betweenStep(NodeSet from, NodeSet to) {
        Objects.requireNonNull(from, "NodeSet 'from' cannot be null");
        Objects.requireNonNull(to, "NodeSet 'to' cannot be null");
        return forwardStep(from).intersection(reverseStep(to));
    }

    // =========================================================================
    // TRANSITIVE BFS TRAVERSALS
    // =========================================================================

    @Override
    public Graph forward(Node origin) {
        Objects.requireNonNull(origin, "Node cannot be null");
        return forward(new UniverseNodeSet(this.universe, singleNodeBit(origin)));
    }

    @Override
    public Graph forward(Graph origin) {
        Objects.requireNonNull(origin, "Graph cannot be null");
        return forward(origin.nodes());
    }

    @Override
    public Graph forward(NodeSet origin) {
        Objects.requireNonNull(origin, "NodeSet cannot be null");
        long expectedModCount = this.universe.modCount();
        BitSet visitedNodes = new BitSet();
        BitSet visitedEdges = new BitSet();
        BitSet frontier = new BitSet();

        // 1. Seed the initial frontier
        for (Node n : origin) {
            Objects.requireNonNull(n, "Node cannot be null");
            if (n instanceof UniverseNode un && un.universe() == this.universe && this.activeNodes.get(un.id())) {
                frontier.set(un.id());
                visitedNodes.set(un.id());
            }
        }

        // 2. CPU-bound matrix BFS
        while (!frontier.isEmpty()) {
            BitSet nextFrontier = new BitSet();

            for (int nodeId = frontier.nextSetBit(0); nodeId >= 0; nodeId = frontier.nextSetBit(nodeId + 1)) {
                int[] out = this.universe.outboundEdges(nodeId);
                if (out != null) {
                    for (int edgeId : out) {
                        if (this.activeEdges.get(edgeId)) {
                            visitedEdges.set(edgeId);
                            int targetId = this.universe.edgeTarget(edgeId);

                            // If we haven't seen this target yet, mark it visited and add to next frontier
                            if (!visitedNodes.get(targetId)) {
                                visitedNodes.set(targetId);
                                nextFrontier.set(targetId);
                            }
                        }
                    }
                }
            }
            checkTopologyState(expectedModCount);
            frontier = nextFrontier;
        }

        checkTopologyState(expectedModCount);
        return new UniverseGraph(this.universe, visitedNodes, visitedEdges);
    }

    @Override
    public Graph reverse(Node origin) {
        Objects.requireNonNull(origin, "Node cannot be null");
        return reverse(new UniverseNodeSet(this.universe, singleNodeBit(origin)));
    }

    @Override
    public Graph reverse(Graph origin) {
        Objects.requireNonNull(origin, "Graph cannot be null");
        return reverse(origin.nodes());
    }

    @Override
    public Graph reverse(NodeSet origin) {
        Objects.requireNonNull(origin, "NodeSet cannot be null");
        long expectedModCount = this.universe.modCount();
        BitSet visitedNodes = new BitSet();
        BitSet visitedEdges = new BitSet();
        BitSet frontier = new BitSet();

        for (Node n : origin) {
            Objects.requireNonNull(n, "Node cannot be null");
            if (n instanceof UniverseNode un && un.universe() == this.universe && this.activeNodes.get(un.id())) {
                frontier.set(un.id());
                visitedNodes.set(un.id());
            }
        }

        while (!frontier.isEmpty()) {
            BitSet nextFrontier = new BitSet();

            for (int nodeId = frontier.nextSetBit(0); nodeId >= 0; nodeId = frontier.nextSetBit(nodeId + 1)) {
                int[] in = this.universe.inboundEdges(nodeId);
                if (in != null) {
                    for (int edgeId : in) {
                        if (this.activeEdges.get(edgeId)) {
                            visitedEdges.set(edgeId);
                            int sourceId = this.universe.edgeSource(edgeId);

                            if (!visitedNodes.get(sourceId)) {
                                visitedNodes.set(sourceId);
                                nextFrontier.set(sourceId);
                            }
                        }
                    }
                }
            }
            checkTopologyState(expectedModCount);
            frontier = nextFrontier;
        }

        checkTopologyState(expectedModCount);
        return new UniverseGraph(this.universe, visitedNodes, visitedEdges);
    }

    @Override
    public Graph between(Node from, Node to) {
        Objects.requireNonNull(from, "Node 'from' cannot be null");
        Objects.requireNonNull(to, "Node 'to' cannot be null");
        return forward(from).intersection(reverse(to));
    }

    @Override
    public Graph between(Graph from, Graph to) {
        Objects.requireNonNull(from, "Graph 'from' cannot be null");
        Objects.requireNonNull(to, "Graph 'to' cannot be null");
        return forward(from).intersection(reverse(to));
    }

    @Override
    public Graph between(NodeSet from, NodeSet to) {
        Objects.requireNonNull(from, "NodeSet 'from' cannot be null");
        Objects.requireNonNull(to, "NodeSet 'to' cannot be null");
        return forward(from).intersection(reverse(to));
    }

    // =========================================================================
    // GRAPH INDUCTION
    // =========================================================================

    @Override
    public Graph induce(Edge edge) {
        Objects.requireNonNull(edge, "Edge cannot be null");
        UniverseEdge ue = validateLineage(edge);
        BitSet newNodes = (BitSet) this.activeNodes.clone();
        BitSet newEdges = new BitSet();

        int sourceId = this.universe.edgeSource(ue.id());
        int targetId = this.universe.edgeTarget(ue.id());

        if (this.activeNodes.get(sourceId) && this.activeNodes.get(targetId)) {
            newEdges.set(ue.id());
        }

        return new UniverseGraph(this.universe, newNodes, newEdges);
    }

    @Override
    public Graph induce(Graph graph) {
        Objects.requireNonNull(graph, "Graph cannot be null");
        UniverseGraph other = validateLineage(graph);
        BitSet newNodes = (BitSet) this.activeNodes.clone();
        BitSet newEdges = new BitSet();

        // Fast-path: Directly iterate the other graph's BitSet mask
        for (int edgeId = other.activeEdges.nextSetBit(0); edgeId >= 0; edgeId = other.activeEdges.nextSetBit(edgeId + 1)) {
            int sourceId = this.universe.edgeSource(edgeId);
            int targetId = this.universe.edgeTarget(edgeId);

            if (this.activeNodes.get(sourceId) && this.activeNodes.get(targetId)) {
                newEdges.set(edgeId);
            }
        }

        return new UniverseGraph(this.universe, newNodes, newEdges);
    }

    @Override
    public Graph induce(EdgeSet edges) {
        Objects.requireNonNull(edges, "EdgeSet cannot be null");
        BitSet newNodes = (BitSet) this.activeNodes.clone();
        BitSet newEdges = new BitSet();

        // Slow-path: Iterate the generic EdgeSet interface
        for (Edge e : edges) {
            Objects.requireNonNull(e, "Edge cannot be null");
            if (e instanceof UniverseEdge ue && ue.universe() == this.universe) {
                int sourceId = this.universe.edgeSource(ue.id());
                int targetId = this.universe.edgeTarget(ue.id());

                if (this.activeNodes.get(sourceId) && this.activeNodes.get(targetId)) {
                    newEdges.set(ue.id());
                }
            }
        }

        return new UniverseGraph(this.universe, newNodes, newEdges);
    }
}
