import re

for filename in ["pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/ShadowEdgeSet.java", "pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/ShadowNodeSet.java"]:
    with open(filename, 'r') as f:
        text = f.read()

    # if (!(obj instanceof Edge edge)) return false;
    text = re.sub(r'if \(!\(obj instanceof (\w+) (\w+)\)\) return false;', r'if (!(obj instanceof \1 \2)) { return false; }', text)

    # if (local.equals(node) || node.equals(local)) return true;
    text = re.sub(r'if \((local\.equals\(.*?\)|.*?\.equals\(local\))\) return true;', r'if (\1) { return true; }', text)

    # if (local instanceof ShadowUniverseNode && ((ShadowUniverseNode) local).id() == node.id()) return true;
    text = re.sub(r'if \((local instanceof .*?)\) return true;', r'if (\1) { return true; }', text)

    # if (node instanceof ShadowUniverseNode && ((ShadowUniverseNode) node).id() == local.id()) return true;
    text = re.sub(r'if \((node instanceof .*?)\) return true;', r'if (\1) { return true; }', text)

    # if (edge instanceof ShadowEdge && ((ShadowEdge) edge).backingEdge().equals(local)) return true;
    text = re.sub(r'if \((edge instanceof .*?)\) return true;', r'if (\1) { return true; }', text)

    # if (size() == 0) return EdgeSet.empty();
    text = re.sub(r'if \(size\(\) == 0\) return (.*?);', r'if (size() == 0) { return \1; }', text)

    # if (size() == 1) return new dev.chpg.pg.api.GenericImmutableEdgeSet(java.util.Collections.singleton(one().get()));
    text = re.sub(r'if \(size\(\) == 1\) return (.*?);', r'if (size() == 1) { return \1; }', text)

    # if (node instanceof ShadowUniverseNode && ((ShadowUniverseNode) node).id() == local.id()) { return true; } (Catching ones that I previously fixed partially)
    text = re.sub(r'if \(size\(\) == 0\) \{ return (.*?); \} \(size\(\) == 1\) \{ return (.*?); \}', r'if (size() == 0) { return \1; }\n        if (size() == 1) { return \2; }', text)

    with open(filename, 'w') as f:
        f.write(text)
