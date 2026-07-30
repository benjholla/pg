import re
with open('./pg-multiverse/src/test/java/dev/chpg/pg/multiverse/ephemeral/EphemeralUnmodifiableLiveNodeSetTest.java', 'r') as f:
    content = f.read()

content = re.sub(r'Map<Integer, EphemeralNode> map = new HashMap<>\(\);', 'Map<Integer, Node> map = new HashMap<>();', content)
content = re.sub(r'Map<Integer, EphemeralNode> map = new HashMap<Integer, Node>\(\);', 'Map<Integer, Node> map = new HashMap<>();', content)
content = re.sub(r'Map<Integer, EphemeralNode>', 'Map<Integer, Node>', content)

with open('./pg-multiverse/src/test/java/dev/chpg/pg/multiverse/ephemeral/EphemeralUnmodifiableLiveNodeSetTest.java', 'w') as f:
    f.write(content)

with open('./pg-multiverse/src/test/java/dev/chpg/pg/multiverse/ephemeral/EphemeralUnmodifiableLiveEdgeSetTest.java', 'r') as f:
    content = f.read()

content = re.sub(r'Map<Integer, EphemeralNode> nodesMap = new HashMap<>\(\);', 'Map<Integer, Node> nodesMap = new HashMap<>();', content)
content = re.sub(r'Map<Integer, EphemeralNode> nodesMap = new HashMap<Integer, Node>\(\);', 'Map<Integer, Node> nodesMap = new HashMap<>();', content)
content = re.sub(r'Map<Integer, EphemeralNode>', 'Map<Integer, Node>', content)

with open('./pg-multiverse/src/test/java/dev/chpg/pg/multiverse/ephemeral/EphemeralUnmodifiableLiveEdgeSetTest.java', 'w') as f:
    f.write(content)
