package dev.chpg.pg.exporter;

/**
 * An abstraction of a graph designed specifically for exportation.
 * <p>
 * <b>What it represents:</b> A collection of {@link ExportNode}s and {@link ExportEdge}s that can be serialized.
 * <p>
 * <b>Why it exists:</b> To provide a normalized interface that the `pg-exporter` module can consume without depending on concrete graph implementations.
 * <p>
 * <b>When to use it:</b> Implement this interface on your internal graph types if you need to pass them to {@link DgbExporter}.
 */
public interface ExportGraph {
    /**
     * Returns an iterable of all nodes in this graph.
     *
     * @return the nodes iterable
     */
    Iterable<? extends ExportNode> nodes();

    /**
     * Returns an iterable of all edges in this graph.
     *
     * @return the edges iterable
     */
    Iterable<? extends ExportEdge> edges();
}
