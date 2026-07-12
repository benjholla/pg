package dev.chpg.pg.api;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public final class GenericImmutableNodeSet extends AbstractSet<Node> implements NodeSet {

    private final Set<Node> elements;

    public GenericImmutableNodeSet(Collection<? extends Node> elements) {
        this.elements = Set.copyOf(elements);
    }

    @Override
    public NodeSet toImmutable() {
        return this;
    }

    @Override
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
        return elements.stream().findAny();
    }

    @Override
    public NodeSet filter(String attribute) {
        Set<Node> filtered = elements.stream()
            .filter(n -> n.attributes().containsKey(attribute))
            .collect(Collectors.toUnmodifiableSet());
        return filtered.isEmpty() ? NodeSet.empty() : new GenericImmutableNodeSet(filtered);
    }

    @Override
    public NodeSet filter(String attribute, AttributeValue... values) {
        Set<Node> filtered = elements.stream()
            .filter(n -> {
                AttributeValue val = n.attributes().get(attribute);
                if (val == null) return false;
                for (AttributeValue v : values) {
                    if (Objects.equals(val, v)) return true;
                }
                return false;
            })
            .collect(Collectors.toUnmodifiableSet());
        return filtered.isEmpty() ? NodeSet.empty() : new GenericImmutableNodeSet(filtered);
    }

    @Override
    public NodeSet intersect(Collection<? extends Node> other) {
        if (other == null || other.isEmpty()) {
            return NodeSet.empty();
        }
        Set<Node> intersected = elements.stream()
            .filter(other::contains)
            .collect(Collectors.toUnmodifiableSet());
        return intersected.isEmpty() ? NodeSet.empty() : new GenericImmutableNodeSet(intersected);
    }

    @Override
    public NodeSet difference(Collection<? extends Node> other) {
        if (other == null || other.isEmpty()) {
            return this;
        }
        Set<Node> differenced = elements.stream()
            .filter(n -> !other.contains(n))
            .collect(Collectors.toUnmodifiableSet());
        return differenced.isEmpty() ? NodeSet.empty() : new GenericImmutableNodeSet(differenced);
    }

    @Override
    public NodeSet union(Collection<? extends Node> other) {
        if (other == null || other.isEmpty()) {
            return this;
        }
        Set<Node> unioned = java.util.stream.Stream.concat(elements.stream(), other.stream())
            .collect(Collectors.toUnmodifiableSet());
        return new GenericImmutableNodeSet(unioned);
    }

    @Override
    public Set<Integer> ids() {
        return elements.stream().map(Node::id).collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public int[] toIdArray() {
        return elements.stream().mapToInt(Node::id).toArray();
    }

    @Override
    public NodeSet taggedWithAny(String... tags) {
        Set<Node> filtered = elements.stream()
            .filter(e -> {
                if (tags == null || tags.length == 0) return false;
                for (String tag : tags) {
                    if (e.tags().contains(tag)) return true;
                }
                return false;
            })
            .collect(Collectors.toUnmodifiableSet());
        return filtered.isEmpty() ? NodeSet.empty() : new GenericImmutableNodeSet(filtered);
    }

    @Override
    public NodeSet taggedWithAll(String... tags) {
        Set<Node> filtered = elements.stream()
            .filter(e -> {
                if (tags == null || tags.length == 0) return false;
                for (String tag : tags) {
                    if (!e.tags().contains(tag)) return false;
                }
                return true;
            })
            .collect(Collectors.toUnmodifiableSet());
        return filtered.isEmpty() ? NodeSet.empty() : new GenericImmutableNodeSet(filtered);
    }
}
