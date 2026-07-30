with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/ShadowEdgeSet.java', 'r') as f:
    content = f.read()

import re

# `if (size() == 1) {\n            return new dev.chpg.pg.api.GenericImmutableEdgeSet(java.util.Collections.singleton(one().get()));\n        }\n        return new dev.chpg.pg.api.GenericImmutableEdgeSet(this);\n    }`
# Let's fix this in `ShadowEdgeSet` and `ShadowNodeSet`
content = re.sub(r'    @Override\n    public EdgeSet toImmutable\(\) \{.*?return new dev\.chpg\.pg\.api\.GenericImmutableEdgeSet\(this\);\n    \}', """    @Override
    public EdgeSet toImmutable() {
        if (isEmpty()) return EdgeSet.empty();
        if (size() == 1) return new dev.chpg.pg.api.GenericImmutableEdgeSet(java.util.Collections.singleton(one().get()));
        return new dev.chpg.pg.api.GenericImmutableEdgeSet(this);
    }""", content, flags=re.DOTALL)
with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/ShadowEdgeSet.java', 'w') as f:
    f.write(content)

with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/ShadowNodeSet.java', 'r') as f:
    content = f.read()

content = re.sub(r'    @Override\n    public NodeSet toImmutable\(\) \{.*?return new dev\.chpg\.pg\.api\.GenericImmutableNodeSet\(this\);\n    \}', """    @Override
    public NodeSet toImmutable() {
        if (isEmpty()) return NodeSet.empty();
        if (size() == 1) return new dev.chpg.pg.api.GenericImmutableNodeSet(java.util.Collections.singleton(one().get()));
        return new dev.chpg.pg.api.GenericImmutableNodeSet(this);
    }""", content, flags=re.DOTALL)
with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/ShadowNodeSet.java', 'w') as f:
    f.write(content)
