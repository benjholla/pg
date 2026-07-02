package io.github.benjholla.pg.heavy;

import io.github.benjholla.pg.api.Edge;
import io.github.benjholla.pg.api.Node;

public class HeavyEdge extends HeavyGraphElement implements Edge {

	private Node from;
	private Node to;

	public HeavyEdge(Node from, Node to) {
		super();
		if (from == null || to == null) {
			throw new IllegalArgumentException("Edge endpoints cannot be null.");
		}
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
		return "HeavyEdge [from=" + from + ", to=" + to + ", attributes=" + this.attributes().toString() + ", tags=" + this.tags().toString() + "]";
	}

}
