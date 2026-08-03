package dev.chpg.pg.multiverse.ephemeral;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.EdgeSet;

/** Immutable singleton set of an EphemeralEdge or ShadowEdge. */
public final class EphemeralImmutableSingletonEdgeSet extends AbstractSet<Edge> implements EdgeSet {

    private final Edge element;

    /**
     * Constructs a new EphemeralImmutableSingletonEdgeSet.
     * @param element the single element
     */
    public EphemeralImmutableSingletonEdgeSet(Edge element) {
        this.element = Objects.requireNonNull(element, "element cannot be null");
    }

    // --- The Firewall ---
    private Edge validate(Edge edge) {
        Objects.requireNonNull(edge, "Edge cannot be null");
        if (!(edge instanceof EphemeralEdge) && !(edge.getClass().getSimpleName().contains("Shadow"))) {
            throw new IllegalArgumentException(
                "Cross-graph contamination: Expected EphemeralEdge or ShadowEdge, got " + edge.getClass().getSimpleName()
            );
        }
        if (edge.id() >= 0) {
            throw new IllegalArgumentException(
                "Topological violation: Local adjacency sets can only store brand-new transaction edges (negative IDs)."
            );
        }
        return edge;
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
    public EdgeSet intersect(Collection<? extends Edge> other) {
        Objects.requireNonNull(other, "other cannot be null");

        // Pre-flight Fail-Fast Validation
        for (Edge e : other) {
            this.validate(e);
        }

        if (other.contains(element)) {
            return this;
        }
        return EdgeSet.empty();
    }

    @Override
    public EdgeSet difference(Collection<? extends Edge> other) {
        Objects.requireNonNull(other, "other cannot be null");

        // Pre-flight Fail-Fast Validation
        for (Edge e : other) {
            this.validate(e);
        }

        if (other.contains(element)) {
            return EdgeSet.empty();
        }
        return this;
    }

    @Override
    public EdgeSet union(Collection<? extends Edge> other) {
        Objects.requireNonNull(other, "other cannot be null");
        EphemeralEdgeSet result = new EphemeralEdgeSet();

        result.add(element);
        for (Edge e : other) {
            // EphemeralEdgeSet.add() naturally applies the firewall here
            result.add(e);
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
