package dev.chpg.pg.multiverse.universe;

import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.NodeSet;

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
 * <p>
 * <b>What it represents:</b> A set of nodes backed by a highly optimized {@code BitSet} rather than object references.
 * <p>
 * <b>Why it exists:</b> It provides zero-GC traversal and O(1) set operations (union, intersection, difference) by operating directly on primitive bit masks instead of wrapping individual elements.
 * <p>
 * <b>When to use it:</b> This is the standard {@code NodeSet} implementation returned by all core engine operations (e.g., querying nodes, graph traversals).
 * <p>
 * <b>Common usage patterns:</b> Functions exactly like a standard Java {@code Set}. Highly optimized for boolean algebra via the {@code union}, {@code difference}, and {@code intersect} methods.
 * <p>
 * <b>Important invariants:</b> Modifying this set only alters the local bit-mask view; it never mutates the underlying engine's topology. Rejects cross-contamination from foreign universes.
 * <p>
 * <b>Thread safety:</b> Enforces fail-fast concurrency during traversal. If the core universe topology is mutated while this set is being iterated or materially modified, it will throw a {@code ConcurrentModificationException}.
 * <p>
 * <b>Performance characteristics:</b> Traversal operates entirely within the CPU L1 cache. Instantiating elements only occurs lazily when requested by an iterator.
 */
public final class UniverseNodeSet implements NodeSet, UniverseView {

    private final Universe universe;
    private final BitSet activeBits;

    /**
     * Constructs a new {@code UniverseNodeSet}.
     *
     * @param universe the isolated Universe engine this set belongs to
     * @param activeBits the bitmask representing the active nodes in this set
     */
    public UniverseNodeSet(Universe universe, BitSet activeBits) {
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
    public NodeSet toImmutable() {
        if (isEmpty()) {
            return NodeSet.empty();
        } else if (size() == 1) {
            return new UniverseImmutableSingletonNodeSet((UniverseNode) one().get());
        }
        BitSet clonedBits = (BitSet) this.activeBits.clone();
        return new UniverseImmutableNodeSet(new UniverseNodeSet(this.universe, clonedBits));
    }

    @Override
    public Optional<Node> one() {
        int firstId = this.activeBits.nextSetBit(0);
        if (firstId == -1) {
            return Optional.empty();
        }
        return Optional.of(new UniverseNode(this.universe, firstId));
    }

    // =========================================================================
    // 2. PRIMITIVE ROUTING & CONTAINMENT
    // =========================================================================

    @Override
    public boolean contains(Object o) {
        if (this == o) { return true; }
        if (!(o instanceof UniverseNode)) { return false; }

        UniverseNode node = (UniverseNode) o;

        if (node.universe() != this.universe) { return false; }

        return this.activeBits.get(node.id());
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        long expectedModCount = this.universe.modCount();
        for (Object e : c) {
            if (!contains(e)) {
                if (this.universe.modCount() != expectedModCount) {
                    throw new java.util.ConcurrentModificationException("Universe engine was modified during bulk operation.");
                }
                return false;
            }
        }
        if (this.universe.modCount() != expectedModCount) {
            throw new java.util.ConcurrentModificationException("Universe engine was modified during bulk operation.");
        }
        return true;
    }

    // =========================================================================
    // 3. STRICTLY BOUNDED SET OPERATIONS
    // =========================================================================

    @Override
    public NodeSet intersect(Collection<? extends Node> other) {
        if (other instanceof UniverseNodeSet && ((UniverseNodeSet) other).universe == this.universe) {
            BitSet clonedBits = (BitSet) this.activeBits.clone();
            clonedBits.and(((UniverseNodeSet) other).activeBits);
            return clonedBits.isEmpty() ? NodeSet.empty() : new UniverseNodeSet(this.universe, clonedBits);
        }

        BitSet fallbackBits = new BitSet();
        for (Node n : other) {
            if (this.contains(n)) { fallbackBits.set(n.id()); }
        }
        return fallbackBits.isEmpty() ? NodeSet.empty() : new UniverseNodeSet(this.universe, fallbackBits);
    }

    @Override
    public NodeSet difference(Collection<? extends Node> other) {
        if (other instanceof UniverseNodeSet && ((UniverseNodeSet) other).universe == this.universe) {
            BitSet clonedBits = (BitSet) this.activeBits.clone();
            clonedBits.andNot(((UniverseNodeSet) other).activeBits);
            return clonedBits.isEmpty() ? NodeSet.empty() : new UniverseNodeSet(this.universe, clonedBits);
        }

        BitSet fallbackBits = (BitSet) this.activeBits.clone();
        for (Node n : other) {
            if (n instanceof UniverseNode && ((UniverseNode) n).universe() == this.universe) {
                fallbackBits.clear(n.id());
            }
        }
        return fallbackBits.isEmpty() ? NodeSet.empty() : new UniverseNodeSet(this.universe, fallbackBits);
    }

    @Override
    public NodeSet union(Collection<? extends Node> other) {
        if (other instanceof UniverseNodeSet && ((UniverseNodeSet) other).universe == this.universe) {
            BitSet clonedBits = (BitSet) this.activeBits.clone();
            clonedBits.or(((UniverseNodeSet) other).activeBits);
            return new UniverseNodeSet(this.universe, clonedBits);
        }

        BitSet clonedBits = (BitSet) this.activeBits.clone();
        for (Node n : other) {
            if (!(n instanceof UniverseNode)) {
                throw new IllegalArgumentException("Cannot union with foreign node. Must be a UniverseNode.");
            }
            UniverseNode uNode = (UniverseNode) n;
            if (uNode.universe() != this.universe) {
                throw new IllegalArgumentException("Cannot union with a UniverseNode from a different Universe instance.");
            }
            clonedBits.set(uNode.id());
        }

        return new UniverseNodeSet(this.universe, clonedBits);
    }

    // =========================================================================
    // 4. ZERO-ALLOCATION TRAVERSAL & ID BRIDGING
    // =========================================================================

    @Override
    public Iterator<Node> iterator() {
        return new Iterator<>() {
            // 1. Capture the timeline
            private final long expectedModCount = universe.modCount();
            // 2. Initialize the L1-cached hardware scan
            private int cursor = activeBits.nextSetBit(0);

            private void checkForComodification() {
                if (universe.modCount() != expectedModCount) {
                    throw new java.util.ConcurrentModificationException(
                        "Universe engine was modified during NodeSet iteration."
                    );
                }
            }

            @Override
            public boolean hasNext() {
                checkForComodification();
                return cursor >= 0;
            }

            @Override
            public Node next() {
                checkForComodification();
                if (cursor < 0) {
                    throw new java.util.NoSuchElementException();
                }

                // Capture the current valid ID
                int currentId = cursor;

                // Advance the bitset cursor to the next active ID
                cursor = activeBits.nextSetBit(currentId + 1);

                // Spawn the ephemeral flyweight
                return new UniverseNode(universe, currentId);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Universe views are strictly read-only.");
            }
        };
    }

    public Stream<Node> stream() {
        return this.activeBits.stream()
                .mapToObj(id -> (Node) new UniverseNode(this.universe, id));
    }

    @Override
    public Set<Integer> ids() {
        return new AbstractSet<Integer>() {
            @Override
            public Iterator<Integer> iterator() {
                return new Iterator<Integer>() {
                    private final long expectedModCount = universe.modCount();
                    private int cursor = activeBits.nextSetBit(0);

                    private void checkForComodification() {
                        if (universe.modCount() != expectedModCount) {
                            throw new java.util.ConcurrentModificationException(
                                "Universe engine was modified during ID set iteration."
                            );
                        }
                    }

                    @Override
                    public boolean hasNext() {
                        checkForComodification();
                        return cursor >= 0;
                    }

                    @Override
                    public Integer next() {
                        checkForComodification();
                        if (cursor < 0) {
                            throw new java.util.NoSuchElementException();
                        }
                        int currentId = cursor;
                        cursor = activeBits.nextSetBit(currentId + 1);
                        return currentId;
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException("Universe views are strictly read-only.");
                    }
                };
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
        long expectedModCount = this.universe.modCount();
        int size = size();
        int[] result = new int[size];
        int i = 0;
        for (int id = this.activeBits.nextSetBit(0); id >= 0; id = this.activeBits.nextSetBit(id + 1)) {
            result[i++] = id;
        }
        if (this.universe.modCount() != expectedModCount) {
            throw new java.util.ConcurrentModificationException("Universe engine was modified during bulk operation.");
        }
        return result;
    }

    @Override
    public Object[] toArray() {
        long expectedModCount = this.universe.modCount();
        int size = size();
        Object[] result = new Object[size];
        int i = 0;
        for (int id = this.activeBits.nextSetBit(0); id >= 0; id = this.activeBits.nextSetBit(id + 1)) {
            result[i++] = new UniverseNode(this.universe, id);
        }
        if (this.universe.modCount() != expectedModCount) {
            throw new java.util.ConcurrentModificationException("Universe engine was modified during bulk operation.");
        }
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        long expectedModCount = this.universe.modCount();
        int size = size();
        T[] result = a.length >= size ? a : (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);
        int i = 0;
        for (int id = this.activeBits.nextSetBit(0); id >= 0; id = this.activeBits.nextSetBit(id + 1)) {
            result[i++] = (T) new UniverseNode(this.universe, id);
        }
        if (result.length > size) { result[size] = null; }
        if (this.universe.modCount() != expectedModCount) {
            throw new java.util.ConcurrentModificationException("Universe engine was modified during bulk operation.");
        }
        return result;
    }

    // =========================================================================
    // 5. MUTABILITY & LINEAGE CHECKS
    // =========================================================================

    @Override
    public boolean add(Node node) {
        if (!(node instanceof UniverseNode)) {
            throw new IllegalArgumentException("Cannot add foreign node. Must be a UniverseNode.");
        }
        UniverseNode uNode = (UniverseNode) node;
        if (uNode.universe() != this.universe) {
            throw new IllegalArgumentException("Cannot add a UniverseNode from a different Universe instance.");
        }

        boolean isNew = !this.activeBits.get(uNode.id());
        this.activeBits.set(uNode.id());
        return isNew;
    }

    @Override
    public boolean remove(Object o) {
        if (!(o instanceof UniverseNode)) { return false; }
        UniverseNode uNode = (UniverseNode) o;
        if (uNode.universe() != this.universe) { return false; }

        boolean exists = this.activeBits.get(uNode.id());
        this.activeBits.clear(uNode.id());
        return exists;
    }

    @Override
    public boolean addAll(Collection<? extends Node> c) {
        boolean modified = false;
        for (Node n : c) {
            if (this.add(n)) { modified = true; }
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
            if (o instanceof UniverseNode && ((UniverseNode) o).universe() == this.universe) {
                retainMask.set(((UniverseNode) o).id());
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
