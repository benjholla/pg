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
 * A read-optimized, immutable view of active edges within a UniverseGraph.
 * Wraps a raw BitSet and instantiates transient UniverseEdge flyweights on demand.
 */
public final class UniverseEdgeSet implements EdgeSet {

    private final Universe universe;
    private final BitSet activeBits;

    /**
     * Package-private constructor.
     * Only UniverseGraph and internal Universe queries should instantiate this view.
     *
     * @param universe The parent universe instance
     * @param activeBits The underlying active bits
     */
    UniverseEdgeSet(Universe universe, BitSet activeBits) {
        this.universe = Objects.requireNonNull(universe, "Universe cannot be null");
        this.activeBits = Objects.requireNonNull(activeBits, "Active BitSet cannot be null");
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
        return this; // Inherently immutable, zero-allocation return
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
        if (this == o) {
            return true;
        }
        if (!(o instanceof UniverseEdge)) {
            return false;
        }

        UniverseEdge edge = (UniverseEdge) o;
        if (edge.universe() != this.universe) {
            return false;
        }

        return this.activeBits.get(edge.id());
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        Objects.requireNonNull(c, "Collection cannot be null");
        for (Object e : c) {
            Objects.requireNonNull(e, "Element cannot be null");
            if (!contains(e)) {
                return false;
            }
        }
        return true;
    }

    // =========================================================================
    // 3. MATHEMATICAL SET OPERATIONS
    // =========================================================================

    @Override
    public EdgeSet intersect(Collection<? extends Edge> other) {
        Objects.requireNonNull(other, "Collection cannot be null");

        // 1. Bitwise Fast-Path
        if (other instanceof UniverseEdgeSet && ((UniverseEdgeSet) other).universe == this.universe) {
            BitSet clonedBits = (BitSet) this.activeBits.clone();
            clonedBits.and(((UniverseEdgeSet) other).activeBits);
            return clonedBits.isEmpty() ? EdgeSet.empty() : new UniverseEdgeSet(this.universe, clonedBits);
        }

        // 2. Optimized Fallback (Result is guaranteed to be a subset of this)
        BitSet fallbackBits = new BitSet();
        for (Edge e : other) {
            Objects.requireNonNull(e, "Element cannot be null");
            if (this.contains(e)) {
                fallbackBits.set(e.id());
            }
        }
        return fallbackBits.isEmpty() ? EdgeSet.empty() : new UniverseEdgeSet(this.universe, fallbackBits);
    }

    @Override
    public EdgeSet difference(Collection<? extends Edge> other) {
        Objects.requireNonNull(other, "Collection cannot be null");

        // 1. Bitwise Fast-Path
        if (other instanceof UniverseEdgeSet && ((UniverseEdgeSet) other).universe == this.universe) {
            BitSet clonedBits = (BitSet) this.activeBits.clone();
            clonedBits.andNot(((UniverseEdgeSet) other).activeBits);
            return clonedBits.isEmpty() ? EdgeSet.empty() : new UniverseEdgeSet(this.universe, clonedBits);
        }

        // 2. Optimized Fallback (Result is guaranteed to be a subset of this)
        BitSet fallbackBits = (BitSet) this.activeBits.clone();
        for (Edge e : other) {
            Objects.requireNonNull(e, "Element cannot be null");
            if (e instanceof UniverseEdge && ((UniverseEdge) e).universe() == this.universe) {
                fallbackBits.clear(e.id());
            }
        }
        return fallbackBits.isEmpty() ? EdgeSet.empty() : new UniverseEdgeSet(this.universe, fallbackBits);
    }

    @Override
    public EdgeSet union(Collection<? extends Edge> other) {
        Objects.requireNonNull(other, "Collection cannot be null");

        // 1. Bitwise Fast-Path
        if (other instanceof UniverseEdgeSet && ((UniverseEdgeSet) other).universe == this.universe) {
            BitSet clonedBits = (BitSet) this.activeBits.clone();
            clonedBits.or(((UniverseEdgeSet) other).activeBits);
            return new UniverseEdgeSet(this.universe, clonedBits);
        }

        // 2. Mandatory Generic Fallback (Heterogeneous result)
        java.util.Set<Edge> unioned = new java.util.HashSet<>();

        for (int id = this.activeBits.nextSetBit(0); id >= 0; id = this.activeBits.nextSetBit(id + 1)) {
            unioned.add(new UniverseEdge(this.universe, id));
        }

        for (Edge e : other) {
            Objects.requireNonNull(e, "Element cannot be null");
            unioned.add(e);
        }

        return new dev.chpg.pg.api.GenericImmutableEdgeSet(unioned);
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

    @Override
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
                if (!(o instanceof Integer)) {
                    return false;
                }
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
        Objects.requireNonNull(a, "Array cannot be null");
        int currentSize = size();
        T[] result = a.length >= currentSize ? a
                : (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), currentSize);

        int i = 0;
        for (int id = this.activeBits.nextSetBit(0); id >= 0; id = this.activeBits.nextSetBit(id + 1)) {
            result[i++] = (T) new UniverseEdge(this.universe, id);
        }

        if (result.length > currentSize) {
            result[currentSize] = null;
        }
        return result;
    }

    // =========================================================================
    // 5. IMMUTABILITY GUARDRAILS (Fail-Fast Mutations)
    // =========================================================================

    @Override
    public boolean add(Edge edge) {
        throw new UnsupportedOperationException("UniverseEdgeSet is a read-only bitwise view.");
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("UniverseEdgeSet is a read-only bitwise view.");
    }

    @Override
    public boolean addAll(Collection<? extends Edge> c) {
        throw new UnsupportedOperationException("UniverseEdgeSet is a read-only bitwise view.");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("UniverseEdgeSet is a read-only bitwise view.");
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("UniverseEdgeSet is a read-only bitwise view.");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("UniverseEdgeSet is a read-only bitwise view.");
    }
}
