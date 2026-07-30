import re

with open('./pg-multiverse/src/test/java/dev/chpg/pg/multiverse/ephemeral/EphemeralUnmodifiableLiveNodeSetTest.java', 'r') as f:
    content = f.read()

content = re.sub(r'Map<Integer, dev.chpg.pg.multiverse.ephemeral.EphemeralEdge> edges = new HashMap<>\(\);', 'Map<Integer, Edge> edges = new HashMap<>();', content)

with open('./pg-multiverse/src/test/java/dev/chpg/pg/multiverse/ephemeral/EphemeralUnmodifiableLiveNodeSetTest.java', 'w') as f:
    f.write(content)

with open('./pg-multiverse/src/test/java/dev/chpg/pg/multiverse/ephemeral/EphemeralUnmodifiableLiveEdgeSetTest.java', 'r') as f:
    content = f.read()

content = re.sub(r'Map<Integer, dev.chpg.pg.multiverse.ephemeral.EphemeralEdge> map = new HashMap<>\(\);', 'Map<Integer, Edge> map = new HashMap<>();', content)
content = re.sub(r'Map<Integer, dev.chpg.pg.multiverse.ephemeral.EphemeralNode> nodesMap = new HashMap<>\(\);', 'Map<Integer, Node> nodesMap = new HashMap<>();', content)

with open('./pg-multiverse/src/test/java/dev/chpg/pg/multiverse/ephemeral/EphemeralUnmodifiableLiveEdgeSetTest.java', 'w') as f:
    f.write(content)
