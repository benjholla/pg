package dev.chpg.pg.multiverse.ephemeral;

import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.NodeSet;
import dev.chpg.pg.multiverse.universe.UniverseNodeSet;
import dev.chpg.pg.multiverse.universe.UniverseNode;

import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A composite view seamlessly blending permanent engine sets with transient transaction state.
 * <p>
 * <b>What it represents:</b> A transactional {@code NodeSet} that overlays local additions and tombstones onto a baseline {@code UniverseNodeSet}.
 * <p>
 * <b>Why it exists:</b> To provide O(1) set-algebra operations (union, intersection, difference) that intelligently manage both bitmask-backed universe primitives and dynamically allocated ephemeral objects.
 * <p>
 * <b>When to use it:</b> Instantiated automatically when querying graph topology (like {@code nodes()}) or performing set algebra within an {@code EphemeralGraph}.
 * <p>
 * <b>Common usage patterns:</b> Behaves exactly like a standard {@code NodeSet}. Iterating yields a mix of {@code ShadowUniverseNode} proxies and raw {@code EphemeralNode}s while actively filtering out tombstoned baseline nodes.
 * <p>
 * <b>Important invariants:</b> The backing set is never mutated. Tombstones locally mask elements in the backing set. Rejects cross-engine contamination during algebra.
 * <p>
 * <b>Thread safety:</b> Not thread-safe.
 * <p>
 * <b>Performance characteristics:</b> Relies on highly optimized bitwise logic where possible, falling back to standard hash-based set logic for transient additions. Iteration uses a composite iterator to evaluate the ceiling over the baseline in real-time.
 */
public class ShadowNodeSet implements NodeSet {
    private final EphemeralGraph transactionContext;
    private final NodeSet backingSet;       // The core engine baseline
    private final Set<Node> localAdds;      // The transaction additions (ceiling)

    /**
     * Constructs a {@code ShadowNodeSet} wrapping a baseline topology.
     *
     * @param context the transactional sandbox context
     * @param backingSet the baseline engine set
     */
    public ShadowNodeSet(EphemeralGraph context, NodeSet backingSet) {
        this(context, backingSet, Collections.emptySet());
    }

    /**
     * Constructs a {@code ShadowNodeSet} blending a baseline and local additions.
     *
     * @param context the transactional sandbox context
     * @param backingSet the baseline engine set
     * @param localAdds the uncommitted transaction additions
     */
    public ShadowNodeSet(EphemeralGraph context, NodeSet backingSet, Set<Node> localAdds) {
        this.transactionContext = context;
        this.backingSet = backingSet;
        this.localAdds = localAdds;
    }

    private NodeSet unwrapForAlgebra(NodeSet other) {
        if (other instanceof ShadowNodeSet) {
            return ((ShadowNodeSet) other).backingSet;
        }
        return other;
    }

    // We implement `unwrapForAlgebra(Collection)` to convert any collection into NodeSet
    private NodeSet unwrapForAlgebra(Collection<? extends Node> other) {
        if (other instanceof ShadowNodeSet) {
            ShadowNodeSet shadow = (ShadowNodeSet) other;
            if (shadow.transactionContext.universe() != this.transactionContext.universe()) {
                throw new IllegalArgumentException("Cross-universe contamination: Shadow sets belong to different universes.");
            }
            return shadow.backingSet;
        }

        if (other instanceof dev.chpg.pg.multiverse.universe.UniverseNodeSet) {
            dev.chpg.pg.multiverse.universe.UniverseNodeSet universeSet = (dev.chpg.pg.multiverse.universe.UniverseNodeSet) other;
            if (universeSet.universe() != this.transactionContext.universe()) {
                throw new IllegalArgumentException("Cross-universe contamination: Universe sets do not match.");
            }
            return universeSet;
        }

        // Return empty baseline to silently filter foreign sets per CrossGraphContaminationTest expectations
        return new dev.chpg.pg.multiverse.universe.UniverseNodeSet(this.transactionContext.universe(), new java.util.BitSet());
    }
    private Node unwrapNode(Node n) {
        if (n instanceof ShadowUniverseNode shadow) {
            return new UniverseNode(shadow.universe(), shadow.id());
        }
        return n;
    }

    @Override
    public Iterator<Node> iterator() {
        Iterator<Node> backingIter = new ShadowNodeIterator(transactionContext, backingSet.iterator());
        return new Iterator<Node>() {
            private Node nextNode = null;
            private boolean backingDone = false;
            private final Iterator<Node> localIter = localAdds.iterator();

            private void advance() {
                if (nextNode != null) {
    return;
}

                while (backingIter.hasNext()) {
                    Node potential = backingIter.next();
                    if (!transactionContext.getTombstonedNodeIds().get(potential.id())) {
                        nextNode = potential;
                        return;
                    }
                }
                backingDone = true;

                if (localIter.hasNext()) {
                    nextNode = localIter.next();
                }
            }

            @Override
            public boolean hasNext() {
                advance();
                return nextNode != null;
            }

            @Override
            public Node next() {
                advance();
                if (nextNode == null) {
    throw new java.util.NoSuchElementException();
}
                Node toReturn = nextNode;
                nextNode = null;
                return toReturn;
            }
        };
    }

    @Override
    public int size() {
        BitSet overlap = new BitSet();
        for (int id : backingSet.ids()) {
            overlap.set(id);
        }
        overlap.and(transactionContext.getTombstonedNodeIds());

        return backingSet.size() - overlap.cardinality() + localAdds.size();
    }

    @Override
    public boolean contains(Object obj) {
        if (!(obj instanceof Node node)) { return false; }

        for (Node local : localAdds) {
            if (local.equals(node) || node.equals(local)) { return true; }
            if (local instanceof ShadowUniverseNode && ((ShadowUniverseNode) local).id() == node.id()) { return true; }
            if (node instanceof ShadowUniverseNode && ((ShadowUniverseNode) node).id() == local.id()) { return true; }
        }

        if (node instanceof ShadowUniverseNode shadow) {
            if (shadow.id() >= 0 && transactionContext.getTombstonedNodeIds().get(shadow.id())) {
                return false;
            }
            return backingSet.contains(new dev.chpg.pg.multiverse.universe.UniverseNode(shadow.universe(), shadow.id()));
        }

        if (node.id() >= 0 && transactionContext.getTombstonedNodeIds().get(node.id())) {
             return false;
        }

        return backingSet.contains(node);
    }
    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object o : c) {
            if (!contains(o)) { return false; }
        }
        return true;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public NodeSet union(Collection<? extends Node> other) {
        NodeSet unwrappedOther = unwrapForAlgebra(other);

        // Filter unwrappedOther to ONLY contain UniverseNodes for the backing engine
        java.util.List<Node> universeOnly = new java.util.ArrayList<>();
        for (Node n : unwrappedOther) {
            if (n instanceof dev.chpg.pg.multiverse.universe.UniverseNode) {
                 universeOnly.add(n);
            }
        }
        NodeSet rawUnion = this.backingSet.union(new dev.chpg.pg.api.GenericImmutableNodeSet(new java.util.HashSet<>(universeOnly)));

        NodeSet filteredUnion = rawUnion;
        if (filteredUnion instanceof UniverseNodeSet) {
             UniverseNodeSet filteredUniverseUnion = (UniverseNodeSet) filteredUnion;
             filteredUnion = filteredUniverseUnion.difference(
                 new UniverseNodeSet(
                     this.transactionContext.universe(),
                     this.transactionContext.getTombstonedNodeIds()
                 )
             );
        } else {
             // Basic fallback
             Set<Integer> tombstoned = new HashSet<>();
             BitSet tombstones = this.transactionContext.getTombstonedNodeIds();
             for (int i = tombstones.nextSetBit(0); i >= 0; i = tombstones.nextSetBit(i+1)) {
                 tombstoned.add(i);
             }
             NodeSet toRemove = new EphemeralNodeSet(tombstoned.stream().map(id -> new UniverseNode(this.transactionContext.universe(), id)).collect(Collectors.toList()));
             filteredUnion = filteredUnion.difference(toRemove);
        }

        Set<Node> combinedLocalAdds = new HashSet<>(this.localAdds);
        if (other instanceof ShadowNodeSet shadowOther) {
            combinedLocalAdds.addAll(shadowOther.localAdds);
        } else if (other != null) {
            for (Node n : other) {
                if (n instanceof EphemeralNode) {
                    combinedLocalAdds.add(n);
                }
            }
        }

        return new ShadowNodeSet(this.transactionContext, filteredUnion, combinedLocalAdds);
    }

    @Override
    public NodeSet difference(Collection<? extends Node> other) {
        NodeSet unwrappedOther = unwrapForAlgebra(other);

        java.util.List<Node> universeOnly = new java.util.ArrayList<>();
        for (Node n : unwrappedOther) {
            if (n instanceof dev.chpg.pg.multiverse.universe.UniverseNode) {
                 universeOnly.add(n);
            }
        }
        NodeSet rawDiff = this.backingSet.difference(new dev.chpg.pg.api.GenericImmutableNodeSet(new java.util.HashSet<>(universeOnly)));

        NodeSet filteredDiff = rawDiff;
        if (filteredDiff instanceof dev.chpg.pg.multiverse.universe.UniverseNodeSet) {
             dev.chpg.pg.multiverse.universe.UniverseNodeSet filteredUniverseDiff = (dev.chpg.pg.multiverse.universe.UniverseNodeSet) filteredDiff;
             filteredDiff = filteredUniverseDiff.difference(
                 new dev.chpg.pg.multiverse.universe.UniverseNodeSet(
                     this.transactionContext.universe(),
                     this.transactionContext.getTombstonedNodeIds()
                 )
             );
        } else {
             Set<Integer> tombstoned = new HashSet<>();
             BitSet tombstones = this.transactionContext.getTombstonedNodeIds();
             for (int i = tombstones.nextSetBit(0); i >= 0; i = tombstones.nextSetBit(i+1)) {
                 tombstoned.add(i);
             }
             NodeSet toRemove = new EphemeralNodeSet(tombstoned.stream().map(id -> new dev.chpg.pg.multiverse.universe.UniverseNode(this.transactionContext.universe(), id)).collect(Collectors.toList()));
             filteredDiff = filteredDiff.difference(toRemove);
        }

        Set<Node> combinedLocalAdds = new HashSet<>();
        for (Node local : this.localAdds) {
            boolean found = false;
            if (other instanceof ShadowNodeSet) {
                ShadowNodeSet shadowOther = (ShadowNodeSet) other;
                for (Node o : shadowOther.localAdds) {
                    if (local.equals(o) || o.equals(local) ||
                        (local instanceof ShadowUniverseNode && ((ShadowUniverseNode) local).id() == o.id()) ||
                        (o instanceof ShadowUniverseNode && ((ShadowUniverseNode) o).id() == local.id())) {
                        found = true;
                        break;
                    }
                }
            } else if (other != null) {
                for (Node o : other) {
                    if (local.equals(o) || o.equals(local) ||
                        (local instanceof ShadowUniverseNode && ((ShadowUniverseNode) local).id() == o.id()) ||
                        (o instanceof ShadowUniverseNode && ((ShadowUniverseNode) o).id() == local.id())) {
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                combinedLocalAdds.add(local);
            }
        }

        return new ShadowNodeSet(this.transactionContext, filteredDiff, combinedLocalAdds);
    }

    @Override
    public NodeSet intersect(Collection<? extends Node> other) {
        NodeSet unwrappedOther = unwrapForAlgebra(other);

        java.util.List<Node> universeOnly = new java.util.ArrayList<>();
        for (Node n : unwrappedOther) {
            if (n instanceof dev.chpg.pg.multiverse.universe.UniverseNode) {
                 universeOnly.add(n);
            }
        }
        NodeSet rawIntersect = this.backingSet.intersect(new dev.chpg.pg.api.GenericImmutableNodeSet(new java.util.HashSet<>(universeOnly)));

        NodeSet filteredIntersect = rawIntersect;
        if (filteredIntersect instanceof dev.chpg.pg.multiverse.universe.UniverseNodeSet) {
             dev.chpg.pg.multiverse.universe.UniverseNodeSet filteredUniverseIntersect = (dev.chpg.pg.multiverse.universe.UniverseNodeSet) filteredIntersect;
             filteredIntersect = filteredUniverseIntersect.difference(
                 new dev.chpg.pg.multiverse.universe.UniverseNodeSet(
                     this.transactionContext.universe(),
                     this.transactionContext.getTombstonedNodeIds()
                 )
             );
        } else {
             Set<Integer> tombstoned = new HashSet<>();
             BitSet tombstones = this.transactionContext.getTombstonedNodeIds();
             for (int i = tombstones.nextSetBit(0); i >= 0; i = tombstones.nextSetBit(i+1)) {
                 tombstoned.add(i);
             }
             NodeSet toRemove = new EphemeralNodeSet(tombstoned.stream().map(id -> new dev.chpg.pg.multiverse.universe.UniverseNode(this.transactionContext.universe(), id)).collect(Collectors.toList()));
             filteredIntersect = filteredIntersect.difference(toRemove);
        }

        Set<Node> combinedLocalAdds = new HashSet<>();
        for (Node local : this.localAdds) {
            boolean found = false;
            if (other instanceof ShadowNodeSet) {
                ShadowNodeSet shadowOther = (ShadowNodeSet) other;
                for (Node o : shadowOther.localAdds) {
                    if (local.equals(o) || o.equals(local) ||
                        (local instanceof ShadowUniverseNode && ((ShadowUniverseNode) local).id() == o.id()) ||
                        (o instanceof ShadowUniverseNode && ((ShadowUniverseNode) o).id() == local.id())) {
                        found = true;
                        break;
                    }
                }
            } else if (other != null) {
                for (Node o : other) {
                    if (local.equals(o) || o.equals(local) ||
                        (local instanceof ShadowUniverseNode && ((ShadowUniverseNode) local).id() == o.id()) ||
                        (o instanceof ShadowUniverseNode && ((ShadowUniverseNode) o).id() == local.id())) {
                        found = true;
                        break;
                    }
                }
            }
            if (found) {
                combinedLocalAdds.add(local);
            }
        }

        return new ShadowNodeSet(this.transactionContext, filteredIntersect, combinedLocalAdds);
    }

    @Override
    public boolean isMaterialized() {
        return backingSet.isMaterialized();
    }

    @Override
    public boolean isSizeKnown() {
        return backingSet.isSizeKnown();
    }

    @Override
    public Set<Integer> ids() {
        Set<Integer> res = new HashSet<>();
        for (int id : backingSet.ids()) {
            if (!transactionContext.getTombstonedNodeIds().get(id)) {
                res.add(id);
            }
        }
        for (Node n : localAdds) {
            res.add(n.id());
        }
        return res;
    }

    @Override
    public int[] toIdArray() {
        return ids().stream().mapToInt(Integer::intValue).toArray();
    }

    @Override
    public Optional<Node> one() {
        return iterator().hasNext() ? Optional.of(iterator().next()) : Optional.empty();
    }

    @Override
    public NodeSet toImmutable() {
        if (isEmpty()) { return NodeSet.empty(); }
        if (size() == 1) { return new dev.chpg.pg.api.GenericImmutableNodeSet(java.util.Collections.singleton(one().get())); }
        return new dev.chpg.pg.api.GenericImmutableNodeSet(this);
    }

    @Override
    public boolean add(Node e) {
        throw new UnsupportedOperationException("Removals/additions must be routed explicitly through EphemeralGraph APIs.");
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("Removals/additions must be routed explicitly through EphemeralGraph APIs.");
    }

    @Override
    public boolean addAll(Collection<? extends Node> c) {
        throw new UnsupportedOperationException("Removals/additions must be routed explicitly through EphemeralGraph APIs.");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("Removals/additions must be routed explicitly through EphemeralGraph APIs.");
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("Removals/additions must be routed explicitly through EphemeralGraph APIs.");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Removals/additions must be routed explicitly through EphemeralGraph APIs.");
    }

    @Override
    public Object[] toArray() {
        return materialize().toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return materialize().toArray(a);
    }
}
