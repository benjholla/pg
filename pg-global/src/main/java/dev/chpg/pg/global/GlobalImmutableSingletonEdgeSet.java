package dev.chpg.pg.global;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.EdgeSet;

public final class GlobalImmutableSingletonEdgeSet extends AbstractSet<Edge> implements EdgeSet {

    private final GlobalEdge element;

    public GlobalImmutableSingletonEdgeSet(GlobalEdge element) {
        this.element = Objects.requireNonNull(element, "element cannot be null");
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
        GlobalEdgeSet result = new GlobalEdgeSet();
        result.add(element);
        for (Edge e : other) {
            result.add((GlobalEdge) e);
        }
        if (result.size() == 1) {
            return this;
        }
        return new GlobalImmutableEdgeSet(result);
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
