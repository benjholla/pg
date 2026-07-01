package io.github.benjholla.pg;

public class HeavyNode extends HeavyGraphElement implements Node {


    @Override
    public String toString() {
        return "HeavyNode [ attributes=" + this.attributes().toString() + ", tags=" + this.tags().toString() + "]";
    }

}
