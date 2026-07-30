with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/EphemeralGraph.java', 'r') as f:
    content = f.read()

import re

edges_new = """    @Override
    public EdgeSet edges(Node source, Node target) {
        if (!nodes.containsKey(source.id())) { return EMPTY_EDGES; }
        if (!nodes.containsKey(target.id())) { return EMPTY_EDGES; }

        Optional<EdgeSet> out = getOutEdgesFromNode(source);
        if (out.isEmpty()) { return EMPTY_EDGES; }

        EdgeSet result = new EphemeralEdgeSet();
        for (Edge e : out.get()) {
            if (e.to().equals(target) || e.to().id() == target.id()) {
                if (e instanceof ShadowEdge && ((ShadowEdge) e).backingEdge() instanceof EphemeralEdge) {
                    result.add(((ShadowEdge) e).backingEdge());
                } else {
                    result.add(e);
                }
            }
        }
        return result.isEmpty() ? EMPTY_EDGES : (result.size() == 1 ? new dev.chpg.pg.api.GenericImmutableEdgeSet(java.util.Collections.singleton(result.iterator().next())) : new dev.chpg.pg.api.GenericImmutableEdgeSet(result));
    }"""

content = re.sub(r'    @Override\n    public EdgeSet edges\(Node source, Node target\) \{.*?return result\.isEmpty\(\) \? EMPTY_EDGES.*?;\n    \}', edges_new, content, flags=re.DOTALL)

with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/EphemeralGraph.java', 'w') as f:
    f.write(content)
