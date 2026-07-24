package dev.chpg.pg.exporter;

import java.util.Map;

/**
 * An abstraction of a graph node designed specifically for exportation.
 * <p>
 * <b>What it represents:</b> A vertex in the graph (identified by a primitive ID) that may carry tags and attributes.
 * <p>
 * <b>Why it exists:</b> To provide a normalized interface that the `pg-exporter` module can consume without depending on concrete graph implementations.
 * <p>
 * <b>When to use it:</b> Implement this interface on your internal node types if you need to pass them to {@link DgbExporter}.
 */
public interface ExportNode {
    /**
     * Returns the globally unique primitive ID of this node.
     *
     * @return the node ID
     */
    int id();

    /**
     * Returns an iterable of tags attached to this node.
     *
     * @return the iterable of tags, or null if none
     */
    Iterable<String> tags();

    /**
     * Returns a map of typed attributes attached to this node.
     *
     * @return the attributes map, or null if none
     */
    Map<String, ExportAttributeValue> attributes();
}
