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

/**
 * An immutable, highly optimized set containing exactly one {@link Node} (EphemeralNode or ShadowNode).
 * <p>
 * <b>What it represents:</b> A single-element, read-only collection in the ephemeral sandbox architecture.
 * <p>
 * <b>Why it exists:</b> It avoids the memory overhead and hashing cost of standard {@code HashSet} collections for singleton queries.
 * <p>
 * <b>When to use it:</b> It is used internally as the return type for operations that definitively isolate a single node (e.g., resolving a specific ID).
 * <p>
 * <b>Common usage patterns:</b> Generated dynamically when operations result in a size of 1.
 * <p>
 * <b>Important invariants:</b> The set size is strictly fixed at 1. Mutative operations throw an {@code UnsupportedOperationException}.
 * <p>
 * <b>Thread safety:</b> Completely thread-safe and stateless beyond the singular frozen element reference.
 * <p>
 * <b>Performance characteristics:</b> Operations like {@code contains()}, {@code size()}, and {@code toArray()} execute in perfect O(1) time with zero allocation overhead.
 */
public final class EphemeralImmutableSingletonNodeSet extends AbstractSet<Node> implements NodeSet {

    private final Node element;

    /**
     * Constructs a new immutable singleton set containing the specified element.
     *
     * @param element the single node to freeze in this set
     */
    public EphemeralImmutableSingletonNodeSet(Node element) {
        this.element = Objects.requireNonNull(element, "element cannot be null");
    }

    // --- The Firewall ---
    private Node validate(Node node) {
        Objects.requireNonNull(node, "Node cannot be null");
        if (!(node instanceof EphemeralNode) && !(node.getClass().getSimpleName().contains("Shadow"))) {
            throw new IllegalArgumentException(
                "Cross-graph contamination: Expected EphemeralNode or ShadowNode, got " + node.getClass().getSimpleName()
            );
        }
        if (node.id() >= 0) {
            throw new IllegalArgumentException(
                "Topological violation: Local adjacency sets can only store brand-new transaction elements (negative IDs)."
            );
        }
        return node;
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
        Objects.requireNonNull(other, "other cannot be null");

        // Pre-flight Fail-Fast Validation
        for (Node n : other) {
            this.validate(n);
        }

        if (other.contains(element)) {
            return this;
        }
        return NodeSet.empty();
    }

    @Override
    public NodeSet difference(Collection<? extends Node> other) {
        Objects.requireNonNull(other, "other cannot be null");

        // Pre-flight Fail-Fast Validation
        for (Node n : other) {
            this.validate(n);
        }

        if (other.contains(element)) {
            return NodeSet.empty();
        }
        return this;
    }

    @Override
    public NodeSet union(Collection<? extends Node> other) {
        Objects.requireNonNull(other, "other cannot be null");
        EphemeralNodeSet result = new EphemeralNodeSet();

        result.add(element);
        for (Node n : other) {
            // EphemeralNodeSet.add() naturally applies the firewall here
            result.add(n);
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
