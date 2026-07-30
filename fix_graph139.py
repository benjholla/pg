with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/ShadowNodeSet.java', 'r') as f:
    content = f.read()

import re

# `ShadowNodeSet` has compile errors because we messed up the braces! Let's search for `== 1) return` in `toImmutable()` and replace it correctly
toImmutable_node = """    @Override
    public NodeSet toImmutable() {
        if (isEmpty()) return NodeSet.empty();
        if (size() == 1) return new dev.chpg.pg.api.GenericImmutableNodeSet(java.util.Collections.singleton(one().get()));
        return new dev.chpg.pg.api.GenericImmutableNodeSet(this);
    }"""
content = re.sub(r'    @Override\n    public NodeSet toImmutable\(\) \{.*?(?=    @Override\n    public boolean add)', toImmutable_node + '\n\n', content, flags=re.DOTALL)
with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/ShadowNodeSet.java', 'w') as f:
    f.write(content)


with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/ShadowEdgeSet.java', 'r') as f:
    content = f.read()
toImmutable_edge = """    @Override
    public EdgeSet toImmutable() {
        if (isEmpty()) return EdgeSet.empty();
        if (size() == 1) return new dev.chpg.pg.api.GenericImmutableEdgeSet(java.util.Collections.singleton(one().get()));
        return new dev.chpg.pg.api.GenericImmutableEdgeSet(this);
    }"""
content = re.sub(r'    @Override\n    public EdgeSet toImmutable\(\) \{.*?(?=    @Override\n    public boolean add)', toImmutable_edge + '\n\n', content, flags=re.DOTALL)
with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/ShadowEdgeSet.java', 'w') as f:
    f.write(content)
