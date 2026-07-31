package dev.chpg.pg.multiverse.ephemeral;

import dev.chpg.pg.api.AttributeMap;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.TagSet;
import dev.chpg.pg.multiverse.universe.Universe;
import dev.chpg.pg.multiverse.universe.UniverseNode;
import dev.chpg.pg.multiverse.universe.UniverseView;

/**
 * A transactional proxy wrapper over an existing {@link UniverseNode} to enforce mutation isolation.
 * <p>
 * <b>What it represents:</b> A node reference operating within the context of an {@link EphemeralGraph} transaction, preventing direct structural or property contamination of the global engine.
 * <p>
 * <b>Why it exists:</b> To intercept reads and writes directed at a core universe element so that mutations are routed to the transaction's Delta Log rather than the underlying columnar arrays.
 * <p>
 * <b>When to use it:</b> Instantiated automatically by the {@code EphemeralGraph} engine when yielding nodes from topology queries. Should not be manually constructed by users.
 * <p>
 * <b>Common usage patterns:</b> Consumers use standard {@link Node} capabilities. Property accessors ({@code tags()}, {@code attributes()}) return similarly shielded map/set proxies.
 * <p>
 * <b>Important invariants:</b> The {@code id()} remains strictly identical to the core node, allowing it to mathematically bridge the ephemeral and permanent topologies before promotion.
 * <p>
 * <b>Thread safety:</b> Inherits the thread-safety of the parent transaction (not thread-safe).
 * <p>
 * <b>Performance characteristics:</b> Incurs minimal GC overhead for proxy instantiation during traversal, which is an explicit trade-off to enable zero-pollution transactions.
 */
public class ShadowUniverseNode implements Node, UniverseView {

    private final EphemeralGraph transactionContext;
    private final UniverseNode backingNode;

    /**
     * Retrieves the isolated transaction context governing this proxy.
     *
     * @return the parent ephemeral graph
     */
    public EphemeralGraph transaction() { return transactionContext; }

    /**
     * Constructs a ShadowUniverseNode proxy.
     *
     * @param context the isolated graph context managing pending mutations
     * @param backingNode the core universe node acting as the read baseline
     */
    public ShadowUniverseNode(EphemeralGraph context, UniverseNode backingNode) {
        this.transactionContext = context;
        this.backingNode = backingNode;
    }

    @Override
    public int id() {
        return backingNode.id(); // ID remains identical
    }

    @Override
    public Universe universe() {
        return backingNode.universe();
    }

    @Override
    public TagSet tags() {
        // Returns a custom TagSet proxy that reads from backingNode.tags()
        // but routes all add/remove operations to transactionContext.pendingNodeTags
        return new ShadowTagSet(transactionContext, backingNode);
    }

    @Override
    public AttributeMap attributes() {
        // Routes reads through the local buffer first, falls back to backingNode
        // Routes writes strictly to transactionContext.pendingNodeAttributes
        return new ShadowAttributeMap(transactionContext, backingNode);
    }

    @Override
    public int hashCode() {
        return backingNode.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof ShadowUniverseNode shadow) {
            return this.backingNode.equals(shadow.backingNode);
        }
        return this.backingNode.equals(obj);
    }

    @Override
    public String toString() {
        return "ShadowUniverseNode [ attributes=" + this.attributes().toString() + ", tags=" + this.tags().toString() + "]";
    }
}
