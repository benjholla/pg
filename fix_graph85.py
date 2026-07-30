# EphemeralGraph creates new EphemeralGraph out of operations like union.
# When creating `new EphemeralGraph(..., nodes, edges)` it calls `addAllEdges(edges)` which calls `addEdge(edge)`.
# Since `edges` contains `ShadowEdge` objects from the other graph, they have `transactionContext != this`, which throws "Shadow edge belongs to a foreign transaction."
# But wait, set algebra operations across the SAME universe are valid!
# If two `EphemeralGraph`s share the same `Universe`, can their elements be unioned?
# The `ShadowEdge` and `ShadowNode` are bound to a specific `EphemeralGraph`.
# `ShadowNode.transactionContext()` returns the context.
# We shouldn't store `ShadowEdge` bound to another transaction. We should wrap it in our OWN `ShadowEdge`.

with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/EphemeralGraph.java', 'r') as f:
    content = f.read()

import re

addNodeOld = """    @Override
    public boolean addNode(Node node) {
        Node safeNode = validateAndWrap(node);

        int id = safeNode.id();"""

addNodeNew = """    @Override
    public boolean addNode(Node node) {
        Node safeNode = node;
        if (node instanceof ShadowUniverseNode shadow && shadow.transaction() != this) {
            safeNode = validateAndWrap(new dev.chpg.pg.multiverse.universe.UniverseNode(shadow.universe(), shadow.id()));
        } else {
            safeNode = validateAndWrap(node);
        }

        int id = safeNode.id();"""

content = content.replace(addNodeOld, addNodeNew)

addEdgeOld = """    @Override
    public boolean addEdge(Edge edge) {
        Objects.requireNonNull(edge, "edge cannot be null");
        if (edge instanceof ShadowEdge) {
            ShadowEdge se = (ShadowEdge) edge;
            if (se.transaction() != this) {
                throw new IllegalArgumentException("Shadow edge belongs to a foreign transaction.");
            }
            if (!edges.containsKey(se.id())) {
                boolean result = false;
                result |= addNode(se.from());
                result |= addNode(se.to());
                result |= linkEdge(se);
                return result;
            }
            return false;
        }

        if (edge instanceof UniverseView view) {
            if (view.universe() != this.universe) {
                throw new IllegalArgumentException("Edge belongs to a foreign Universe.");
            }
        }
        if (edge instanceof dev.chpg.pg.multiverse.universe.UniverseEdge) {
            // Needs to be wrapped
        }

        ShadowEdge safeEdge = new ShadowEdge(this, edge);
        if (edges.containsKey(safeEdge.id())) {
            return false;
        }

        boolean result = false;
        result |= addNode(safeEdge.from());
        result |= addNode(safeEdge.to());
        result |= linkEdge(safeEdge);
        return result;
    }"""

addEdgeNew = """    @Override
    public boolean addEdge(Edge edge) {
        Objects.requireNonNull(edge, "edge cannot be null");

        Edge safeEdge = edge;
        if (edge instanceof ShadowEdge) {
            ShadowEdge se = (ShadowEdge) edge;
            if (se.transaction() != this) {
                if (se.universe() != this.universe) {
                    throw new IllegalArgumentException("Edge belongs to a foreign Universe.");
                }
                safeEdge = new ShadowEdge(this, se.backingEdge());
            }
        } else if (edge instanceof dev.chpg.pg.multiverse.universe.UniverseEdge || edge instanceof EphemeralEdge) {
            if (edge instanceof UniverseView view && view.universe() != this.universe) {
                throw new IllegalArgumentException("Edge belongs to a foreign Universe.");
            }
            safeEdge = new ShadowEdge(this, edge);
        } else {
            throw new IllegalArgumentException("Unsupported Edge implementation.");
        }

        if (edges.containsKey(safeEdge.id())) {
            return false;
        }

        boolean result = false;
        result |= addNode(safeEdge.from());
        result |= addNode(safeEdge.to());
        result |= linkEdge(safeEdge);
        return result;
    }"""

content = content.replace(addEdgeOld, addEdgeNew)

linkEdgeOld = """    @Override
    public boolean linkEdge(Edge edge) {
        Objects.requireNonNull(edge, "edge cannot be null");

        Edge safeEdge = edge;
        if (!(edge instanceof ShadowEdge)) {
            if (edge instanceof UniverseView view) {
                if (view.universe() != this.universe) {
                    throw new IllegalArgumentException("Edge belongs to a foreign Universe.");
                }
            }
            if (edge instanceof dev.chpg.pg.multiverse.universe.UniverseEdge) {
                safeEdge = new ShadowEdge(this, edge);
            } else if (edge instanceof EphemeralEdge) {
                safeEdge = edge;
            } else {
                throw new IllegalArgumentException("Unsupported Edge implementation.");
            }
        } else {
             if (((ShadowEdge) safeEdge).transaction() != this) {
                 throw new IllegalArgumentException("Shadow edge belongs to a foreign transaction.");
             }
        }

        int edgeId = safeEdge.id();"""

linkEdgeNew = """    @Override
    public boolean linkEdge(Edge edge) {
        Objects.requireNonNull(edge, "edge cannot be null");

        Edge safeEdge = edge;
        if (edge instanceof ShadowEdge) {
            ShadowEdge se = (ShadowEdge) edge;
            if (se.transaction() != this) {
                if (se.universe() != this.universe) {
                    throw new IllegalArgumentException("Edge belongs to a foreign Universe.");
                }
                safeEdge = new ShadowEdge(this, se.backingEdge());
            }
        } else if (edge instanceof dev.chpg.pg.multiverse.universe.UniverseEdge || edge instanceof EphemeralEdge) {
            if (edge instanceof UniverseView view && view.universe() != this.universe) {
                throw new IllegalArgumentException("Edge belongs to a foreign Universe.");
            }
            safeEdge = new ShadowEdge(this, edge);
        } else {
            throw new IllegalArgumentException("Unsupported Edge implementation.");
        }

        int edgeId = safeEdge.id();"""

content = content.replace(linkEdgeOld, linkEdgeNew)

with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/EphemeralGraph.java', 'w') as f:
    f.write(content)
