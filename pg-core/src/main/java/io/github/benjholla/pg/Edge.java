package io.github.benjholla.pg;

public class Edge extends GraphElement {

	private Node from;
	private Node to;
	
	public Edge(Node from, Node to) {
		super();
		this.from = from;
		this.to = to;
	}
	
	public Node from() {
		return from;
	}

	public Node to() {
		return to;
	}

	@Override
	public String toString() {
		return "Edge [from=" + from + ", to=" + to + ", attributes=" + this.attributes().toString() + ", tags=" + this.tags().toString() + "]";
	}
	
}
