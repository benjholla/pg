package dev.chpg.pg.global;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.NodeSet;

/**
 * An immutable view of a single-element {@link NodeSet} in the global graph ecosystem.
 * <p>
 * <b>What it represents:</b> A highly optimized, read-only collection of exactly one {@link GlobalNode}.
 * <p>
 * <b>Why it exists:</b> To eliminate the memory allocation overhead of a backing HashSet when returning single nodes (e.g., from functional algebra).
 * <p>
 * <b>When to use it:</b> Used internally by the engine when a query or set operation returns exactly one node.
 * <p>
 * <b>Important invariants:</b> Inherits {@link AbstractSet} behavior which blocks mutations.
 */
public final class GlobalImmutableSingletonNodeSet extends AbstractSet<Node> implements NodeSet {

    private final GlobalNode element;

    /**
     * Constructs a new singleton node set containing the specified element.
     *
     * @param element the single global node
     */
    public GlobalImmutableSingletonNodeSet(GlobalNode element) {
        this.element = Objects.requireNonNull(element, "element cannot be null");
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

    public int size() {
        return 1;
    }

    @Override
    public boolean contains(Object o) {
        return element.equals(o);
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
        GlobalNodeSet result = new GlobalNodeSet();
        result.add(element);
        for (Node n : other) {
            result.add((GlobalNode) n);
        }
        if (result.size() == 1) {
            return this;
        }
        return result.asSealed();
    }

    @Override
    public Set<Integer> ids() {
        return Collections.singleton(element.id());
    }

    @Override
    public int[] toIdArray() {
        return new int[]{element.id()};
    }


}
