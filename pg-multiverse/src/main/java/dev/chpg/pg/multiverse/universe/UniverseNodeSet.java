package dev.chpg.pg.multiverse.universe;

import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.NodeSet;

import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * A read-optimized, immutable view of active nodes within a UniverseGraph.
 * Wraps a raw BitSet and instantiates transient UniverseNode flyweights on demand.
 */
public final class UniverseNodeSet implements NodeSet, UniverseView {

    private final Universe universe;
    private final BitSet activeBits;

    /**
     * Package-private constructor.
     * Only UniverseGraph and internal Universe queries should instantiate this view.
     *
     * @param universe   the backing universe
     * @param activeBits the bitset representing active node IDs
     */
    UniverseNodeSet(Universe universe, BitSet activeBits) {
        this.universe = Objects.requireNonNull(universe, "Universe cannot be null");
        this.activeBits = Objects.requireNonNull(activeBits, "Active BitSet cannot be null");
    }

    // =========================================================================
    // =========================================================================
    // 0. ENGINE ACCESS
    // =========================================================================

    /**
     * Exposes the underlying bitwise storage engine backing this element.
     */
    @Override
    public Universe universe() {
        return this.universe;
    }

    // 1. O(1) SIZING & MAGNITUDE
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
        // BitSets are natively materialized; the magnitude is always known in O(1)
        return true;
    }

    // =========================================================================
    // 2. PRIMITIVE ROUTING & CONTAINMENT
    // =========================================================================

    @Override
    public boolean contains(Object o) {
        // 1. Fast-path reference check
        if (this == o) {
            return true;
        }

        // 2. Strict Type Boundary (Silent ignore for subtractions/queries)
        if (!(o instanceof UniverseNode)) {
            return false;
        }

        UniverseNode node = (UniverseNode) o;

        // 3. Optional: Strict Universe boundary check (Prevents cross-contamination)
        if (node.universe() != this.universe) {
            return false;
        }

        // 4. Primitive BitSet lookup
        return this.activeBits.get(node.id());
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        Objects.requireNonNull(c, "Collection cannot be null");
        for (Object e : c) {
            if (!contains(e)) {
                return false;
            }
        }
        return true;
    }

    // =========================================================================
    // 3. ZERO-ALLOCATION TRAVERSAL
    // =========================================================================

    @Override
    public Iterator<Node> iterator() {
        // Leverages JDK 17 IntStream to scan the active bits, mapping them
        // dynamically to transient UniverseNode flyweights.
        return this.activeBits.stream()
                .mapToObj(id -> (Node) new UniverseNode(this.universe, id))
                .iterator();
    }

    /**
     * Expose the raw stream.
     *
     * @return A stream of transient node flyweights.
     */
    public Stream<Node> stream() {
        return this.activeBits.stream()
                .mapToObj(id -> (Node) new UniverseNode(this.universe, id));
    }

    // =========================================================================
    // 4. IMMUTABILITY GUARDRAILS (Fail-Fast Mutations)
    // =========================================================================

    @Override
    public boolean add(Node node) {
        throw new UnsupportedOperationException("UniverseNodeSet is a read-only bitwise view. Mutations are strictly forbidden.");
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("UniverseNodeSet is a read-only bitwise view. Mutations are strictly forbidden.");
    }

    @Override
    public boolean addAll(Collection<? extends Node> c) {
        throw new UnsupportedOperationException("UniverseNodeSet is a read-only bitwise view.");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("UniverseNodeSet is a read-only bitwise view.");
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("UniverseNodeSet is a read-only bitwise view.");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("UniverseNodeSet is a read-only bitwise view.");
    }

    // =========================================================================
    // 5. NODESET API IMPLEMENTATIONS
    // =========================================================================

    @Override
    public NodeSet toImmutable() {
        return this;
    }

    @Override
    public Optional<Node> one() {
        int firstId = this.activeBits.nextSetBit(0);
        if (firstId == -1) {
            return Optional.empty();
        }
        return Optional.of(new UniverseNode(this.universe, firstId));
    }

    @Override
    public NodeSet intersect(Collection<? extends Node> other) {
        Objects.requireNonNull(other, "Collection cannot be null");
        // 1. Bitwise Fast-Path
        if (other instanceof UniverseNodeSet && ((UniverseNodeSet) other).universe == this.universe) {
            BitSet clonedBits = (BitSet) this.activeBits.clone();
            clonedBits.and(((UniverseNodeSet) other).activeBits);
            return clonedBits.isEmpty() ? NodeSet.empty() : new UniverseNodeSet(this.universe, clonedBits);
        }

        // 2. The Optimized Fallback
        BitSet fallbackBits = new BitSet();
        for (Node n : other) {
            // this.contains() safely and silently rejects foreign nodes,
            // so we only set bits for valid UniverseNodes that exist in this set.
            if (this.contains(n)) {
                fallbackBits.set(n.id());
            }
        }

        return fallbackBits.isEmpty() ? NodeSet.empty() : new UniverseNodeSet(this.universe, fallbackBits);
    }

    @Override
    public NodeSet difference(Collection<? extends Node> other) {
        Objects.requireNonNull(other, "Collection cannot be null");
        // 1. Bitwise Fast-Path
        if (other instanceof UniverseNodeSet && ((UniverseNodeSet) other).universe == this.universe) {
            BitSet clonedBits = (BitSet) this.activeBits.clone();
            clonedBits.andNot(((UniverseNodeSet) other).activeBits);
            return clonedBits.isEmpty() ? NodeSet.empty() : new UniverseNodeSet(this.universe, clonedBits);
        }

        // 2. The Optimized Fallback
        BitSet fallbackBits = (BitSet) this.activeBits.clone();
        for (Node n : other) {
            // Safely check if the foreign collection contains one of our nodes
            if (n instanceof UniverseNode && ((UniverseNode) n).universe() == this.universe) {
                fallbackBits.clear(n.id());
            }
        }

        return fallbackBits.isEmpty() ? NodeSet.empty() : new UniverseNodeSet(this.universe, fallbackBits);
    }

    @Override
    public NodeSet union(Collection<? extends Node> other) {
        Objects.requireNonNull(other, "Collection cannot be null");
        // 1. Bitwise Fast-Path
        if (other instanceof UniverseNodeSet && ((UniverseNodeSet) other).universe == this.universe) {
            BitSet clonedBits = (BitSet) this.activeBits.clone();
            clonedBits.or(((UniverseNodeSet) other).activeBits);
            return new UniverseNodeSet(this.universe, clonedBits);
        }

        // 2. The Mandatory Generic Fallback
        // We must use a HashSet because the result will be a polyglot mixture of implementations.
        java.util.Set<Node> unioned = new java.util.HashSet<>();

        // Add all of our internal flyweights (zero-allocation iteration!)
        for (int id = this.activeBits.nextSetBit(0); id >= 0; id = this.activeBits.nextSetBit(id + 1)) {
            unioned.add(new UniverseNode(this.universe, id));
        }

        // Add the foreign nodes
        unioned.addAll(other);

        return new dev.chpg.pg.api.GenericImmutableNodeSet(unioned);
    }

    @Override
    public boolean isMaterialized() {
        return true;
    }

    @Override
    public Set<Integer> ids() {
        return new java.util.AbstractSet<Integer>() {
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
            result[i++] = (T) new UniverseNode(this.universe, id);
        }

        if (result.length > currentSize) {
            result[currentSize] = null;
        }
        return result;
    }
}
