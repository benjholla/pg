package dev.chpg.pg.multiverse.ephemeral;

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.EdgeSet;
import dev.chpg.pg.multiverse.universe.UniverseEdgeSet;

import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class ShadowEdgeSet implements EdgeSet {
    private final EphemeralGraph transactionContext;
    private final EdgeSet backingSet;       // The core engine baseline
    private final Set<Edge> localAdds;      // The transaction additions (ceiling)

    // Standard constructor for traversing universe topology
    public ShadowEdgeSet(EphemeralGraph context, EdgeSet backingSet) {
        this(context, backingSet, Collections.emptySet());
    }

    // Composite constructor for complex algebra and graph captures
    public ShadowEdgeSet(EphemeralGraph context, EdgeSet backingSet, Set<Edge> localAdds) {
        this.transactionContext = context;
        this.backingSet = backingSet;
        this.localAdds = localAdds;
    }

    // --- The Strict Algebra Firewall ---
    private EdgeSet unwrapForAlgebra(Collection<? extends Edge> other) {
        Objects.requireNonNull(other, "Collection cannot be null");

        if (other instanceof ShadowEdgeSet shadow) {
            if (shadow.transactionContext.universe() != this.transactionContext.universe()) {
                throw new IllegalArgumentException("Cross-universe contamination: Shadow sets belong to different universes.");
            }
            return shadow.backingSet;
        }

        if (other instanceof UniverseEdgeSet universeSet) {
            if (universeSet.universe() != this.transactionContext.universe()) {
                throw new IllegalArgumentException("Cross-universe contamination: Universe sets do not match.");
            }
            return universeSet;
        }

        // FAIL FAST: No more silent filtering or fallback arrays
        throw new IllegalArgumentException(
            "Strict algebra whitelist violation: Expected ShadowEdgeSet or UniverseEdgeSet, got " + other.getClass().getSimpleName()
        );
    }

    @Override
    public EdgeSet union(Collection<? extends Edge> other) {
        EdgeSet unwrappedOther = unwrapForAlgebra(other);

        // 1. O(1) Bitwise Algebra on the core engine
        EdgeSet newBacking = this.backingSet.union(unwrappedOther);

        // 2. Set Algebra on the local delta
        Set<Edge> combinedLocalAdds = new HashSet<>(this.localAdds);
        if (other instanceof ShadowEdgeSet shadowOther) {
            combinedLocalAdds.addAll(shadowOther.localAdds);
        }

        // 3. Wrap it up (ShadowEdgeSet intrinsically masks tombstones, no manual subtraction needed!)
        return new ShadowEdgeSet(this.transactionContext, newBacking, combinedLocalAdds);
    }

    @Override
    public EdgeSet difference(Collection<? extends Edge> other) {
        EdgeSet unwrappedOther = unwrapForAlgebra(other);

        // 1. O(1) Bitwise Algebra on the core engine
        EdgeSet newBacking = this.backingSet.difference(unwrappedOther);

        // 2. Set Algebra on the local delta
        Set<Edge> combinedLocalAdds = new HashSet<>(this.localAdds);
        if (other instanceof ShadowEdgeSet shadowOther) {
            combinedLocalAdds.removeAll(shadowOther.localAdds);
        }

        return new ShadowEdgeSet(this.transactionContext, newBacking, combinedLocalAdds);
    }

    @Override
    public EdgeSet intersect(Collection<? extends Edge> other) {
        EdgeSet unwrappedOther = unwrapForAlgebra(other);

        // 1. O(1) Bitwise Algebra on the core engine
        EdgeSet newBacking = this.backingSet.intersect(unwrappedOther);

        // 2. Set Algebra on the local delta
        Set<Edge> combinedLocalAdds = new HashSet<>();
        if (other instanceof ShadowEdgeSet shadowOther) {
            for (Edge local : this.localAdds) {
                if (shadowOther.localAdds.contains(local)) {
                    combinedLocalAdds.add(local);
                }
            }
        }

        return new ShadowEdgeSet(this.transactionContext, newBacking, combinedLocalAdds);
    }

    @Override
    public Iterator<Edge> iterator() {
        Iterator<Edge> backingIter = new ShadowEdgeIterator(transactionContext, backingSet.iterator());
        return new Iterator<Edge>() {
            private Edge nextEdge = null;
            private boolean backingDone = false;
            private final Iterator<Edge> localIter = localAdds.iterator();

            private void advance() {
                if (nextEdge != null) { return; }

                while (backingIter.hasNext()) {
                    Edge potential = backingIter.next();
                    // Perfect Tombstone Masking
                    if (!transactionContext.getTombstonedEdgeIds().get(potential.id())) {
                        nextEdge = potential;
                        return;
                    }
                }
                backingDone = true;

                if (localIter.hasNext()) {
                    nextEdge = localIter.next();
                }
            }

            @Override
            public boolean hasNext() {
                advance();
                return nextEdge != null;
            }

            @Override
            public Edge next() {
                advance();
                if (nextEdge == null) { throw new java.util.NoSuchElementException(); }
                Edge toReturn = nextEdge;
                nextEdge = null;
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
        overlap.and(transactionContext.getTombstonedEdgeIds());

        return backingSet.size() - overlap.cardinality() + localAdds.size();
    }

    @Override
    public boolean contains(Object obj) {
        if (!(obj instanceof Edge edge)) { return false; }

        if (localAdds.contains(edge)) { return true; }

        if (edge.id() >= 0 && transactionContext.getTombstonedEdgeIds().get(edge.id())) {
             return false;
        }

        return backingSet.contains(edge);
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
            if (!transactionContext.getTombstonedEdgeIds().get(id)) {
                res.add(id);
            }
        }
        for (Edge e : localAdds) {
            res.add(e.id());
        }
        return res;
    }

    @Override
    public int[] toIdArray() {
        return ids().stream().mapToInt(Integer::intValue).toArray();
    }

    @Override
    public Optional<Edge> one() {
        return iterator().hasNext() ? Optional.of(iterator().next()) : Optional.empty();
    }

    @Override
    public EdgeSet toImmutable() {
        if (isEmpty()) { return EdgeSet.empty(); }
        if (size() == 1) { return new dev.chpg.pg.api.GenericImmutableEdgeSet(Collections.singleton(one().get())); }
        return new dev.chpg.pg.api.GenericImmutableEdgeSet(this);
    }

    @Override
    public boolean add(Edge e) { throw new UnsupportedOperationException("Removals/additions must be routed explicitly through EphemeralGraph APIs."); }

    @Override
    public boolean remove(Object o) { throw new UnsupportedOperationException("Removals/additions must be routed explicitly through EphemeralGraph APIs."); }

    @Override
    public boolean addAll(Collection<? extends Edge> c) { throw new UnsupportedOperationException("Removals/additions must be routed explicitly through EphemeralGraph APIs."); }

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
