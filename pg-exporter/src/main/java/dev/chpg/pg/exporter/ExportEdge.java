package dev.chpg.pg.exporter;

import java.util.Map;

/**
 * undocumented.
 */
public interface ExportEdge {
    /**
     * undocumented.
     */
    int id();
    /**
     * undocumented.
     */
    int sourceId();
    /**
     * undocumented.
     */
    int targetId();
    /**
     * undocumented.
     */
    Iterable<String> tags();
    /**
     * undocumented.
     */
    Map<String, ExportAttributeValue> attributes();
}
