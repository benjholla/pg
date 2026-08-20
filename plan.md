1. Add new invariant tests:
- `DifferenceNodeInvariantTest.java`: validating `Graph.difference(Node)` where it should remove the Node and any incident edges.
- `DifferenceEdgeInvariantTest.java`: validating `Graph.difference(Edge)` where it should subtract the Edge AND its terminal nodes (and cascade to any incident edges).
- `DifferenceEdgesEdgeInvariantTest.java`: validating `Graph.differenceEdges(Edge)` where it should only remove the Edge, keeping its terminal nodes.
- `UnionNodeInvariantTest.java`: validating `Graph.union(Node)` where it should add the Node to the graph without changing edges.
- `InduceEdgeInvariantTest.java`: validating `Graph.induce(Edge)` where it should only induce the edge if BOTH terminal nodes are present in the graph.
- `NodeEdgeAlgebraEdgeCaseTest.java`: general edge cases.

2. Run Pre-Commit check

3. Execute verification
- `./gradlew test` to make sure all tests pass.

4. Submit the work.
