import re

with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/EphemeralEdgeSet.java', 'r') as f:
    content = f.read()

content = content.replace("modified |= internalSet.add((EphemeralEdge) e);", "modified |= internalSet.add(e);")
content = content.replace("internalSet.remove((EphemeralEdge) o)", "internalSet.remove(o)")

with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/EphemeralEdgeSet.java', 'w') as f:
    f.write(content)


with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/EphemeralNodeSet.java', 'r') as f:
    content = f.read()

content = content.replace("modified |= internalSet.add((EphemeralNode) e);", "modified |= internalSet.add(e);")
content = content.replace("internalSet.remove((EphemeralNode) o)", "internalSet.remove(o)")

with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/EphemeralNodeSet.java', 'w') as f:
    f.write(content)
