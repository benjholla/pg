package io.github.benjholla.pg.multiverse.ephemeral;

import io.github.benjholla.pg.api.AttributeValue;
import io.github.benjholla.pg.api.Edge;
import io.github.benjholla.pg.api.EdgeSet;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class EphemeralImmutableSingletonEdgeSet extends AbstractSet<Edge> implements EdgeSet {

    private final EphemeralEdge element;

    public EphemeralImmutableSingletonEdgeSet(EphemeralEdge element) {
        this.element = Objects.requireNonNull(element, "element cannot be null");
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public boolean contains(Object o) {
        return element.equals(o);
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
    public EdgeSet filter(String attribute) {
        if (element.attributes().containsKey(attribute)) {
            return this;
        }
        return new EphemeralImmutableEdgeSet(new EphemeralEdgeSet()); // return empty edge set
    }

    @Override
    public EdgeSet filter(String attribute, AttributeValue... values) {
        if (element.attributes().containsKey(attribute)) {
            AttributeValue attrValue = element.attributes().get(attribute);
            for (AttributeValue v : values) {
                if (attrValue.equals(v)) {
                    return this;
                }
            }
        }
        return new EphemeralImmutableEdgeSet(new EphemeralEdgeSet()); // return empty edge set
    }

    @Override
    public EdgeSet intersect(Collection<? extends Edge> other) {
        if (other.contains(element)) {
            return this;
        }
        return new EphemeralImmutableEdgeSet(new EphemeralEdgeSet());
    }

    @Override
    public EdgeSet difference(Collection<? extends Edge> other) {
        if (other.contains(element)) {
            return new EphemeralImmutableEdgeSet(new EphemeralEdgeSet());
        }
        return this;
    }

    @Override
    public EdgeSet union(Collection<? extends Edge> other) {
        EphemeralEdgeSet result = new EphemeralEdgeSet();
        result.add(element);
        for (Edge e : other) {
            result.add((EphemeralEdge) e);
        }
        if (result.size() == 1) {
            return this;
        }
        return new EphemeralImmutableEdgeSet(result);
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
