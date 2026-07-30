import re

with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/EphemeralGraph.java', 'r') as f:
    content = f.read()

removeNodeOld = """        // 3. Cascading Teardown: Scrub Outbound Edges
        if (outSet != null) {
            for (Edge out : outSet) {
                edges.remove(out.id()); // Remove from global registry
                int toId = out.to().id();
                if (toId != targetId) { // Skip self-loops, pillar already collapsed
                    inEdges.get(toId).remove(out); // Disconnect from neighbor
                }
            }
        }

        // 4. Cascading Teardown: Scrub Inbound Edges
        if (inSet != null) {
            for (Edge in : inSet) {
                edges.remove(in.id()); // Remove from global registry
                int fromId = in.from().id();
                if (fromId != targetId) { // Skip self-loops
                    outEdges.get(fromId).remove(in); // Disconnect from neighbor
                }
            }
        }"""

removeNodeNew = """        // 3. Cascading Teardown: Scrub Outbound Edges
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
        }"""

content = content.replace(removeNodeOld, removeNodeNew)

with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/EphemeralGraph.java', 'w') as f:
    f.write(content)
