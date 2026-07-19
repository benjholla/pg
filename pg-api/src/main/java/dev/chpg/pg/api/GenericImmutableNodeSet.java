package dev.chpg.pg.api;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

/**
 * Immutable NodeSet.
 */
public final class GenericImmutableNodeSet extends AbstractSet<Node> implements NodeSet {

    private final Set<Node> elements;

    /**
     * Constructs a new GenericImmutableNodeSet from the given source set.
     * @param elements the source set
     */
    /**
     * Constructs a new GenericImmutableNodeSet from the given source set.
     * @param elements the source set
     */
    public GenericImmutableNodeSet(Collection<? extends Node> elements) {
        this.elements = Set.copyOf(elements);
    }

    @Override
    public NodeSet materialize() {
        return this;
    }

    @Override
    public NodeSet toImmutable() {
        return this;
    }
    @Override
public boolean isMaterialized() {
        return true;
    }

    public int size() {
        return elements.size();
    }

    @Override
    public boolean contains(Object o) {
        return elements.contains(o);
    }

    @Override
    public Iterator<Node> iterator() {
        return elements.iterator();
    }

    @Override
    public Optional<Node> one() {
        if (elements.isEmpty()) { return Optional.empty(); }
        return Optional.of(elements.iterator().next());
    }

    @Override
    public NodeSet intersect(Collection<? extends Node> other) {
        java.util.Objects.requireNonNull(other, "other cannot be null");
        if (other.isEmpty()) {
            return NodeSet.empty();
        }
        Set<Node> intersected = new HashSet<>();
        for (Node n : elements) {
            if (other.contains(n)) {
                intersected.add(n);
            }
        }
        return intersected.isEmpty() ? NodeSet.empty() : new GenericImmutableNodeSet(Collections.unmodifiableSet(intersected));
    }

    @Override
    public NodeSet difference(Collection<? extends Node> other) {
        java.util.Objects.requireNonNull(other, "other cannot be null");
        if (other.isEmpty()) {
            return this;
        }
        Set<Node> differenced = new HashSet<>();
        for (Node n : elements) {
            if (!other.contains(n)) {
                differenced.add(n);
            }
        }
        return differenced.isEmpty() ? NodeSet.empty() : new GenericImmutableNodeSet(Collections.unmodifiableSet(differenced));
    }

    @Override
    public NodeSet union(Collection<? extends Node> other) {
        java.util.Objects.requireNonNull(other, "other cannot be null");
        if (other.isEmpty()) {
            return this;
        }
        Set<Node> unioned = new HashSet<>((int) ((elements.size() + other.size()) / 0.75f) + 1);
        unioned.addAll(elements);
        unioned.addAll(other);
        return new GenericImmutableNodeSet(Collections.unmodifiableSet(unioned));
    }

    @Override
    public Set<Integer> ids() {
        Set<Integer> ids = new HashSet<>((int) (elements.size() / 0.75f) + 1);
        for (Node node : elements) {
            ids.add(node.id());
        }
        return Collections.unmodifiableSet(ids);
    }

    @Override
    public int[] toIdArray() {
        int[] result = new int[elements.size()];
        int i = 0;
        for (Node node : elements) {
            result[i++] = node.id();
        }
        return result;
    }
}
