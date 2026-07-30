with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/ShadowEdgeSet.java', 'r') as f:
    content = f.read()

import re

# Fix intersect in ShadowEdgeSet
intersect_edge = """    @Override
    public EdgeSet intersect(Collection<? extends Edge> other) {
        EdgeSet unwrappedOther = unwrapForAlgebra(other);

        java.util.List<Edge> universeOnly = new java.util.ArrayList<>();
        for (Edge e : unwrappedOther) {
            if (e instanceof dev.chpg.pg.multiverse.universe.UniverseEdge) {
                 universeOnly.add(e);
            }
        }
        EdgeSet rawIntersect = this.backingSet.intersect(new dev.chpg.pg.api.GenericImmutableEdgeSet(new java.util.HashSet<>(universeOnly)));

        EdgeSet filteredIntersect = rawIntersect;
        if (filteredIntersect instanceof UniverseEdgeSet) {
             UniverseEdgeSet filteredUniverseIntersect = (UniverseEdgeSet) filteredIntersect;
             filteredIntersect = filteredUniverseIntersect.difference(
                 new UniverseEdgeSet(
                     this.transactionContext.universe(),
                     this.transactionContext.getTombstonedEdgeIds()
                 )
             );
        } else {
             Set<Integer> tombstoned = new HashSet<>();
             BitSet tombstones = this.transactionContext.getTombstonedEdgeIds();
             for (int i = tombstones.nextSetBit(0); i >= 0; i = tombstones.nextSetBit(i+1)) {
                 tombstoned.add(i);
             }
             EdgeSet toRemove = new EphemeralEdgeSet(tombstoned.stream().map(id -> new UniverseEdge(this.transactionContext.universe(), id)).collect(Collectors.toList()));
             filteredIntersect = filteredIntersect.difference(toRemove);
        }

        Set<Edge> combinedLocalAdds = new HashSet<>();
        for (Edge local : this.localAdds) {
            boolean found = false;
            if (other instanceof ShadowEdgeSet) {
                ShadowEdgeSet shadowOther = (ShadowEdgeSet) other;
                for (Edge o : shadowOther.localAdds) {
                    if (local.equals(o) || o.equals(local) ||
                        (local instanceof ShadowEdge && ((ShadowEdge) local).backingEdge().equals(o)) ||
                        (o instanceof ShadowEdge && ((ShadowEdge) o).backingEdge().equals(local))) {
                        found = true;
                        break;
                    }
                }
            } else if (other != null) {
                for (Edge o : other) {
                    if (local.equals(o) || o.equals(local) ||
                        (local instanceof ShadowEdge && ((ShadowEdge) local).backingEdge().equals(o)) ||
                        (o instanceof ShadowEdge && ((ShadowEdge) o).backingEdge().equals(local))) {
                        found = true;
                        break;
                    }
                }
            }
            if (found) {
                combinedLocalAdds.add(local);
            }
        }

        return new ShadowEdgeSet(this.transactionContext, filteredIntersect, combinedLocalAdds);
    }"""
content = re.sub(r'    @Override\n    public EdgeSet intersect\(Collection<\? extends Edge> other\) \{.*?return new ShadowEdgeSet\(this\.transactionContext, filteredIntersect, combinedLocalAdds\);\n    \}', intersect_edge, content, flags=re.DOTALL)
with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/ShadowEdgeSet.java', 'w') as f:
    f.write(content)

with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/ShadowNodeSet.java', 'r') as f:
    content = f.read()

intersect_node = """    @Override
    public NodeSet intersect(Collection<? extends Node> other) {
        NodeSet unwrappedOther = unwrapForAlgebra(other);

        java.util.List<Node> universeOnly = new java.util.ArrayList<>();
        for (Node n : unwrappedOther) {
            if (n instanceof dev.chpg.pg.multiverse.universe.UniverseNode) {
                 universeOnly.add(n);
            }
        }
        NodeSet rawIntersect = this.backingSet.intersect(new dev.chpg.pg.api.GenericImmutableNodeSet(new java.util.HashSet<>(universeOnly)));

        NodeSet filteredIntersect = rawIntersect;
        if (filteredIntersect instanceof dev.chpg.pg.multiverse.universe.UniverseNodeSet) {
             dev.chpg.pg.multiverse.universe.UniverseNodeSet filteredUniverseIntersect = (dev.chpg.pg.multiverse.universe.UniverseNodeSet) filteredIntersect;
             filteredIntersect = filteredUniverseIntersect.difference(
                 new dev.chpg.pg.multiverse.universe.UniverseNodeSet(
                     this.transactionContext.universe(),
                     this.transactionContext.getTombstonedNodeIds()
                 )
             );
        } else {
             Set<Integer> tombstoned = new HashSet<>();
             BitSet tombstones = this.transactionContext.getTombstonedNodeIds();
             for (int i = tombstones.nextSetBit(0); i >= 0; i = tombstones.nextSetBit(i+1)) {
                 tombstoned.add(i);
             }
             NodeSet toRemove = new EphemeralNodeSet(tombstoned.stream().map(id -> new dev.chpg.pg.multiverse.universe.UniverseNode(this.transactionContext.universe(), id)).collect(Collectors.toList()));
             filteredIntersect = filteredIntersect.difference(toRemove);
        }

        Set<Node> combinedLocalAdds = new HashSet<>();
        for (Node local : this.localAdds) {
            boolean found = false;
            if (other instanceof ShadowNodeSet) {
                ShadowNodeSet shadowOther = (ShadowNodeSet) other;
                for (Node o : shadowOther.localAdds) {
                    if (local.equals(o) || o.equals(local) ||
                        (local instanceof ShadowUniverseNode && ((ShadowUniverseNode) local).id() == o.id()) ||
                        (o instanceof ShadowUniverseNode && ((ShadowUniverseNode) o).id() == local.id())) {
                        found = true;
                        break;
                    }
                }
            } else if (other != null) {
                for (Node o : other) {
                    if (local.equals(o) || o.equals(local) ||
                        (local instanceof ShadowUniverseNode && ((ShadowUniverseNode) local).id() == o.id()) ||
                        (o instanceof ShadowUniverseNode && ((ShadowUniverseNode) o).id() == local.id())) {
                        found = true;
                        break;
                    }
                }
            }
            if (found) {
                combinedLocalAdds.add(local);
            }
        }

        return new ShadowNodeSet(this.transactionContext, filteredIntersect, combinedLocalAdds);
    }"""
content = re.sub(r'    @Override\n    public NodeSet intersect\(Collection<\? extends Node> other\) \{.*?return new ShadowNodeSet\(this\.transactionContext, filteredIntersect, combinedLocalAdds\);\n    \}', intersect_node, content, flags=re.DOTALL)
with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/ShadowNodeSet.java', 'w') as f:
    f.write(content)
