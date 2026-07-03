package io.github.benjholla.pg.universe.ephemeral;

import io.github.benjholla.pg.api.Node;

public class EphemeralNode extends EphemeralGraphElement implements Node {


    @Override
    public String toString() {
        return "EphemeralNode [ attributes=" + this.attributes().toString() + ", tags=" + this.tags().toString() + "]";
    }

}
