package dev.chpg.pg.multiverse.universe;

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.EdgeSet;

import java.util.AbstractSet;
import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * A bitwise-backed, mutable topological viewport into a pg-multiverse Universe.
 * Modifying this set only alters the local bit-mask, never the underlying engine arrays.
 * Strictly rejects cross-contamination from foreign graphs or sandboxes.
 */
public final class UniverseEdgeSet implements EdgeSet, UniverseView {

    private final Universe universe;
    private final BitSet activeBits;

    UniverseEdgeSet(Universe universe, BitSet activeBits) {
        this.universe = Objects.requireNonNull(universe, "Universe cannot be null");
        this.activeBits = Objects.requireNonNull(activeBits, "Active BitSet cannot be null");
    }

    @Override
    public Universe universe() {
        return this.universe;
    }

    // =========================================================================
    // 1. CORE API & MAGNITUDE
    // =========================================================================

    @Override
    public int size() {
        return this.activeBits.cardinality();
    }

    @Override
    public boolean isEmpty() {
        return this.activeBits.isEmpty();
    }

    @Override
    public boolean isSizeKnown() {
        return true;
    }

    @Override
    public boolean isMaterialized() {
        return true;
    }

    @Override
    public EdgeSet toImmutable() {
        if (isEmpty()) {
            return EdgeSet.empty();
        } else if (size() == 1) {
            return new UniverseImmutableSingletonEdgeSet((UniverseEdge) one().get());
        }
        BitSet clonedBits = (BitSet) this.activeBits.clone();
        return new UniverseImmutableEdgeSet(new UniverseEdgeSet(this.universe, clonedBits));
    }

    @Override
    public Optional<Edge> one() {
        int firstId = this.activeBits.nextSetBit(0);
        if (firstId == -1) {
            return Optional.empty();
        }
        return Optional.of(new UniverseEdge(this.universe, firstId));
    }

    // =========================================================================
    // 2. PRIMITIVE ROUTING & CONTAINMENT
    // =========================================================================

    @Override
    public boolean contains(Object o) {
        if (this == o) { return true; }
        if (!(o instanceof UniverseEdge)) { return false; }

        UniverseEdge edge = (UniverseEdge) o;

        if (edge.universe() != this.universe) { return false; }

        return this.activeBits.get(edge.id());
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object e : c) {
            if (!contains(e)) { return false; }
        }
        return true;
    }

    // =========================================================================
    // 3. STRICTLY BOUNDED SET OPERATIONS
    // =========================================================================

    @Override
    public EdgeSet intersect(Collection<? extends Edge> other) {
        if (other instanceof UniverseEdgeSet && ((UniverseEdgeSet) other).universe == this.universe) {
            BitSet clonedBits = (BitSet) this.activeBits.clone();
            clonedBits.and(((UniverseEdgeSet) other).activeBits);
            return clonedBits.isEmpty() ? EdgeSet.empty() : new UniverseEdgeSet(this.universe, clonedBits);
        }

        BitSet fallbackBits = new BitSet();
        for (Edge e : other) {
            if (this.contains(e)) { fallbackBits.set(e.id()); }
        }
        return fallbackBits.isEmpty() ? EdgeSet.empty() : new UniverseEdgeSet(this.universe, fallbackBits);
    }

    @Override
    public EdgeSet difference(Collection<? extends Edge> other) {
        if (other instanceof UniverseEdgeSet && ((UniverseEdgeSet) other).universe == this.universe) {
            BitSet clonedBits = (BitSet) this.activeBits.clone();
            clonedBits.andNot(((UniverseEdgeSet) other).activeBits);
            return clonedBits.isEmpty() ? EdgeSet.empty() : new UniverseEdgeSet(this.universe, clonedBits);
        }

        BitSet fallbackBits = (BitSet) this.activeBits.clone();
        for (Edge e : other) {
            if (e instanceof UniverseEdge && ((UniverseEdge) e).universe() == this.universe) {
                fallbackBits.clear(e.id());
            }
        }
        return fallbackBits.isEmpty() ? EdgeSet.empty() : new UniverseEdgeSet(this.universe, fallbackBits);
    }

    @Override
    public EdgeSet union(Collection<? extends Edge> other) {
        if (other instanceof UniverseEdgeSet && ((UniverseEdgeSet) other).universe == this.universe) {
            BitSet clonedBits = (BitSet) this.activeBits.clone();
            clonedBits.or(((UniverseEdgeSet) other).activeBits);
            return new UniverseEdgeSet(this.universe, clonedBits);
        }

        BitSet clonedBits = (BitSet) this.activeBits.clone();
        for (Edge e : other) {
            if (!(e instanceof UniverseEdge)) {
                throw new IllegalArgumentException("Cannot union with foreign edge. Must be a UniverseEdge.");
            }
            UniverseEdge uEdge = (UniverseEdge) e;
            if (uEdge.universe() != this.universe) {
                throw new IllegalArgumentException("Cannot union with a UniverseEdge from a different Universe instance.");
            }
            clonedBits.set(uEdge.id());
        }

        return new UniverseEdgeSet(this.universe, clonedBits);
    }

    // =========================================================================
    // 4. ZERO-ALLOCATION TRAVERSAL & ID BRIDGING
    // =========================================================================

    @Override
    public Iterator<Edge> iterator() {
        return this.activeBits.stream()
                .mapToObj(id -> (Edge) new UniverseEdge(this.universe, id))
                .iterator();
    }

    public Stream<Edge> stream() {
        return this.activeBits.stream()
                .mapToObj(id -> (Edge) new UniverseEdge(this.universe, id));
    }

    @Override
    public Set<Integer> ids() {
        return new AbstractSet<Integer>() {
            @Override
            public Iterator<Integer> iterator() {
                return activeBits.stream().iterator();
            }
            @Override
            public int size() {
                return activeBits.cardinality();
            }
            @Override
            public boolean contains(Object o) {
                if (!(o instanceof Integer)) { return false; }
                return activeBits.get((Integer) o);
            }
        };
    }

    @Override
    public int[] toIdArray() {
        return this.activeBits.stream().toArray();
    }

    @Override
    public Object[] toArray() {
        return this.stream().toArray();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        int size = size();
        T[] result = a.length >= size ? a : (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);
        int i = 0;
        for (int id = this.activeBits.nextSetBit(0); id >= 0; id = this.activeBits.nextSetBit(id + 1)) {
            result[i++] = (T) new UniverseEdge(this.universe, id);
        }
        if (result.length > size) { result[size] = null; }
        return result;
    }

    // =========================================================================
    // 5. MUTABILITY & LINEAGE CHECKS
    // =========================================================================

    @Override
    public boolean add(Edge edge) {
        if (!(edge instanceof UniverseEdge)) {
            throw new IllegalArgumentException("Cannot add foreign edge. Must be a UniverseEdge.");
        }
        UniverseEdge uEdge = (UniverseEdge) edge;
        if (uEdge.universe() != this.universe) {
            throw new IllegalArgumentException("Cannot add a UniverseEdge from a different Universe instance.");
        }

        boolean isNew = !this.activeBits.get(uEdge.id());
        this.activeBits.set(uEdge.id());
        return isNew;
    }

    @Override
    public boolean remove(Object o) {
        if (!(o instanceof UniverseEdge)) { return false; }
        UniverseEdge uEdge = (UniverseEdge) o;
        if (uEdge.universe() != this.universe) { return false; }

        boolean exists = this.activeBits.get(uEdge.id());
        this.activeBits.clear(uEdge.id());
        return exists;
    }

    @Override
    public boolean addAll(Collection<? extends Edge> c) {
        boolean modified = false;
        for (Edge e : c) {
            if (this.add(e)) { modified = true; }
        }
        return modified;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean modified = false;
        for (Object o : c) {
            if (this.remove(o)) { modified = true; }
        }
        return modified;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        BitSet retainMask = new BitSet();
        for (Object o : c) {
            if (o instanceof UniverseEdge && ((UniverseEdge) o).universe() == this.universe) {
                retainMask.set(((UniverseEdge) o).id());
            }
        }

        int preSize = this.activeBits.cardinality();
        this.activeBits.and(retainMask);
        return this.activeBits.cardinality() != preSize;
    }

    @Override
    public void clear() {
        this.activeBits.clear();
    }
}
