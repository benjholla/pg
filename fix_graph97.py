with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/EphemeralNodeSet.java', 'r') as f:
    content = f.read()

# EphemeralNodeSet only accepts EphemeralNode in its internalSet!
# `private final HashSet<EphemeralNode> internalSet;`
# We didn't change it to `HashSet<Node>` like we did for `EphemeralEdgeSet`.
content = content.replace("private final HashSet<EphemeralNode> internalSet;", "private final HashSet<Node> internalSet;")
content = content.replace("for (EphemeralNode node : internalSet) {", "for (Node node : internalSet) {")
content = content.replace("Iterator<EphemeralNode> it", "Iterator<Node> it")
content = content.replace("public Optional<Node> one() {\n        if (internalSet.isEmpty()) { return Optional.empty(); }\n        return Optional.of(internalSet.iterator().next());\n    }", "public Optional<Node> one() {\n        if (internalSet.isEmpty()) { return Optional.empty(); }\n        return Optional.of(internalSet.iterator().next());\n    }")

with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/EphemeralNodeSet.java', 'w') as f:
    f.write(content)
