package dev.chpg.pg.api;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Predicate;

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
                if (val == null || values == null || values.length == 0) { return false; }
                return java.util.Arrays.asList(values).contains(val);
            })
        );
    }

    @Override
    public EdgeSet withAnyTag(String... tags) {
        return new DeferredEdgeSet(
            this.source,
            this.combinedPredicate.and(edge -> {
                if (tags == null || tags.length == 0) { return false; }
                for (String tag : tags) {
                    if (edge.tags().contains(tag)) { return true; }
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
                if (tags == null || tags.length == 0) { return false; }
                for (String tag : tags) {
                    if (!edge.tags().contains(tag)) { return false; }
                }
                return true;
            })
        );
    }

    @Override
    public EdgeSet materialize() {
        Set<Edge> materialized = new java.util.HashSet<>();
        for (Edge e : this) {
            materialized.add(e);
        }
        return materialized.isEmpty() ? EdgeSet.empty() : new GenericImmutableEdgeSet(java.util.Collections.unmodifiableSet(materialized));
    }

    @Override
    public EdgeSet toImmutable() {
        return materialize();
    }

    @Override
    public java.util.Optional<Edge> one() {
        Iterator<Edge> it = iterator();
        if (it.hasNext()) { return java.util.Optional.of(it.next()); }
        return java.util.Optional.empty();
    }

    @Override
    public EdgeSet intersect(Collection<? extends Edge> other) {
        return new DeferredEdgeSet(
            this.source,
            this.combinedPredicate.and(other::contains)
        );
    }

    @Override
    public EdgeSet difference(Collection<? extends Edge> other) {
        return new DeferredEdgeSet(
            this.source,
            this.combinedPredicate.and(e -> !other.contains(e))
        );
    }

    @Override
    public EdgeSet union(Collection<? extends Edge> other) {
        return materialize().union(other);
    }

    @Override
    public Set<Integer> ids() {
        Set<Integer> ids = new java.util.HashSet<>((int) (size() / 0.75f) + 1);
        for (Edge edge : this) {
            ids.add(edge.id());
        }
        return java.util.Collections.unmodifiableSet(ids);
    }

    @Override
    public int[] toIdArray() {
        int[] result = new int[size()];
        int i = 0;
        for (Edge edge : this) {
            result[i++] = edge.id();
        }
        return result;
    }

    @Override
    public boolean contains(Object o) {
        if (!(o instanceof Edge)) { return false; }
        Edge e = (Edge) o;
        return source.contains(e) && combinedPredicate.test(e);
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
                if (nextEdge == null) { advance(); }
                return nextEdge != null;
            }

            @Override
            public Edge next() {
                if (!hasNext()) { throw new NoSuchElementException(); }
                Edge result = nextEdge;
                nextEdge = null;
                return result;
            }
        };
    }


    @Override
    public boolean isMaterialized() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return !iterator().hasNext();
    }
    /**
     * Computes the size of this deferred set by evaluating the pipeline.
     * <p>
     * <b>Warning:</b> This is an O(N) operation that iterates the source and tests the predicate.
     * Do not use in a loop condition (e.g. {@code for (int i = 0; i < set.size(); i++)}).
     */
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
