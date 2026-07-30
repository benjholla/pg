package dev.chpg.pg.multiverse.ephemeral;

import dev.chpg.pg.api.AttributeMap;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.TagSet;
import dev.chpg.pg.multiverse.universe.Universe;
import dev.chpg.pg.multiverse.universe.UniverseNode;
import dev.chpg.pg.multiverse.universe.UniverseView;

/**
 * Represents a shadow proxy for a UniverseNode within an EphemeralGraph transaction.
 *
 * <p><b>What it represents:</b> A transparent wrapper around a permanent universe node that routes property mutations to a local ephemeral transaction buffer.
 * <p><b>Why it exists:</b> It allows universe nodes to be read and virtually modified within an ephemeral sandbox without mutating the permanent core graph.
 * <p><b>When to use it:</b> It is used internally when an ephemeral graph needs to present a universe node as part of its local view.
 */
public class ShadowUniverseNode implements Node, UniverseView {

    private final EphemeralGraph transactionContext;
    private final UniverseNode backingNode;

    /**
     * Constructs a new shadow universe node.
     * @param context the ephemeral transaction context
     * @param backingNode the underlying permanent universe node
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
