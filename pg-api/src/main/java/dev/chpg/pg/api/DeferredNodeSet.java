package dev.chpg.pg.api;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class DeferredNodeSet extends AbstractSet<Node> implements NodeSet {
    private final NodeSet source;
    private final Predicate<Node> combinedPredicate;

    public DeferredNodeSet(NodeSet source, Predicate<Node> initialPredicate) {
        this.source = source;
        this.combinedPredicate = initialPredicate;
    }

    @Override
    public NodeSet withAttribute(String attribute) {
        return new DeferredNodeSet(
            this.source,
            this.combinedPredicate.and(node -> node.attributes().containsKey(attribute))
        );
    }

    @Override
    public NodeSet withAttribute(String attribute, AttributeValue... values) {
        return new DeferredNodeSet(
            this.source,
            this.combinedPredicate.and(node -> {
                AttributeValue val = node.attributes().get(attribute);
                if (val == null || values == null || values.length == 0) return false;
                return java.util.Arrays.asList(values).contains(val);
            })
        );
    }

    @Override
    public NodeSet withAnyTag(String... tags) {
        return new DeferredNodeSet(
            this.source,
            this.combinedPredicate.and(node -> {
                if (tags == null || tags.length == 0) return false;
                for (String tag : tags) {
                    if (node.tags().contains(tag)) return true;
                }
                return false;
            })
        );
    }

    @Override
    public NodeSet withAllTags(String... tags) {
        return new DeferredNodeSet(
            this.source,
            this.combinedPredicate.and(node -> {
                if (tags == null || tags.length == 0) return false;
                for (String tag : tags) {
                    if (!node.tags().contains(tag)) return false;
                }
                return true;
            })
        );
    }

    @Override
    public NodeSet materialize() {
        Set<Node> materialized = stream().collect(Collectors.toUnmodifiableSet());
        return materialized.isEmpty() ? NodeSet.empty() : new GenericImmutableNodeSet(materialized);
    }

    @Override
    public NodeSet toImmutable() {
        return materialize();
    }

    @Override
    public java.util.Optional<Node> one() {
        return stream().findAny();
    }

    @Override
    public NodeSet intersect(Collection<? extends Node> other) {
        return new DeferredNodeSet(
            this.source,
            this.combinedPredicate.and(other::contains)
        );
    }

    @Override
    public NodeSet difference(Collection<? extends Node> other) {
        return new DeferredNodeSet(
            this.source,
            this.combinedPredicate.and(n -> !other.contains(n))
        );
    }

    @Override
    public NodeSet union(Collection<? extends Node> other) {
        return materialize().union(other);
    }

    @Override
    public Set<Integer> ids() {
        return stream().map(Node::id).collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public int[] toIdArray() {
        return stream().mapToInt(Node::id).toArray();
    }

    @Override
    public boolean contains(Object o) {
        if (!(o instanceof Node)) return false;
        Node e = (Node) o;
        return source.contains(e) && combinedPredicate.test(e);
    }

    @Override
    public Iterator<Node> iterator() {
        Iterator<Node> sourceIterator = source.iterator();

        return new Iterator<Node>() {
            private Node nextNode = null;

            private void advance() {
                while (nextNode == null && sourceIterator.hasNext()) {
                    Node candidate = sourceIterator.next();
                    if (combinedPredicate.test(candidate)) {
                        nextNode = candidate;
                    }
                }
            }

            @Override
            public boolean hasNext() {
                if (nextNode == null) advance();
                return nextNode != null;
            }

            @Override
            public Node next() {
                if (!hasNext()) throw new NoSuchElementException();
                Node result = nextNode;
                nextNode = null;
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
        Iterator<Node> it = iterator();
        while (it.hasNext()) {
            it.next();
            count++;
        }
        return count;
    }
}
