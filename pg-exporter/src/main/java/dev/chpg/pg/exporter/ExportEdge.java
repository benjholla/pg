package dev.chpg.pg.exporter;

import java.util.Map;

public interface ExportEdge {
    int id();
    int sourceId();
    int targetId();
    Iterable<String> tags();
    Map<String, ExportAttributeValue> attributes();
}
