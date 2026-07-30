import re
with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/ShadowNodeSet.java', 'r') as f:
    content = f.read()

content = content.replace("        if (size() \n    == 1) {\n            return new dev.chpg.pg.api.GenericImmutableNodeSet(java.util.Collections.singleton(one().get()));\n        }\n}", "        if (size() == 1) return new dev.chpg.pg.api.GenericImmutableNodeSet(java.util.Collections.singleton(one().get()));\n        return new dev.chpg.pg.api.GenericImmutableNodeSet(this);")
content = content.replace("            }\n    \n    @Override\n    public boolean add(Node e) {", "    }\n    \n    @Override\n    public boolean add(Node e) {")
with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/ShadowNodeSet.java', 'w') as f:
    f.write(content)

with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/ShadowEdgeSet.java', 'r') as f:
    content = f.read()

content = content.replace("        if (size() \n    == 1) {\n            return new dev.chpg.pg.api.GenericImmutableEdgeSet(java.util.Collections.singleton(one().get()));\n        }\n}", "        if (size() == 1) return new dev.chpg.pg.api.GenericImmutableEdgeSet(java.util.Collections.singleton(one().get()));\n        return new dev.chpg.pg.api.GenericImmutableEdgeSet(this);")
content = content.replace("            }\n    \n    @Override\n    public boolean add(Edge e) {", "    }\n    \n    @Override\n    public boolean add(Edge e) {")
with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/ShadowEdgeSet.java', 'w') as f:
    f.write(content)
