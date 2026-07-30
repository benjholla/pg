# Restore from git HEAD and re-apply changes properly since Regex messed them up
import os
os.system("git reset HEAD ./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/ShadowNodeSet.java ./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/ShadowEdgeSet.java")
os.system("git checkout ./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/ShadowNodeSet.java ./pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/ShadowEdgeSet.java")
