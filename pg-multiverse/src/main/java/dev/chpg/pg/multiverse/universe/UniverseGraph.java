package dev.chpg.pg.multiverse.universe;

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.EdgeSet;
import dev.chpg.pg.api.Graph;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.Node.NodeDirection;
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
public final class UniverseGraph implements Graph, UniverseView {

    private final Universe universe;
    private final BitSet activeNodes;
    private final BitSet activeEdges;

    /**
     * Constructs a new, empty viewport over the given Universe.
     */
    public UniverseGraph(Universe universe) {
        this.universe = Objects.requireNonNull(universe, "Universe cannot be null");
        this.activeNodes = new BitSet();
        this.activeEdges = new BitSet();
    }

    /**
     * Constructs a viewport backed by existing masks.
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
    // STUBBED: SET ALGEBRA & TRAVERSALS
    // =========================================================================

    @Override public EdgeSet edges(Node node, NodeDirection direction) { throw new UnsupportedOperationException("Not yet implemented"); }
    @Override public NodeSet leaves() { throw new UnsupportedOperationException("Not yet implemented"); }
    @Override public NodeSet roots() { throw new UnsupportedOperationException("Not yet implemented"); }
    @Override public NodeSet isolated() { throw new UnsupportedOperationException("Not yet implemented"); }
    @Override public NodeSet predecessors(Node origin) { throw new UnsupportedOperationException("Not yet implemented"); }
    @Override public NodeSet predecessors(Graph origin) { throw new UnsupportedOperationException("Not yet implemented"); }
    @Override public NodeSet predecessors(NodeSet origin) { throw new UnsupportedOperationException("Not yet implemented"); }
    @Override public NodeSet successors(Node origin) { throw new UnsupportedOperationException("Not yet implemented"); }
    @Override public NodeSet successors(Graph origin) { throw new UnsupportedOperationException("Not yet implemented"); }
    @Override public NodeSet successors(NodeSet origin) { throw new UnsupportedOperationException("Not yet implemented"); }
    @Override public Graph forwardStep(Node origin) { throw new UnsupportedOperationException("Not yet implemented"); }
    @Override public Graph forwardStep(Graph origin) { throw new UnsupportedOperationException("Not yet implemented"); }
    @Override public Graph forwardStep(NodeSet origin) { throw new UnsupportedOperationException("Not yet implemented"); }
    @Override public Graph reverseStep(Node origin) { throw new UnsupportedOperationException("Not yet implemented"); }
    @Override public Graph reverseStep(Graph origin) { throw new UnsupportedOperationException("Not yet implemented"); }
    @Override public Graph reverseStep(NodeSet origin) { throw new UnsupportedOperationException("Not yet implemented"); }
    @Override public Graph union(Node node) { throw new UnsupportedOperationException("Not yet implemented"); }
    @Override public Graph union(Edge edge) { throw new UnsupportedOperationException("Not yet implemented"); }
    @Override public Graph union(Graph graph) { throw new UnsupportedOperationException("Not yet implemented"); }
    @Override public Graph difference(Node node) { throw new UnsupportedOperationException("Not yet implemented"); }
    @Override public Graph difference(Edge edge) { throw new UnsupportedOperationException("Not yet implemented"); }
    @Override public Graph difference(Graph graph) { throw new UnsupportedOperationException("Not yet implemented"); }
    @Override public Graph differenceEdges(Edge edge) { throw new UnsupportedOperationException("Not yet implemented"); }
    @Override public Graph differenceEdges(Graph graph) { throw new UnsupportedOperationException("Not yet implemented"); }
    @Override public Graph intersection(Node node) { throw new UnsupportedOperationException("Not yet implemented"); }
    @Override public Graph intersection(Edge edge) { throw new UnsupportedOperationException("Not yet implemented"); }
    @Override public Graph intersection(Graph graph) { throw new UnsupportedOperationException("Not yet implemented"); }
    @Override public Graph betweenStep(Node from, Node to) { throw new UnsupportedOperationException("Not yet implemented"); }
    @Override public Graph betweenStep(Graph from, Graph to) { throw new UnsupportedOperationException("Not yet implemented"); }
    @Override public Graph betweenStep(NodeSet from, NodeSet to) { throw new UnsupportedOperationException("Not yet implemented"); }
    @Override public Graph between(Node from, Node to) { throw new UnsupportedOperationException("Not yet implemented"); }
    @Override public Graph between(Graph from, Graph to) { throw new UnsupportedOperationException("Not yet implemented"); }
    @Override public Graph between(NodeSet from, NodeSet to) { throw new UnsupportedOperationException("Not yet implemented"); }
    @Override public Graph forward(Node origin) { throw new UnsupportedOperationException("Not yet implemented"); }
    @Override public Graph forward(Graph origin) { throw new UnsupportedOperationException("Not yet implemented"); }
    @Override public Graph forward(NodeSet origin) { throw new UnsupportedOperationException("Not yet implemented"); }
    @Override public Graph reverse(Node origin) { throw new UnsupportedOperationException("Not yet implemented"); }
    @Override public Graph reverse(Graph origin) { throw new UnsupportedOperationException("Not yet implemented"); }
    @Override public Graph reverse(NodeSet origin) { throw new UnsupportedOperationException("Not yet implemented"); }
    @Override public Graph induce(Edge edge) { throw new UnsupportedOperationException("Not yet implemented"); }
    @Override public Graph induce(Graph graph) { throw new UnsupportedOperationException("Not yet implemented"); }
    @Override public Graph induce(EdgeSet edges) { throw new UnsupportedOperationException("Not yet implemented"); }
    @Override public boolean adjacent(Node source, Node target) { throw new UnsupportedOperationException("Not yet implemented"); }
    @Override public EdgeSet edges(Node source, Node target) { throw new UnsupportedOperationException("Not yet implemented"); }
    @Override public int degree(Node node, NodeDirection direction) { throw new UnsupportedOperationException("Not yet implemented"); }
}
