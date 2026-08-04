package dev.chpg.pg.multiverse.ephemeral;

import dev.chpg.pg.api.AttributeMap;
import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.TagSet;
import dev.chpg.pg.multiverse.universe.Universe;
import dev.chpg.pg.multiverse.universe.UniverseEdge;
import dev.chpg.pg.multiverse.universe.UniverseView;

public class ShadowEdge implements Edge, UniverseView {

    private final EphemeralGraph transaction;
    private final UniverseEdge backingEdge; // Strictly UniverseEdge now

    public ShadowEdge(EphemeralGraph transaction, UniverseEdge backingEdge) {
        this.transaction = transaction;
        this.backingEdge = backingEdge;
    }

    public UniverseEdge backingEdge() { return backingEdge; }

    public EphemeralGraph transaction() {
        return transaction;
    }

    @Override
    public int id() {
        return backingEdge.id();
    }

    @Override
    public Universe universe() {
        // No fallback needed, a UniverseEdge always belongs to a Universe
        return backingEdge.universe();
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
        // Unconditionally route to the delta buffer
        return new ShadowTagSet(transaction, backingEdge);
    }

    @Override
    public AttributeMap attributes() {
        // Unconditionally route to the delta buffer
        return new ShadowAttributeMap(transaction, backingEdge);
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
