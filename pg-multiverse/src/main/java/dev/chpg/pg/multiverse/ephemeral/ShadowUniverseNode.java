package dev.chpg.pg.multiverse.ephemeral;

import dev.chpg.pg.api.AttributeMap;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.TagSet;
import dev.chpg.pg.multiverse.universe.Universe;
import dev.chpg.pg.multiverse.universe.UniverseNode;
import dev.chpg.pg.multiverse.universe.UniverseView;

/**
 * A transactional proxy for a permanent {@code UniverseNode} within an {@code EphemeralGraph}.
 * <p>
 * <b>What it represents:</b> A transparent wrapper around a baseline engine node that intercepts property mutations and routes them to a local delta log.
 * <p>
 * <b>Why it exists:</b> To allow an {@code EphemeralGraph} to function as a true, isolated write-buffer without polluting the underlying {@code Universe}.
 * <p>
 * <b>When to use it:</b> Instantiated automatically by the {@code EphemeralGraph} when a permanent node is accessed or traversed within a transaction.
 * <p>
 * <b>Common usage patterns:</b> Clients interact with this object exactly like any standard {@code Node}. Mutations to its tags or attributes are safely buffered.
 * <p>
 * <b>Important invariants:</b> Shares the exact same positive ID as its backing core element.
 * <p>
 * <b>Thread safety:</b> Not thread-safe. Concurrent structural or state mutations must be synchronized.
 * <p>
 * <b>Performance characteristics:</b> Reading properties incurs slight overhead to check local delta logs before falling back to the baseline engine arrays.
 */
public class ShadowUniverseNode implements Node, UniverseView {

    private final EphemeralGraph transactionContext;
    private final UniverseNode backingNode;

    /**
     * Constructs a new {@code ShadowUniverseNode}.
     *
     * @param context the transactional sandbox this proxy belongs to
     * @param backingNode the baseline node to wrap
     */
    public ShadowUniverseNode(EphemeralGraph context, UniverseNode backingNode) {
        this.transactionContext = context;
        this.backingNode = backingNode;
    }

    /**
     * Gets the transactional sandbox this proxy is bound to.
     *
     * @return the {@code EphemeralGraph} transaction
     */
    public EphemeralGraph transaction() { return transactionContext; }

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
