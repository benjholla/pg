package dev.chpg.pg.multiverse.ephemeral;

import dev.chpg.pg.api.AttributeMap;
import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.TagSet;
import dev.chpg.pg.multiverse.universe.Universe;
import dev.chpg.pg.multiverse.universe.UniverseEdge;
import dev.chpg.pg.multiverse.universe.UniverseView;

/**
 * A transactional proxy for a permanent {@code UniverseEdge} within an {@code EphemeralGraph}.
 * <p>
 * <b>What it represents:</b> A transparent wrapper around a baseline engine edge that intercepts property mutations and routes them to a local delta log.
 * <p>
 * <b>Why it exists:</b> To allow an {@code EphemeralGraph} to function as a true, isolated write-buffer without polluting the underlying {@code Universe}.
 * <p>
 * <b>When to use it:</b> Instantiated automatically by the {@code EphemeralGraph} when a permanent edge is accessed or traversed within a transaction.
 * <p>
 * <b>Common usage patterns:</b> Clients interact with this object exactly like any standard {@code Edge}. Mutations to its tags or attributes are safely buffered.
 * <p>
 * <b>Important invariants:</b> Shares the exact same positive ID as its backing core element. Lazily wraps its endpoint nodes in corresponding shadow proxies when traversed.
 * <p>
 * <b>Thread safety:</b> Not thread-safe. Concurrent structural or state mutations must be synchronized.
 * <p>
 * <b>Performance characteristics:</b> Reading properties incurs slight overhead to check local delta logs before falling back to the baseline engine arrays.
 */
public class ShadowEdge implements Edge, UniverseView {

    private final EphemeralGraph transaction;
    private final Edge backingEdge;

    /**
     * Constructs a new {@code ShadowEdge}.
     *
     * @param transaction the transactional sandbox this proxy belongs to
     * @param backingEdge the baseline edge to wrap
     */
    public ShadowEdge(EphemeralGraph transaction, Edge backingEdge) {
        this.transaction = transaction;
        this.backingEdge = backingEdge;
    }

    /**
     * Gets the backing baseline edge.
     *
     * @return the unproxied baseline edge
     */
    public Edge backingEdge() { return backingEdge; }

    /**
     * Gets the transactional sandbox this proxy is bound to.
     *
     * @return the {@code EphemeralGraph} transaction
     */
    public EphemeralGraph transaction() {
        return transaction;
    }

    @Override
    public int id() {
        return backingEdge.id();
    }

    @Override
    public Universe universe() {
        if (backingEdge instanceof UniverseView) {
            return ((UniverseView) backingEdge).universe();
        }
        return transaction.universe();
    }

    @Override
    public Node from() {
        // Lazily shield the endpoint
        return transaction.validateAndWrap(backingEdge.from());
    }

    @Override
    public Node to() {
        // Lazily shield the endpoint
        return transaction.validateAndWrap(backingEdge.to());
    }

    @Override
    public TagSet tags() {
        // Route mutations to the delta log ONLY if it's a core universe edge
        if (backingEdge instanceof UniverseEdge universeEdge) {
            return new ShadowTagSet(transaction, universeEdge);
        }

        // If it's an EphemeralEdge, its own internal TagSet is perfectly safe to mutate
        return backingEdge.tags();
    }

    @Override
    public AttributeMap attributes() {
        // Route mutations to the delta log ONLY if it's a core universe edge
        if (backingEdge instanceof UniverseEdge universeEdge) {
            return new ShadowAttributeMap(transaction, universeEdge);
        }

        // EphemeralEdges safely hold their own attributes until promotion
        return backingEdge.attributes();
    }

    @Override
    public int hashCode() {
        return backingEdge.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof ShadowEdge shadow) {
            return this.backingEdge.equals(shadow.backingEdge);
        }
        return this.backingEdge.equals(obj);
    }

    @Override
    public String toString() {
        return "ShadowEdge [from=" + from() + ", to=" + to() + ", attributes=" + this.attributes().toString() + ", tags=" + this.tags().toString() + "]";
    }
}
