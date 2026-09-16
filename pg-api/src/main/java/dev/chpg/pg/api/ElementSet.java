package dev.chpg.pg.api;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

/**
 * A generalized interface for specialized sets of {@link GraphElement}s.
 * <p>
 * <b>What it represents:</b> A distinct collection of graph elements (e.g., nodes or edges), supporting basic set-theoretic operations and core identity exposure.
 * <p>
 * <b>Why it exists:</b> To unify common behaviors between specialized sets like {@link NodeSet} and {@link EdgeSet}, enabling generalized pipelines for shared graph capabilities without duplicating API surfaces.
 * <p>
 * <b>When to use it:</b> Primarily used as an architectural base interface. Use the specialized subtypes {@link NodeSet} or {@link EdgeSet} in concrete code.
 * <p>
 * <b>Important invariants:</b> The operations defined here may return lazy or materialized representations based on the implementation. Methods covariantly returning the type {@code T} rely on subtype overrides to narrow the return type.
 */
public interface ElementSet<T extends GraphElement> extends Set<T> {

    /**
     * Converts this element set into an immutable snapshot.
     *
     * @return an immutable representation of this set
     */
    ElementSet<T> toImmutable();

    /**
     * Returns any single element from this set, if it is not empty.
     *
     * @return an Optional containing an element, or empty if the set is empty
     */
    Optional<T> one();

    /**
     * Filters this set to include only elements with the specified attribute key.
     *
     * @param attribute the attribute key to check for
     * @return a deferred set containing matching elements
     */
    ElementSet<T> withAttribute(String attribute);

    /**
     * Filters this set to include only elements with the specified attribute key matching any of the given values.
     *
     * @param attribute the attribute key to check for
     * @param values    the allowed attribute values
     * @return a deferred set containing matching elements
     */
    ElementSet<T> withAttribute(String attribute, AttributeValue... values);

    /**
     * Filters this set to include only elements possessing at least one of the specified tags.
     *
     * @param tags the tags to check for
     * @return a deferred set containing matching elements
     */
    ElementSet<T> withAnyTag(String... tags);

    /**
     * Filters this set to include only elements possessing all of the specified tags.
     *
     * @param tags the tags to check for
     * @return a deferred set containing matching elements
     */
    ElementSet<T> withAllTags(String... tags);

    /**
     * Forces eager evaluation of the deferred pipeline, materializing
     * the final IDs into a high-performance array in memory.
     * Note: This incurs an allocation and iteration cost.
     *
     * @return a materialized, immutable ElementSet
     */
    ElementSet<T> materialize();

    /**
     * Returns a new immutable snapshot containing elements present in both this set and the specified collection.
     *
     * @param other the collection to perform the set operation with
     * @return the intersected set
     */
    ElementSet<T> intersect(Collection<? extends T> other);

    /**
     * Returns a new immutable snapshot containing elements from this set, excluding those in the specified collection.
     *
     * @param other the collection to perform the set operation with
     * @return the differenced set
     */
    ElementSet<T> difference(Collection<? extends T> other);

    /**
     * Returns a new immutable snapshot containing all elements from this set and the specified collection.
     *
     * @param other the collection to perform the set operation with
     * @return the unioned set
     */
    ElementSet<T> union(Collection<? extends T> other);

    /**
     * Returns true if this set is already backed by a flat, allocated
     * memory structure. Returns false if this set requires computation
     * (lazy evaluation) during iteration.
     *
     * @return true if materialized, false otherwise
     */
    boolean isMaterialized();

    /**
     * Returns true if the size of the set can be determined in O(1) time
     * without iterating or evaluating the elements.
     *
     * @return true if the size is known in O(1) time, false otherwise
     */
    default boolean isSizeKnown() {
        return true;
    }

    /**
     * Returns a standard set of the primitive integer IDs of the elements in this set.
     *
     * @return a set of primitive integer IDs
     */
    Set<Integer> ids();

    /**
     * Returns an array of the primitive integer IDs of the elements in this set.
     *
     * @return an array of primitive integer IDs
     */
    int[] toIdArray();
}
