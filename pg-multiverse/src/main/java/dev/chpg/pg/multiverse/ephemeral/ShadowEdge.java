package dev.chpg.pg.multiverse.ephemeral;

import dev.chpg.pg.api.AttributeMap;
import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.TagSet;
import dev.chpg.pg.multiverse.universe.Universe;
import dev.chpg.pg.multiverse.universe.UniverseEdge;
import dev.chpg.pg.multiverse.universe.UniverseView;

/**
 * A transactional view over a Universe element.
 */
public class ShadowEdge implements Edge, UniverseView {

    private final EphemeralGraph transaction;
    private final Edge backingEdge;

    /**
     * Constructs a new ShadowEdge.
     * @param transaction the ephemeral graph
     * @param backingEdge the backing universe edge
     */
    public ShadowEdge(EphemeralGraph transaction, Edge backingEdge) {
        this.transaction = transaction;
        this.backingEdge = backingEdge;
    }

    /**
     * Returns the backing edge.
     * @return the backing edge
     */
    public Edge backingEdge() { return backingEdge; }
    /**
     * Returns the transaction graph.
     * @return the transaction graph
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
