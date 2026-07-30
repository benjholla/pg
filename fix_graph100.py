with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/EphemeralGraph.java', 'r') as f:
    content = f.read()

import re

removeNodeOld = """    @Override
    public boolean removeNode(Node node) {
        int targetId = node.id();

        if (node instanceof ShadowUniverseNode || node instanceof dev.chpg.pg.multiverse.universe.UniverseNode) {
             tombstonedNodeIds.set(targetId);
        }

        // 2. Collapse the pillars for this ID
        if (nodes.remove(targetId) == null) {
            return false;
        }

        EphemeralEdgeSet outSet = outEdges.remove(targetId);
        EphemeralEdgeSet inSet = inEdges.remove(targetId);

        // 3. Cascading Teardown: Scrub Outbound Edges
        if (outSet != null) {
            for (Edge out : outSet) {
                removeEdge(out);
            }
        }

        // 4. Cascading Teardown: Scrub Inbound Edges
        if (inSet != null) {
            for (Edge in : inSet) {
                removeEdge(in);
            }
        }

        return true;
    }"""

removeNodeNew = """    @Override
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
        EphemeralEdgeSet inSet = inEdges.remove(targetId);

        // 3. Cascading Teardown: Scrub Outbound Edges
        if (outSet != null) {
            for (Edge out : outSet) {
                removeEdge(out);
            }
        }

        // 4. Cascading Teardown: Scrub Inbound Edges
        if (inSet != null) {
            for (Edge in : inSet) {
                removeEdge(in);
            }
        }

        return true;
    }"""

content = content.replace(removeNodeOld, removeNodeNew)

removeEdgeOld = """    @Override
    public boolean removeEdge(Edge edge) {
        int edgeId = edge.id();

        if (edge instanceof ShadowEdge || edge instanceof dev.chpg.pg.multiverse.universe.UniverseEdge) {
            tombstonedEdgeIds.set(edgeId);
        }

        if (edges.remove(edgeId) == null && !tombstonedEdgeIds.get(edgeId)) {
            return false;
        }

        int fromId = edge.from().id();
        int toId = edge.to().id();

        // 2. Disconnect from pillars
        EphemeralEdgeSet fromOut = outEdges.get(fromId);
        if (fromOut != null) {
            fromOut.remove(edge);
        }

        EphemeralEdgeSet toIn = inEdges.get(toId);
        if (toIn != null) {
            toIn.remove(edge);
        }

        return true;
    }"""

removeEdgeNew = """    @Override
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
        int toId = edge.to().id();

        // 2. Disconnect from pillars
        EphemeralEdgeSet fromOut = outEdges.get(fromId);
        if (fromOut != null) {
            fromOut.remove(edge);
        }

        EphemeralEdgeSet toIn = inEdges.get(toId);
        if (toIn != null) {
            toIn.remove(edge);
        }

        return true;
    }"""

content = content.replace(removeEdgeOld, removeEdgeNew)

with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/EphemeralGraph.java', 'w') as f:
    f.write(content)
