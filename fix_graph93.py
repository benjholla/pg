with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/EphemeralGraph.java', 'r') as f:
    content = f.read()

import re

# `EphemeralGraph.removeAllEdges` handles iteration over the same graph safely:
# for (Edge edge : new ArrayList<>(edges)) { result |= removeEdge(edge); }
# But in `ShadowEdgeSet.java`, we changed intersect/difference/union to drop `tombstonedEdgeIds`.
# AND we used `removeEdge(edge)` in `EphemeralGraph.java`.

# Wait! Did I change `validateAndWrap` to return `ShadowUniverseNode` AND keep it in `tombstonedNodeIds`?
# In `removeNode`:
# if (node instanceof ShadowUniverseNode || node instanceof dev.chpg.pg.multiverse.universe.UniverseNode) { tombstonedNodeIds.set(targetId); }
# AND cascades removeEdge.

# The test failures: `ClassCastException`!
# `java.lang.ClassCastException at GraphInduceInvariantTest.java:65`
# `java.lang.ClassCastException at GraphDegreeInvariantTest.java:43`
# `java.lang.ClassCastException at EphemeralGraphTest.java:355`
# `java.lang.ClassCastException at AssociativityLawsInvariantTest.java:78`
