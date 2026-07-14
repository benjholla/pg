package dev.chpg.pg.exporter;

import java.util.Map;

public interface ExportNode {
    int id();
    Iterable<String> tags();
    Map<String, ExportAttributeValue> attributes();
}
