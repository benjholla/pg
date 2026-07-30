import re

with open('./pg-multiverse/src/test/java/dev/chpg/pg/multiverse/ephemeral/EphemeralUnmodifiableLiveNodeSetTest.java', 'r') as f:
    content = f.read()

content = re.sub(r'java\.util\.Map<Integer, EphemeralNode>', 'java.util.Map<Integer, dev.chpg.pg.api.Node>', content)

with open('./pg-multiverse/src/test/java/dev/chpg/pg/multiverse/ephemeral/EphemeralUnmodifiableLiveNodeSetTest.java', 'w') as f:
    f.write(content)

with open('./pg-multiverse/src/test/java/dev/chpg/pg/multiverse/ephemeral/EphemeralUnmodifiableLiveEdgeSetTest.java', 'r') as f:
    content = f.read()

content = re.sub(r'java\.util\.Map<Integer, EphemeralNode>', 'java.util.Map<Integer, dev.chpg.pg.api.Node>', content)

with open('./pg-multiverse/src/test/java/dev/chpg/pg/multiverse/ephemeral/EphemeralUnmodifiableLiveEdgeSetTest.java', 'w') as f:
    f.write(content)
