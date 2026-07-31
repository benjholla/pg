package dev.chpg.pg.multiverse.ephemeral;

import dev.chpg.pg.api.AttributeMap;
import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.TagSet;
import dev.chpg.pg.multiverse.universe.Universe;
import dev.chpg.pg.multiverse.universe.UniverseEdge;
import dev.chpg.pg.multiverse.universe.UniverseView;

/**
 * A transactional proxy wrapper over an existing {@link Edge} to enforce mutation isolation.
 * <p>
 * <b>What it represents:</b> An edge reference operating within the context of an {@link EphemeralGraph} transaction, bridging the gap between pending ephemeral state and persistent universe state.
 * <p>
 * <b>Why it exists:</b> To intercept reads and writes to an underlying {@code UniverseEdge} (or re-wrap an {@code EphemeralEdge}) so that mutations are routed to the transaction's Delta Log rather than the shared {@code Universe}.
 * <p>
 * <b>When to use it:</b> Instantiated automatically by the {@code EphemeralGraph} engine when traversing topologies. Should not be manually constructed by users.
 * <p>
 * <b>Common usage patterns:</b> Exposes standard {@link Edge} capabilities (e.g., {@code tags()}, {@code attributes()}, {@code from()}, {@code to()}). Topology queries return similarly shielded elements.
 * <p>
 * <b>Important invariants:</b> The {@code id()} remains identical to the underlying edge. If the backing edge is a core {@code UniverseEdge}, properties are wrapped in Shadow proxies. Endpoints (source/target) are lazily re-wrapped into the transaction context.
 * <p>
 * <b>Thread safety:</b> Inherits the thread-safety of the parent transaction (not thread-safe).
 * <p>
 * <b>Performance characteristics:</b> Incurs minimal GC overhead for proxy instantiation during traversal, which is an explicit trade-off to enable zero-pollution transactions over bit-mask engines.
 */
public class ShadowEdge implements Edge, UniverseView {

    private final EphemeralGraph transaction;
    private final Edge backingEdge;

    /**
     * Constructs a ShadowEdge proxy.
     *
     * @param transaction the isolated graph context managing pending mutations
     * @param backingEdge the core universe edge or ephemeral edge acting as the read baseline
     */
    public ShadowEdge(EphemeralGraph transaction, Edge backingEdge) {
        this.transaction = transaction;
        this.backingEdge = backingEdge;
    }

    /**
     * Retrieves the unshielded, core engine element underlying this proxy.
     *
     * @return the raw backing edge
     */
    public Edge backingEdge() { return backingEdge; }

    /**
     * Retrieves the isolated transaction context governing this proxy.
     *
     * @return the parent ephemeral graph
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
