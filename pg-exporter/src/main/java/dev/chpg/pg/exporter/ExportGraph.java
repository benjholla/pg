package dev.chpg.pg.exporter;

public interface ExportGraph {
    Iterable<? extends ExportNode> nodes();
    Iterable<? extends ExportEdge> edges();
}
