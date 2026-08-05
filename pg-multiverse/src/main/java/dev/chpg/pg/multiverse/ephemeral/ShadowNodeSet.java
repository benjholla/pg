package dev.chpg.pg.multiverse.ephemeral;

import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.NodeSet;
import dev.chpg.pg.multiverse.universe.UniverseNodeSet;

import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class ShadowNodeSet implements NodeSet {
    private final EphemeralGraph transactionContext;
    private final NodeSet backingSet;       // The core engine baseline
    private final Set<Node> localAdds;      // The transaction additions (ceiling)

    // Standard constructor for traversing universe topology
    public ShadowNodeSet(EphemeralGraph context, NodeSet backingSet) {
        this(context, backingSet, Collections.emptySet());
    }

    // Composite constructor for complex algebra and graph captures
    public ShadowNodeSet(EphemeralGraph context, NodeSet backingSet, Set<Node> localAdds) {
        this.transactionContext = context;
        this.backingSet = backingSet;
        this.localAdds = localAdds;
    }

    // --- The Strict Algebra Firewall ---
    private NodeSet unwrapForAlgebra(Collection<? extends Node> other) {
        Objects.requireNonNull(other, "Collection cannot be null");

        if (other instanceof ShadowNodeSet shadow) {
            if (shadow.transactionContext.universe() != this.transactionContext.universe()) {
                throw new IllegalArgumentException("Cross-universe contamination: Shadow sets belong to different universes.");
            }
            return shadow.backingSet;
        }

        if (other instanceof UniverseNodeSet universeSet) {
            if (universeSet.universe() != this.transactionContext.universe()) {
                throw new IllegalArgumentException("Cross-universe contamination: Universe sets do not match.");
            }
            return universeSet;
        }

        if (other instanceof EphemeralNodeSet ||
            other instanceof EphemeralImmutableNodeSet ||
            other instanceof EphemeralImmutableSingletonNodeSet ||
            other instanceof EphemeralUnmodifiableLiveNodeSet ||
            other.isEmpty()) {
            return new UniverseNodeSet(this.transactionContext.universe(), new java.util.BitSet());
        }

        // FAIL FAST: No more silent filtering or fallback arrays
        throw new IllegalArgumentException(
            "Strict algebra whitelist violation: Expected ShadowNodeSet or UniverseNodeSet, got " + other.getClass().getSimpleName()
        );
    }

    // Add this helper method right below unwrapForAlgebra
    @SuppressWarnings("unchecked")
    private Set<Node> extractLocalDelta(Collection<? extends Node> other) {
        if (other instanceof ShadowNodeSet shadow) {
            return shadow.localAdds;
        }
        if (other instanceof UniverseNodeSet) {
            return Collections.emptySet(); // Core baselines have no local delta
        }
        // If it survived unwrapForAlgebra, it is a valid, pure-local Ephemeral set
        return new HashSet<>(other);
    }

    @Override
    public NodeSet union(Collection<? extends Node> other) {
        NodeSet unwrappedOther = unwrapForAlgebra(other);

        // 1. O(1) Bitwise Algebra on the core engine
        NodeSet newBacking = this.backingSet.union(unwrappedOther);

        // 2. Set Algebra on the local delta
        Set<Node> combinedLocalAdds = new HashSet<>(this.localAdds);
        combinedLocalAdds.addAll(extractLocalDelta(other));

        // 3. Wrap it up (ShadowNodeSet intrinsically masks tombstones, no manual subtraction needed!)
        return new ShadowNodeSet(this.transactionContext, newBacking, combinedLocalAdds);
    }

    @Override
    public NodeSet difference(Collection<? extends Node> other) {
        NodeSet unwrappedOther = unwrapForAlgebra(other);

        // 1. O(1) Bitwise Algebra on the core engine
        NodeSet newBacking = this.backingSet.difference(unwrappedOther);

        // 2. Set Algebra on the local delta
        Set<Node> combinedLocalAdds = new HashSet<>(this.localAdds);
        combinedLocalAdds.removeAll(extractLocalDelta(other));

        return new ShadowNodeSet(this.transactionContext, newBacking, combinedLocalAdds);
    }

    @Override
    public NodeSet intersect(Collection<? extends Node> other) {
        NodeSet unwrappedOther = unwrapForAlgebra(other);

        // 1. O(1) Bitwise Algebra on the core engine
        NodeSet newBacking = this.backingSet.intersect(unwrappedOther);

        // 2. Set Algebra on the local delta
        Set<Node> combinedLocalAdds = new HashSet<>();
        Set<Node> otherLocal = extractLocalDelta(other);

        for (Node local : this.localAdds) {
            if (otherLocal.contains(local)) {
                combinedLocalAdds.add(local);
            }
        }

        return new ShadowNodeSet(this.transactionContext, newBacking, combinedLocalAdds);
    }

    @Override
    public Iterator<Node> iterator() {
        Iterator<Node> backingIter = new ShadowNodeIterator(transactionContext, backingSet.iterator());
        return new Iterator<Node>() {
            private Node nextNode = null;
            private boolean backingDone = false;
            private final Iterator<Node> localIter = localAdds.iterator();

            private void advance() {
                if (nextNode != null) { return; }

                while (backingIter.hasNext()) {
                    Node potential = backingIter.next();
                    // Perfect Tombstone Masking
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
                if (nextNode == null) { throw new java.util.NoSuchElementException(); }
                Node toReturn = nextNode;
                nextNode = null;
                return toReturn;
            }
        };
    }

    @Override
    public int size() {
        // Dynamically compute the size minus active tombstones
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

        if (localAdds.contains(node)) { return true; }

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
        if (size() == 1) { return new dev.chpg.pg.api.GenericImmutableNodeSet(Collections.singleton(one().get())); }
        return new dev.chpg.pg.api.GenericImmutableNodeSet(this);
    }

    @Override
    public boolean add(Node e) { throw new UnsupportedOperationException("Removals/additions must be routed explicitly through EphemeralGraph APIs."); }

    @Override
    public boolean remove(Object o) { throw new UnsupportedOperationException("Removals/additions must be routed explicitly through EphemeralGraph APIs."); }

    @Override
    public boolean addAll(Collection<? extends Node> c) { throw new UnsupportedOperationException("Removals/additions must be routed explicitly through EphemeralGraph APIs."); }

    @Override
    public boolean removeAll(Collection<?> c) { throw new UnsupportedOperationException("Removals/additions must be routed explicitly through EphemeralGraph APIs."); }

    @Override
    public boolean retainAll(Collection<?> c) { throw new UnsupportedOperationException("Removals/additions must be routed explicitly through EphemeralGraph APIs."); }

    @Override
    public void clear() { throw new UnsupportedOperationException("Removals/additions must be routed explicitly through EphemeralGraph APIs."); }

    @Override
    public Object[] toArray() { return materialize().toArray(); }

    @Override
    public <T> T[] toArray(T[] a) { return materialize().toArray(a); }
}
