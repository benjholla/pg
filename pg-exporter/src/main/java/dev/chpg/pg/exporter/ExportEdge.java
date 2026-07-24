package dev.chpg.pg.exporter;

import java.util.Map;

/**
 * An abstraction of a graph edge designed specifically for exportation.
 * <p>
 * <b>What it represents:</b> A directed connection between two nodes (identified by primitive IDs) that may carry tags and attributes.
 * <p>
 * <b>Why it exists:</b> To provide a normalized interface that the `pg-exporter` module can consume without depending on concrete graph implementations.
 * <p>
 * <b>When to use it:</b> Implement this interface on your internal edge types if you need to pass them to {@link DgbExporter}.
 * <p>
 * <b>Important invariants:</b> The {@code sourceId} and {@code targetId} must correspond to valid {@link ExportNode} IDs within the same {@link ExportGraph}.
 */
public interface ExportEdge {
    /**
     * Returns the globally unique primitive ID of this edge.
     *
     * @return the edge ID
     */
    int id();

    /**
     * Returns the primitive ID of the source (from) node.
     *
     * @return the source node ID
     */
    int sourceId();

    /**
     * Returns the primitive ID of the target (to) node.
     *
     * @return the target node ID
     */
    int targetId();

    /**
     * Returns an iterable of tags attached to this edge.
     *
     * @return the iterable of tags, or null if none
     */
    Iterable<String> tags();

    /**
     * Returns a map of typed attributes attached to this edge.
     *
     * @return the attributes map, or null if none
     */
    Map<String, ExportAttributeValue> attributes();
}
