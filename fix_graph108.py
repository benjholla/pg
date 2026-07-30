with open('./pg-multiverse/src/test/java/dev/chpg/pg/multiverse/ephemeral/GraphAdjacentInvariantTest.java', 'r') as f:
    lines = f.readlines()

for i, line in enumerate(lines[70:85]):
    print(f"{i+71}: {line}", end='')
