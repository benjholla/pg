import re
for f_name in ["pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/EphemeralEdgeSet.java", "pg-multiverse/src/main/java/dev/chpg/pg/multiverse/ephemeral/EphemeralNodeSet.java"]:
    with open(f_name, "r") as f:
        text = f.read()

    # The guardrail was removed. We can just check id < 0 manually since these are ephemeral sets that expect local adds
    if "EphemeralNodeSet" in f_name:
        text = text.replace("EphemeralGuardrails.requireLocalId(impl.id());", """if (impl.id() >= 0) { throw new IllegalArgumentException("Ephemeral sets only accept un-promoted local elements."); }""")
    else:
        text = text.replace("EphemeralGuardrails.requireLocalId(impl.id());", """if (impl.id() >= 0) { throw new IllegalArgumentException("Ephemeral sets only accept un-promoted local elements."); }""")

    with open(f_name, "w") as f:
        f.write(text)
