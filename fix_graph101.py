with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/EphemeralGraph.java', 'r') as f:
    content = f.read()

import re

removeNodeOld = """    @Override
    public boolean removeNode(Node node) {
        int targetId = node.id();

        boolean existedLocal = nodes.remove(targetId) != null;

        if (targetId >= 0) {
             if (tombstonedNodeIds.get(targetId)) return false;
             tombstonedNodeIds.set(targetId);
        } else if (!existedLocal) {
             return false;
        }

        EphemeralEdgeSet outSet = outEdges.remove(targetId);
        EphemeralEdgeSet inSet = inEdges.remove(targetId);"""

removeNodeNew = """    @Override
    public boolean removeNode(Node node) {
        int targetId = node.id();

        if (targetId >= 0) {
             if (node instanceof UniverseView view) {
                 if (view.universe() != this.universe) {
                     throw new IllegalArgumentException("Cross-universe contamination: Cannot remove a node belonging to a foreign Universe.");
                 }
             } else {
                 if (node instanceof dev.chpg.pg.global.GlobalNode) {
                     return false; // Silently ignore global node
                 }
                 throw new IllegalArgumentException("Cross-universe contamination: Cannot remove a node belonging to a foreign Universe.");
             }
        }

        boolean existedLocal = nodes.remove(targetId) != null;

        if (targetId >= 0) {
             if (tombstonedNodeIds.get(targetId)) return false;
             tombstonedNodeIds.set(targetId);
        } else if (!existedLocal) {
             return false;
        }

        EphemeralEdgeSet outSet = outEdges.remove(targetId);
        EphemeralEdgeSet inSet = inEdges.remove(targetId);"""

content = content.replace(removeNodeOld, removeNodeNew)

removeEdgeOld = """    @Override
    public boolean removeEdge(Edge edge) {
        int edgeId = edge.id();

        boolean existedLocal = edges.remove(edgeId) != null;
        if (edgeId >= 0) {
            if (tombstonedEdgeIds.get(edgeId)) return false;
            tombstonedEdgeIds.set(edgeId);
        } else if (!existedLocal) {
            return false;
        }

        int fromId = edge.from().id();
        int toId = edge.to().id();"""

removeEdgeNew = """    @Override
    public boolean removeEdge(Edge edge) {
        int edgeId = edge.id();

        if (edgeId >= 0) {
             if (edge instanceof UniverseView view) {
                 if (view.universe() != this.universe) {
                     throw new IllegalArgumentException("Cross-universe contamination: Cannot remove an edge belonging to a foreign Universe.");
                 }
             } else {
                 if (edge instanceof dev.chpg.pg.global.GlobalEdge) {
                     return false; // Silently ignore global edge
                 }
                 throw new IllegalArgumentException("Cross-universe contamination: Cannot remove an edge belonging to a foreign Universe.");
             }
        }

        boolean existedLocal = edges.remove(edgeId) != null;
        if (edgeId >= 0) {
            if (tombstonedEdgeIds.get(edgeId)) return false;
            tombstonedEdgeIds.set(edgeId);
        } else if (!existedLocal) {
            return false;
        }

        int fromId = edge.from().id();
        int toId = edge.to().id();"""

content = content.replace(removeEdgeOld, removeEdgeNew)

with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/EphemeralGraph.java', 'w') as f:
    f.write(content)
