with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/EphemeralEdgeSet.java', 'r') as f:
    content = f.read()

# Let's check `addAll` entirely.
lines = content.split('\n')
for i, line in enumerate(lines):
    if "public boolean addAll(Collection<? extends Edge> c)" in line:
        for j in range(i, i+15):
            print(f"{j}: {lines[j]}")
        break
