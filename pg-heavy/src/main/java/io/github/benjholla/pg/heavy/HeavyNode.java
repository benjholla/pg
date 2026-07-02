package io.github.benjholla.pg.heavy;

import io.github.benjholla.pg.api.*;

public class HeavyNode extends HeavyGraphElement implements Node {


    @Override
    public String toString() {
        return "HeavyNode [ attributes=" + this.attributes().toString() + ", tags=" + this.tags().toString() + "]";
    }

}
