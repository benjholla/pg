package io.github.benjholla.pg.universe.ephemeral;

import io.github.benjholla.pg.api.Edge;
import io.github.benjholla.pg.api.Node;

public class EphemeralEdge extends EphemeralGraphElement implements Edge {

	private Node from;
	private Node to;

	public EphemeralEdge(Node from, Node to) {
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
		return "EphemeralEdge [from=" + from + ", to=" + to + ", attributes=" + this.attributes().toString() + ", tags=" + this.tags().toString() + "]";
	}

}
