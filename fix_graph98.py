with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/EphemeralNodeSet.java', 'r') as f:
    content = f.read()
content = content.replace("return new EphemeralImmutableSingletonNodeSet(internalSet.iterator().next());", "return new EphemeralImmutableSingletonNodeSet((EphemeralNode) internalSet.iterator().next());")
with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/EphemeralNodeSet.java', 'w') as f:
    f.write(content)
