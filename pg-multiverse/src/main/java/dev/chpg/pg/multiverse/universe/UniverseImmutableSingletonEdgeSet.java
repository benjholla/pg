package dev.chpg.pg.multiverse.universe;

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.EdgeSet;

import java.util.AbstractSet;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * An immutable view of a single-element {@link EdgeSet} in the closed Universe ecosystem.
 * <p>
 * <b>What it represents:</b> A highly optimized, read-only collection of exactly one {@link UniverseEdge}.
 * <p>
 * <b>Why it exists:</b> To eliminate the memory allocation overhead of bit-masks and viewports when returning single edges (e.g., from functional algebra).
 * <p>
 * <b>Important invariants:</b> Inherits {@link AbstractSet} behavior which blocks mutations.
 * Strictly rejects cross-contamination from foreign graphs during set operations.
 */
public final class UniverseImmutableSingletonEdgeSet extends AbstractSet<Edge> implements EdgeSet, UniverseView {

    private final UniverseEdge element;

    /**
     * Constructs a new singleton edge set containing the specified element.
     *
     * @param element the single universe edge
     */
    public UniverseImmutableSingletonEdgeSet(UniverseEdge element) {
        this.element = Objects.requireNonNull(element, "element cannot be null");
    }

    @Override
    public Universe universe() {
        return this.element.universe();
    }

    @Override
    public EdgeSet toImmutable() {
        return this;
    }

    @Override
    public EdgeSet materialize() {
        return this;
    }

    @Override
    public boolean isMaterialized() {
        return true;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public boolean contains(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UniverseEdge)) {
            return false;
        }

        UniverseEdge uEdge = (UniverseEdge) o;

        // Strictly enforce engine boundaries using CPU pointer equality
        return uEdge.universe() == this.universe() && uEdge.id() == this.element.id();
    }

    @Override
    public Iterator<Edge> iterator() {
        return Collections.<Edge>singleton(element).iterator();
    }

    @Override
    public Optional<Edge> one() {
        return Optional.of(element);
    }

    @Override
    public EdgeSet intersect(Collection<? extends Edge> other) {
        java.util.Objects.requireNonNull(other, "other cannot be null");
        if (other.contains(element)) {
            return this;
        }
        return EdgeSet.empty();
    }

    @Override
    public EdgeSet difference(Collection<? extends Edge> other) {
        java.util.Objects.requireNonNull(other, "other cannot be null");
        if (other.contains(element)) {
            return EdgeSet.empty();
        }
        return this;
    }

    @Override
    public EdgeSet union(Collection<? extends Edge> other) {
        java.util.Objects.requireNonNull(other, "other cannot be null");

        if (other.isEmpty()) {
            return this;
        }

        BitSet mergedBits = new BitSet();
        mergedBits.set(this.element.id());

        // Strictly enforce lineage across the incoming collection
        for (Edge e : other) {
            if (!(e instanceof UniverseEdge)) {
                throw new IllegalArgumentException("Cannot union with foreign edge. Must be a UniverseEdge.");
            }
            UniverseEdge uEdge = (UniverseEdge) e;
            if (uEdge.universe() != this.universe()) {
                throw new IllegalArgumentException("Cannot union with a UniverseEdge from a different Universe instance.");
            }
            mergedBits.set(uEdge.id());
        }

        // If the other set only contained duplicates of our single element
        if (mergedBits.cardinality() == 1) {
            return this;
        }

        // Promote to a standard bitwise viewport to hold multiple edges
        return new UniverseEdgeSet(this.universe(), mergedBits);
    }

    @Override
    public Set<Integer> ids() {
        return new AbstractSet<Integer>() {
            @Override
            public Iterator<Integer> iterator() {
                return Collections.singleton(element.id()).iterator();
            }

            @Override
            public int size() {
                return 1;
            }

            @Override
            public boolean contains(Object o) {
                if (!(o instanceof Integer)) {
                    return false;
                }
                return (Integer) o == element.id();
            }
        };
    }

    @Override
    public int[] toIdArray() {
        return new int[]{element.id()};
    }
}
