with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/EphemeralGraph.java', 'r') as f:
    content = f.read()

# Let's fix `removeEdge(Edge edge)` and `removeNode(Node node)` to ALWAYS cascade disconnects correctly!
import re

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
        int toId = edge.to().id();

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
content = re.sub(r'    @Override\n    public boolean removeEdge\(Edge edge\) \{.*?return true;\n    \}', removeEdgeNew, content, flags=re.DOTALL)

with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/EphemeralGraph.java', 'w') as f:
    f.write(content)
