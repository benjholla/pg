package dev.chpg.pg.exporter;

import java.util.Map;

/**
 * undocumented.
 */
public interface ExportNode {
    /**
     * undocumented.
     */
    int id();
    /**
     * undocumented.
     */
    Iterable<String> tags();
    /**
     * undocumented.
     */
    Map<String, ExportAttributeValue> attributes();
}
