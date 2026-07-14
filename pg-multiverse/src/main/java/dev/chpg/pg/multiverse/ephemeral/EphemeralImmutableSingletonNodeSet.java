package dev.chpg.pg.multiverse.ephemeral;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.NodeSet;

public final class EphemeralImmutableSingletonNodeSet extends AbstractSet<Node> implements NodeSet {

    private final EphemeralNode element;

    public EphemeralImmutableSingletonNodeSet(EphemeralNode element) {
        this.element = Objects.requireNonNull(element, "element cannot be null");
    }

    @Override
    public NodeSet toImmutable() {
        return this;
    }
    @Override
public NodeSet materialize() {
        return this;
    }

    @Override
    public boolean isMaterialized() {
        return true;
    }

    public int size() {
        return 1;
    }

    @Override
    public boolean contains(Object o) {
        return element.equals(o);
    }

    @Override
    public Iterator<Node> iterator() {
        return Collections.<Node>singleton(element).iterator();
    }

    @Override
    public Optional<Node> one() {
        return Optional.of(element);
    }



    @Override
    public NodeSet intersect(Collection<? extends Node> other) {
        java.util.Objects.requireNonNull(other, "other cannot be null");
        if (other.contains(element)) {
            return this;
        }
        return NodeSet.empty();
    }

    @Override
    public NodeSet difference(Collection<? extends Node> other) {
        java.util.Objects.requireNonNull(other, "other cannot be null");
        if (other.contains(element)) {
            return NodeSet.empty();
        }
        return this;
    }

    @Override
    public NodeSet union(Collection<? extends Node> other) {
        java.util.Objects.requireNonNull(other, "other cannot be null");
        EphemeralNodeSet result = new EphemeralNodeSet();
        result.add(element);
        for (Node n : other) {
            result.add((EphemeralNode) n);
        }
        if (result.size() == 1) {
            return this;
        }
        return new EphemeralImmutableNodeSet(result);
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
