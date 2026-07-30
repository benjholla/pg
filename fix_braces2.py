import re
for filename in ["pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/ShadowEdgeSet.java", "pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/ShadowNodeSet.java"]:
    with open(filename, 'r') as f:
        text = f.read()

    text = re.sub(r'if \(!contains\(o\)\) return false;', r'if (!contains(o)) { return false; }', text)
    text = re.sub(r'if \(isEmpty\(\)\) return EdgeSet\.empty\(\);', r'if (isEmpty()) { return EdgeSet.empty(); }', text)
    text = re.sub(r'if \(isEmpty\(\)\) return NodeSet\.empty\(\);', r'if (isEmpty()) { return NodeSet.empty(); }', text)

    with open(filename, 'w') as f:
        f.write(text)
