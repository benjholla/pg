package io.github.benjholla.pg;

public class Node extends GraphElement {
	
	public static enum NodeDirection {
		IN, OUT;
	}

    @Override
    public String toString() {
        return "Node [ attributes=" + this.attributes().toString() + ", tags=" + this.tags().toString() + "]";
    }
	
}
