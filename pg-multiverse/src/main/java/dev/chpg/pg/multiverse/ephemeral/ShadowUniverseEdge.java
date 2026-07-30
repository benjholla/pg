package dev.chpg.pg.multiverse.ephemeral;

import dev.chpg.pg.api.AttributeMap;
import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.TagSet;
import dev.chpg.pg.multiverse.universe.Universe;
import dev.chpg.pg.multiverse.universe.UniverseEdge;
import dev.chpg.pg.multiverse.universe.UniverseNode;
import dev.chpg.pg.multiverse.universe.UniverseView;

/**
 * Represents a shadow proxy for a UniverseEdge within an EphemeralGraph transaction.
 *
 * <p><b>What it represents:</b> A transparent wrapper around a permanent universe edge that routes property mutations to a local ephemeral transaction buffer.
 * <p><b>Why it exists:</b> It allows universe edges to be read and virtually modified within an ephemeral sandbox without mutating the permanent core graph.
 * <p><b>When to use it:</b> It is used internally when an ephemeral graph needs to present a universe edge as part of its local view.
 */
public class ShadowUniverseEdge implements Edge, UniverseView {

    private final EphemeralGraph transactionContext;
    private final UniverseEdge backingEdge;

    /**
     * Constructs a new shadow universe edge.
     * @param context the ephemeral transaction context
     * @param backingEdge the underlying permanent universe edge
     */
    public ShadowUniverseEdge(EphemeralGraph context, UniverseEdge backingEdge) {
        this.transactionContext = context;
        this.backingEdge = backingEdge;
    }

    @Override
    public int id() {
        return backingEdge.id();
    }

    @Override
    public Universe universe() {
        return backingEdge.universe();
    }

    @Override
    public Node from() {
        Node fromNode = backingEdge.from();
        if (fromNode instanceof UniverseNode un) {
            return new ShadowUniverseNode(transactionContext, un);
        }
        return fromNode;
    }

    @Override
    public Node to() {
        Node toNode = backingEdge.to();
        if (toNode instanceof UniverseNode un) {
            return new ShadowUniverseNode(transactionContext, un);
        }
        return toNode;
    }

    @Override
    public TagSet tags() {
        return new ShadowTagSet(transactionContext, backingEdge);
    }

    @Override
    public AttributeMap attributes() {
        return new ShadowAttributeMap(transactionContext, backingEdge);
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
        if (obj instanceof ShadowUniverseEdge shadow) {
            return this.backingEdge.equals(shadow.backingEdge);
        }
        return this.backingEdge.equals(obj);
    }

    @Override
    public String toString() {
        return "ShadowUniverseEdge [from=" + from() + ", to=" + to() + ", attributes=" + this.attributes().toString() + ", tags=" + this.tags().toString() + "]";
    }
}
