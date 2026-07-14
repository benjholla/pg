package dev.chpg.pg.multiverse.ephemeral;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import dev.chpg.pg.api.AttributeValue;
import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.EdgeSet;

public final class EphemeralEdgeSet implements EdgeSet {

    private final HashSet<EphemeralEdge> internalSet;

    public EphemeralEdgeSet() {
        this.internalSet = new HashSet<>();
    }

    public EphemeralEdgeSet(Edge initialEdge) {
        this();
        add(initialEdge);
    }

    public EphemeralEdgeSet(Edge... initialEdges) {
        this();
        Objects.requireNonNull(initialEdges, "Edge array cannot be null");
        addAll(Arrays.asList(initialEdges));
    }

    public EphemeralEdgeSet(Collection<Edge> initialEdges) {
        this();
        Objects.requireNonNull(initialEdges, "Edge collection cannot be null");
        addAll(initialEdges);
    }

    private EphemeralEdge validate(Edge edge) {
        Objects.requireNonNull(edge, "Edge cannot be null");
        if (!(edge instanceof EphemeralEdge impl)) {
            throw new IllegalArgumentException(
                "Cross-graph contamination: Expected EphemeralEdge, got " + edge.getClass().getSimpleName()
            );
        }
        EphemeralGuardrails.requireLocalId(impl.id());
        return impl;
    }

    @Override
    public EdgeSet toImmutable() {
        if (internalSet.isEmpty()) return EdgeSet.empty();
        if (internalSet.size() == 1) return new EphemeralImmutableSingletonEdgeSet(internalSet.iterator().next());
        return new EphemeralImmutableEdgeSet(new EphemeralEdgeSet(this));
    }

    @Override
    public Optional<Edge> one() {
        return internalSet.stream().map(e -> (Edge) e).findAny();
    }

    @Override
    public EdgeSet intersect(Collection<? extends Edge> other) {
        EphemeralEdgeSet result = new EphemeralEdgeSet();
        if (other == null || other.isEmpty()) {
            return result.isEmpty() ? EdgeSet.empty() : (result.size() == 1 ? new EphemeralImmutableSingletonEdgeSet((EphemeralEdge) result.iterator().next()) : new EphemeralImmutableEdgeSet(result));
        }
        for (EphemeralEdge edge : internalSet) {
            if (other.contains(edge)) {
                result.internalSet.add(edge);
            }
        }
        return result.isEmpty() ? EdgeSet.empty() : (result.size() == 1 ? new EphemeralImmutableSingletonEdgeSet((EphemeralEdge) result.iterator().next()) : new EphemeralImmutableEdgeSet(result));
    }

    @Override
    public EdgeSet difference(Collection<? extends Edge> other) {
        EphemeralEdgeSet result = new EphemeralEdgeSet();
        for (EphemeralEdge edge : internalSet) {
            if (other == null || !other.contains(edge)) {
                result.internalSet.add(edge);
            }
        }
        return result.isEmpty() ? EdgeSet.empty() : (result.size() == 1 ? new EphemeralImmutableSingletonEdgeSet((EphemeralEdge) result.iterator().next()) : new EphemeralImmutableEdgeSet(result));
    }

    @Override
    public EdgeSet union(Collection<? extends Edge> other) {
        EphemeralEdgeSet result = new EphemeralEdgeSet();
        result.internalSet.addAll(this.internalSet);
        if (other != null) {
            for (Edge e : other) {
                if (e instanceof EphemeralEdge ee) {
                    result.internalSet.add(ee);
                }
            }
        }
        return result.isEmpty() ? EdgeSet.empty() : (result.size() == 1 ? new EphemeralImmutableSingletonEdgeSet((EphemeralEdge) result.iterator().next()) : new EphemeralImmutableEdgeSet(result));
    }

    @Override
    public Set<Integer> ids() {
        Set<Integer> ids = new HashSet<>((int) (internalSet.size() / 0.75f) + 1);
        for (EphemeralEdge edge : internalSet) {
            ids.add(edge.id());
        }
        return ids;
    }

    @Override
    public int[] toIdArray() {
        int[] result = new int[internalSet.size()];
        int i = 0;
        for (Edge edge : internalSet) {
            result[i++] = edge.id();
        }
        return result;
    }

    @Override
    public boolean add(Edge edge) {
        return internalSet.add(validate(edge));
    }

    @Override
    public boolean contains(Object obj) {
        if (!(obj instanceof Edge edge)) return false;
        try {
            return internalSet.contains(validate(edge));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public boolean remove(Object obj) {
        if (!(obj instanceof Edge edge)) return false;
        try {
            return internalSet.remove(validate(edge));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    @Override
public boolean isMaterialized() {
        return true;
    }

    public int size() {
        return internalSet.size();
    }

    @Override
    public boolean isEmpty() {
        return internalSet.isEmpty();
    }

    @Override
    public void clear() {
        internalSet.clear();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Iterator<Edge> iterator() {
        return (Iterator<Edge>) (Iterator<?>) internalSet.iterator();
    }

    @Override
    public Object[] toArray() {
        return internalSet.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return internalSet.toArray(a);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        Objects.requireNonNull(c);
        for (Object obj : c) {
            if (!contains(obj)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends Edge> c) {
        Objects.requireNonNull(c, "Edge collection cannot be null");
        for (Edge e : c) {
            validate(e);
        }
        boolean modified = false;
        for (Edge e : c) {
            modified |= internalSet.add((EphemeralEdge) e);
        }
        return modified;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        Objects.requireNonNull(c);
        boolean modified = false;
        Iterator<EphemeralEdge> it = internalSet.iterator();
        while (it.hasNext()) {
            if (!c.contains(it.next())) {
                it.remove();
                modified = true;
            }
        }
        return modified;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        Objects.requireNonNull(c);
        boolean modified = false;
        for (Object obj : c) {
            modified |= this.remove(obj);
        }
        return modified;
    }

    @Override
    public String toString() {
        String joined = internalSet.stream()
                .map(Object::toString)
                .collect(Collectors.joining(", ", "[", "]"));
        return "EphemeralEdgeSet [edges=" + joined + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        // Standard Java semantics: safely compares sizes and elements,
        // evaluating to true for empty sets of different types,
        // while deferring to elements for populated sets.
        return internalSet.equals(o);
    }

    @Override
    public int hashCode() {
        return internalSet.hashCode();
    }
}
