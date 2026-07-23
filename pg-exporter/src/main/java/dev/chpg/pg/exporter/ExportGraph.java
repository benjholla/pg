package dev.chpg.pg.exporter;

/**
 * undocumented.
 */
public interface ExportGraph {
    /**
     * undocumented.
     */
    Iterable<? extends ExportNode> nodes();
    /**
     * undocumented.
     */
    Iterable<? extends ExportEdge> edges();
}
