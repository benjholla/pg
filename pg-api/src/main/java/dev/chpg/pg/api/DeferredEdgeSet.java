package dev.chpg.pg.api;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class DeferredEdgeSet extends AbstractSet<Edge> implements EdgeSet {
    private final EdgeSet source;
    private final Predicate<Edge> combinedPredicate;

    public DeferredEdgeSet(EdgeSet source, Predicate<Edge> initialPredicate) {
        this.source = source;
        this.combinedPredicate = initialPredicate;
    }

    @Override
    public EdgeSet withAttribute(String attribute) {
        return new DeferredEdgeSet(
            this.source,
            this.combinedPredicate.and(edge -> edge.attributes().containsKey(attribute))
        );
    }

    @Override
    public EdgeSet withAttribute(String attribute, AttributeValue... values) {
        return new DeferredEdgeSet(
            this.source,
            this.combinedPredicate.and(edge -> {
                AttributeValue val = edge.attributes().get(attribute);
                if (val == null || values == null || values.length == 0) return false;
                for (AttributeValue v : values) {
                    if (java.util.Objects.equals(val, v)) return true;
                }
                return false;
            })
        );
    }

    @Override
    public EdgeSet withAnyTag(String... tags) {
        return new DeferredEdgeSet(
            this.source,
            this.combinedPredicate.and(edge -> {
                if (tags == null || tags.length == 0) return false;
                for (String tag : tags) {
                    if (edge.tags().contains(tag)) return true;
                }
                return false;
            })
        );
    }

    @Override
    public EdgeSet withAllTags(String... tags) {
        return new DeferredEdgeSet(
            this.source,
            this.combinedPredicate.and(edge -> {
                if (tags == null || tags.length == 0) return false;
                for (String tag : tags) {
                    if (!edge.tags().contains(tag)) return false;
                }
                return true;
            })
        );
    }

    @Override
    public EdgeSet materialize() {
        Set<Edge> materialized = stream().collect(Collectors.toUnmodifiableSet());
        return materialized.isEmpty() ? EdgeSet.empty() : new GenericImmutableEdgeSet(materialized);
    }

    @Override
    public EdgeSet toImmutable() {
        return materialize();
    }

    @Override
    public java.util.Optional<Edge> one() {
        return stream().findAny();
    }

    @Override
    public EdgeSet intersect(Collection<? extends Edge> other) {
        return materialize().intersect(other);
    }

    @Override
    public EdgeSet difference(Collection<? extends Edge> other) {
        return materialize().difference(other);
    }

    @Override
    public EdgeSet union(Collection<? extends Edge> other) {
        return materialize().union(other);
    }

    @Override
    public Set<Integer> ids() {
        return stream().map(Edge::id).collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public int[] toIdArray() {
        return stream().mapToInt(Edge::id).toArray();
    }

    @Override
    public Iterator<Edge> iterator() {
        Iterator<Edge> sourceIterator = source.iterator();

        return new Iterator<Edge>() {
            private Edge nextEdge = null;

            private void advance() {
                while (nextEdge == null && sourceIterator.hasNext()) {
                    Edge candidate = sourceIterator.next();
                    if (combinedPredicate.test(candidate)) {
                        nextEdge = candidate;
                    }
                }
            }

            @Override
            public boolean hasNext() {
                if (nextEdge == null) advance();
                return nextEdge != null;
            }

            @Override
            public Edge next() {
                if (!hasNext()) throw new NoSuchElementException();
                Edge result = nextEdge;
                nextEdge = null;
                return result;
            }
        };
    }

    @Override
    public int size() {
        int count = 0;
        Iterator<Edge> it = iterator();
        while (it.hasNext()) {
            it.next();
            count++;
        }
        return count;
    }
}
