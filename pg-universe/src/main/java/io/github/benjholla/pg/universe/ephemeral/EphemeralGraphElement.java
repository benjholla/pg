package io.github.benjholla.pg.universe.ephemeral;

import java.util.Objects;

import io.github.benjholla.pg.api.AttributeMap;
import io.github.benjholla.pg.api.GraphElement;
import io.github.benjholla.pg.api.TagSet;

public abstract class EphemeralGraphElement implements GraphElement {

	private final int id;

	private final TagSet tags;

	// attributes define specialized graph properties
	private final AttributeMap attributes;

	protected EphemeralGraphElement() {
		this.id = EphemeralIdGenerator.INSTANCE.create();
		this.tags = new EphemeralTagSet();
		this.attributes = new EphemeralAttributeMap();
	}

	/**
	 * Returns the dynamic boolean tags associated with this element.
	 *
	 * @return the TagSet for this element
	 */
	public TagSet tags(){
		return tags;
	}

	/**
	 * Returns the map of arbitrary key-value properties associated with this element.
	 *
	 * @return the attribute map
	 */
	public AttributeMap attributes(){
		return attributes;
	}

	public int id() {
		return id;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof EphemeralGraphElement))
			return false;
		EphemeralGraphElement other = (EphemeralGraphElement) obj;
		return Objects.equals(id, other.id);
	}

}
