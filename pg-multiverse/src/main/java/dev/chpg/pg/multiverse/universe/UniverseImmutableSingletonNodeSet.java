package dev.chpg.pg.multiverse.universe;

import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.NodeSet;

import java.util.AbstractSet;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * An immutable view of a single-element {@link NodeSet} in the closed Universe ecosystem.
 * <p>
 * <b>What it represents:</b> A highly optimized, read-only collection of exactly one {@link UniverseNode}.
 * <p>
 * <b>Why it exists:</b> To eliminate the memory allocation overhead of bit-masks and viewports when returning single nodes (e.g., from functional algebra).
 * <p>
 * <b>Important invariants:</b> Inherits {@link AbstractSet} behavior which blocks mutations.
 * Strictly rejects cross-contamination from foreign graphs during set operations.
 */
public final class UniverseImmutableSingletonNodeSet extends AbstractSet<Node> implements NodeSet, UniverseView {

    private final UniverseNode element;

    /**
     * Constructs a new singleton node set containing the specified element.
     *
     * @param element the single universe node
     */
    public UniverseImmutableSingletonNodeSet(UniverseNode element) {
        this.element = Objects.requireNonNull(element, "element cannot be null");
    }

    @Override
    public Universe universe() {
        return this.element.universe();
    }

    @Override
    public NodeSet toImmutable() {
        return this;
    }

    @Override
    public NodeSet materialize() {
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
        if (!(o instanceof UniverseNode)) {
            return false;
        }

        UniverseNode uNode = (UniverseNode) o;

        // Strictly enforce engine boundaries using CPU pointer equality
        return uNode.universe() == this.universe() && uNode.id() == this.element.id();
    }

    @Override
    public Iterator<Node> iterator() {
        return Collections.<Node>singleton(element).iterator();
    }

    @Override
    public Optional<Node> one() {
        return Optional.of(element);
    }

    @Override
    public NodeSet intersect(Collection<? extends Node> other) {
        java.util.Objects.requireNonNull(other, "other cannot be null");
        if (other.contains(element)) {
            return this;
        }
        return NodeSet.empty();
    }

    @Override
    public NodeSet difference(Collection<? extends Node> other) {
        java.util.Objects.requireNonNull(other, "other cannot be null");
        if (other.contains(element)) {
            return NodeSet.empty();
        }
        return this;
    }

    @Override
    public NodeSet union(Collection<? extends Node> other) {
        java.util.Objects.requireNonNull(other, "other cannot be null");

        if (other.isEmpty()) {
            return this;
        }

        BitSet mergedBits = new BitSet();
        mergedBits.set(this.element.id());

        // Strictly enforce lineage across the incoming collection
        for (Node n : other) {
            if (!(n instanceof UniverseNode)) {
                throw new IllegalArgumentException("Cannot union with foreign node. Must be a UniverseNode.");
            }
            UniverseNode uNode = (UniverseNode) n;
            if (uNode.universe() != this.universe()) {
                throw new IllegalArgumentException("Cannot union with a UniverseNode from a different Universe instance.");
            }
            mergedBits.set(uNode.id());
        }

        // If the other set only contained duplicates of our single element
        if (mergedBits.cardinality() == 1) {
            return this;
        }

        // Promote to a standard bitwise viewport to hold multiple nodes
        return new UniverseNodeSet(this.universe(), mergedBits);
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
