package io.github.benjholla.pg;



import java.util.Objects;

public abstract class HeavyGraphElement implements GraphElement {

	private final int id;

	private final TagSet tags;

	// attributes define specialized graph properties
	private final AttributeMap attributes;

	protected HeavyGraphElement() {
		this.id = IdGenerator.INSTANCE.create();
		this.tags = new HeavyTagSet();
		this.attributes = new HeavyAttributeMap();
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
		if (!(obj instanceof HeavyGraphElement))
			return false;
		HeavyGraphElement other = (HeavyGraphElement) obj;
		return Objects.equals(id, other.id);
	}

}
