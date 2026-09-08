package dev.chpg.pg.api;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

/**
 * A specialized base set for managing collections of {@link GraphElement}s.
 * <p>
 * <b>What it represents:</b> A distinct collection of graph elements, supporting set-theoretic math and fluent filtering.
 * <p>
 * <b>Why it exists:</b> To provide a zero-allocation, functional query pipeline for graph analysis, enabling operations like filtering without intermediate collection allocation.
 * <p>
 * <b>When to use it:</b> Use concrete sub-interfaces like {@code NodeSet} or {@code EdgeSet} whenever dealing with aggregate collections of elements, especially when extracting properties or executing declarative filters.
 * <p>
 * <b>Common usage patterns:</b>
 * <ul>
 * <li>Filtering elements via fluent predicates (e.g., {@code elements.withAnyTag("Person")}).</li>
 * <li>Extracting primitive ID arrays for bulk operations (e.g., {@code elements.toIdArray()}).</li>
 * <li>Forcing eager materialization of deferred pipelines via {@code materialize()}.</li>
 * </ul>
 * <p>
 * <b>Important invariants:</b> {@code ElementSet} implementations may be "live" (dynamically reflecting changes in the underlying graph) or "snapshot" based. Many filtering methods return a deferred evaluation pipeline that only executes when terminal operations (like {@code size()} or {@code toIdArray()}) are invoked.
 * <p>
 * <b>Thread safety:</b> Thread safety guarantees depend heavily on the concrete implementation. Modifying the underlying graph while iterating a live ElementSet will likely produce a {@code ConcurrentModificationException}.
 * <p>
 * <b>Performance characteristics:</b> The fluent filtering API (e.g., {@code withAttribute}) returns a deferred, zero-allocation wrapper. Terminal operations on deferred sets evaluate the pipeline and take O(N) time.
 *
 * @param <T> the specific element type
 */
public interface ElementSet<T extends GraphElement> extends Set<T> {

    /**
     * Converts this element set into an immutable snapshot.
     *
     * @return an immutable ElementSet
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
     * @return a deferred ElementSet containing matching elements
     */
    ElementSet<T> withAttribute(String attribute);

    /**
     * Filters this set to include only elements with the specified attribute key matching any of the given values.
     *
     * @param attribute the attribute key to check for
     * @param values    the allowed attribute values
     * @return a deferred ElementSet containing matching elements
     */
    ElementSet<T> withAttribute(String attribute, AttributeValue... values);

    /**
     * Filters this set to include only elements possessing at least one of the specified tags.
     *
     * @param tags the tags to check for
     * @return a deferred ElementSet containing matching elements
     */
    ElementSet<T> withAnyTag(String... tags);

    /**
     * Filters this set to include only elements possessing all of the specified tags.
     *
     * @param tags the tags to check for
     * @return a deferred ElementSet containing matching elements
     */
    ElementSet<T> withAllTags(String... tags);

    /**
     * Forces eager evaluation of the deferred pipeline.
     *
     * @return a materialized ElementSet
     */
    ElementSet<T> materialize();

    /**
     * Returns a new immutable ElementSet snapshot containing elements present in both this set and the specified collection.
     * @param other the collection to perform the set operation with
     * @return the intersected ElementSet
     */
    ElementSet<T> intersect(Collection<? extends T> other);

    /**
     * Returns a new immutable ElementSet snapshot containing elements from this set, excluding those in the specified collection.
     * @param other the collection to perform the set operation with
     * @return the differenced ElementSet
     */
    ElementSet<T> difference(Collection<? extends T> other);

    /**
     * Returns a new immutable ElementSet snapshot containing all elements from this set and the specified collection.
     * @param other the collection to perform the set operation with
     * @return the unioned ElementSet
     */
    ElementSet<T> union(Collection<? extends T> other);

    /**
     * Returns true if this set is already backed by a flat, allocated memory structure.
     *
     * @return true if materialized, false otherwise
     */
    boolean isMaterialized();

    /**
     * Returns true if the size of the set can be determined in O(1) time.
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
