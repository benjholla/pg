package dev.chpg.pg.multiverse.ephemeral;

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.EdgeSet;
import dev.chpg.pg.multiverse.universe.UniverseEdgeSet;
import dev.chpg.pg.multiverse.universe.UniverseEdge;

import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

    private EdgeSet unwrapForAlgebra(EdgeSet other) {
        if (other instanceof ShadowEdgeSet) {
            return ((ShadowEdgeSet) other).backingSet;
        }
        return other;
    }

    // We implement `unwrapForAlgebra(Collection)` to convert any collection into EdgeSet
    private EdgeSet unwrapForAlgebra(Collection<? extends Edge> other) {
        if (other instanceof ShadowEdgeSet) {
            ShadowEdgeSet shadow = (ShadowEdgeSet) other;
            if (shadow.transactionContext.universe() != this.transactionContext.universe()) {
                throw new IllegalArgumentException("Cross-universe contamination: Shadow sets belong to different universes.");
            }
            return shadow.backingSet;
        }

        if (other instanceof dev.chpg.pg.multiverse.universe.UniverseEdgeSet) {
            dev.chpg.pg.multiverse.universe.UniverseEdgeSet universeSet = (dev.chpg.pg.multiverse.universe.UniverseEdgeSet) other;
            if (universeSet.universe() != this.transactionContext.universe()) {
                throw new IllegalArgumentException("Cross-universe contamination: Universe sets do not match.");
            }
            return universeSet;
        }

        return new dev.chpg.pg.multiverse.universe.UniverseEdgeSet(this.transactionContext.universe(), new java.util.BitSet());
    }
    private Edge unwrapEdge(Edge e) {
        if (e instanceof ShadowEdge shadow) {
            return new UniverseEdge(shadow.universe(), shadow.id());
        }
        return e;
    }

    @Override
    public Iterator<Edge> iterator() {
        Iterator<Edge> backingIter = new ShadowEdgeIterator(transactionContext, backingSet.iterator());
        return new Iterator<Edge>() {
            private Edge nextEdge = null;
            private boolean backingDone = false;
            private final Iterator<Edge> localIter = localAdds.iterator();

            private void advance() {
                if (nextEdge != null) {
    return;
}

                while (backingIter.hasNext()) {
                    Edge potential = backingIter.next();
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
                if (nextEdge == null) {
    throw new java.util.NoSuchElementException();
}
                Edge toReturn = nextEdge;
                nextEdge = null;
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
        overlap.and(transactionContext.getTombstonedEdgeIds());

        return backingSet.size() - overlap.cardinality() + localAdds.size();
    }

    @Override
    public boolean contains(Object obj) {
        if (!(obj instanceof Edge edge)) { return false; }

        for (Edge local : localAdds) {
            if (local.equals(edge) || edge.equals(local)) { return true; }
            if (local instanceof ShadowEdge && ((ShadowEdge) local).backingEdge().equals(edge)) { return true; }
            if (edge instanceof ShadowEdge && ((ShadowEdge) edge).backingEdge().equals(local)) { return true; }
        }

        if (edge instanceof ShadowEdge shadow) {
            if (shadow.id() >= 0 && transactionContext.getTombstonedEdgeIds().get(shadow.id())) {
                return false;
            }
            if (shadow.backingEdge() instanceof dev.chpg.pg.multiverse.universe.UniverseEdge) {
                return backingSet.contains(new dev.chpg.pg.multiverse.universe.UniverseEdge(shadow.universe(), shadow.id()));
            }
            return false;
        }

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
    public EdgeSet union(Collection<? extends Edge> other) {
        EdgeSet unwrappedOther = unwrapForAlgebra(other);

        java.util.List<Edge> universeOnly = new java.util.ArrayList<>();
        for (Edge e : unwrappedOther) {
            if (e instanceof dev.chpg.pg.multiverse.universe.UniverseEdge) {
                 universeOnly.add(e);
            }
        }
        EdgeSet rawUnion = this.backingSet.union(new dev.chpg.pg.api.GenericImmutableEdgeSet(new java.util.HashSet<>(universeOnly)));

        EdgeSet filteredUnion = rawUnion;
        if (filteredUnion instanceof UniverseEdgeSet) {
             UniverseEdgeSet filteredUniverseUnion = (UniverseEdgeSet) filteredUnion;
             filteredUnion = filteredUniverseUnion.difference(
                 new UniverseEdgeSet(
                     this.transactionContext.universe(),
                     this.transactionContext.getTombstonedEdgeIds()
                 )
             );
        } else {
             Set<Integer> tombstoned = new HashSet<>();
             BitSet tombstones = this.transactionContext.getTombstonedEdgeIds();
             for (int i = tombstones.nextSetBit(0); i >= 0; i = tombstones.nextSetBit(i+1)) {
                 tombstoned.add(i);
             }
             EdgeSet toRemove = new EphemeralEdgeSet(tombstoned.stream().map(id -> new UniverseEdge(this.transactionContext.universe(), id)).collect(Collectors.toList()));
             filteredUnion = filteredUnion.difference(toRemove);
        }

        Set<Edge> combinedLocalAdds = new HashSet<>(this.localAdds);
        if (other instanceof ShadowEdgeSet shadowOther) {
            combinedLocalAdds.addAll(shadowOther.localAdds);
        } else if (other != null) {
            for (Edge e : other) {
                if (e instanceof EphemeralEdge || e instanceof ShadowEdge) {
                    combinedLocalAdds.add(e);
                }
            }
        }
        System.out.println("Union combinedLocalAdds size: " + combinedLocalAdds.size());

        return new ShadowEdgeSet(this.transactionContext, filteredUnion, combinedLocalAdds);
    }

    @Override
    public EdgeSet difference(Collection<? extends Edge> other) {
        EdgeSet unwrappedOther = unwrapForAlgebra(other);

        java.util.List<Edge> universeOnly = new java.util.ArrayList<>();
        for (Edge e : unwrappedOther) {
            if (e instanceof dev.chpg.pg.multiverse.universe.UniverseEdge) {
                 universeOnly.add(e);
            }
        }
        EdgeSet rawDiff = this.backingSet.difference(new dev.chpg.pg.api.GenericImmutableEdgeSet(new java.util.HashSet<>(universeOnly)));

        EdgeSet filteredDiff = rawDiff;
        if (filteredDiff instanceof UniverseEdgeSet) {
             UniverseEdgeSet filteredUniverseDiff = (UniverseEdgeSet) filteredDiff;
             filteredDiff = filteredUniverseDiff.difference(
                 new UniverseEdgeSet(
                     this.transactionContext.universe(),
                     this.transactionContext.getTombstonedEdgeIds()
                 )
             );
        } else {
             Set<Integer> tombstoned = new HashSet<>();
             BitSet tombstones = this.transactionContext.getTombstonedEdgeIds();
             for (int i = tombstones.nextSetBit(0); i >= 0; i = tombstones.nextSetBit(i+1)) {
                 tombstoned.add(i);
             }
             EdgeSet toRemove = new EphemeralEdgeSet(tombstoned.stream().map(id -> new UniverseEdge(this.transactionContext.universe(), id)).collect(Collectors.toList()));
             filteredDiff = filteredDiff.difference(toRemove);
        }

        Set<Edge> combinedLocalAdds = new HashSet<>();
        for (Edge local : this.localAdds) {
            boolean found = false;
            if (other instanceof ShadowEdgeSet) {
                ShadowEdgeSet shadowOther = (ShadowEdgeSet) other;
                for (Edge o : shadowOther.localAdds) {
                    if (local.equals(o) || o.equals(local) ||
                        (local instanceof ShadowEdge && ((ShadowEdge) local).backingEdge().equals(o)) ||
                        (o instanceof ShadowEdge && ((ShadowEdge) o).backingEdge().equals(local))) {
                        found = true;
                        break;
                    }
                }
            } else if (other != null) {
                for (Edge o : other) {
                    if (local.equals(o) || o.equals(local) ||
                        (local instanceof ShadowEdge && ((ShadowEdge) local).backingEdge().equals(o)) ||
                        (o instanceof ShadowEdge && ((ShadowEdge) o).backingEdge().equals(local))) {
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                combinedLocalAdds.add(local);
            }
        }

        return new ShadowEdgeSet(this.transactionContext, filteredDiff, combinedLocalAdds);
    }

    @Override
    public EdgeSet intersect(Collection<? extends Edge> other) {
        EdgeSet unwrappedOther = unwrapForAlgebra(other);

        java.util.List<Edge> universeOnly = new java.util.ArrayList<>();
        for (Edge e : unwrappedOther) {
            if (e instanceof dev.chpg.pg.multiverse.universe.UniverseEdge) {
                 universeOnly.add(e);
            }
        }
        EdgeSet rawIntersect = this.backingSet.intersect(new dev.chpg.pg.api.GenericImmutableEdgeSet(new java.util.HashSet<>(universeOnly)));

        EdgeSet filteredIntersect = rawIntersect;
        if (filteredIntersect instanceof UniverseEdgeSet) {
             UniverseEdgeSet filteredUniverseIntersect = (UniverseEdgeSet) filteredIntersect;
             filteredIntersect = filteredUniverseIntersect.difference(
                 new UniverseEdgeSet(
                     this.transactionContext.universe(),
                     this.transactionContext.getTombstonedEdgeIds()
                 )
             );
        } else {
             Set<Integer> tombstoned = new HashSet<>();
             BitSet tombstones = this.transactionContext.getTombstonedEdgeIds();
             for (int i = tombstones.nextSetBit(0); i >= 0; i = tombstones.nextSetBit(i+1)) {
                 tombstoned.add(i);
             }
             EdgeSet toRemove = new EphemeralEdgeSet(tombstoned.stream().map(id -> new UniverseEdge(this.transactionContext.universe(), id)).collect(Collectors.toList()));
             filteredIntersect = filteredIntersect.difference(toRemove);
        }

        Set<Edge> combinedLocalAdds = new HashSet<>();
        for (Edge local : this.localAdds) {
            boolean found = false;
            if (other instanceof ShadowEdgeSet) {
                ShadowEdgeSet shadowOther = (ShadowEdgeSet) other;
                for (Edge o : shadowOther.localAdds) {
                    if (local.equals(o) || o.equals(local) ||
                        (local instanceof ShadowEdge && ((ShadowEdge) local).backingEdge().equals(o)) ||
                        (o instanceof ShadowEdge && ((ShadowEdge) o).backingEdge().equals(local))) {
                        found = true;
                        break;
                    }
                }
            } else if (other != null) {
                for (Edge o : other) {
                    if (local.equals(o) || o.equals(local) ||
                        (local instanceof ShadowEdge && ((ShadowEdge) local).backingEdge().equals(o)) ||
                        (o instanceof ShadowEdge && ((ShadowEdge) o).backingEdge().equals(local))) {
                        found = true;
                        break;
                    }
                }
            }
            if (found) {
                combinedLocalAdds.add(local);
            }
        }

        return new ShadowEdgeSet(this.transactionContext, filteredIntersect, combinedLocalAdds);
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
        if (size() == 1) { return new dev.chpg.pg.api.GenericImmutableEdgeSet(java.util.Collections.singleton(one().get())); }
        return new dev.chpg.pg.api.GenericImmutableEdgeSet(this);
    }

    @Override
    public boolean add(Edge e) {
        throw new UnsupportedOperationException("Removals/additions must be routed explicitly through EphemeralGraph APIs.");
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("Removals/additions must be routed explicitly through EphemeralGraph APIs.");
    }

    @Override
    public boolean addAll(Collection<? extends Edge> c) {
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
