with open('./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/EphemeralEdgeSet.java', 'r') as f:
    content = f.read()

# `EphemeralEdgeSet.addAll()` is still casting to `EphemeralEdge` somewhere!
# Let's check `addAll` in `EphemeralEdgeSet.java`.
import re
print("EphemeralEdgeSet addAll:")
print(re.search(r'    public boolean addAll\(Collection<\? extends Edge> c\) \{.*?    \}', content, flags=re.DOTALL).group(0))
