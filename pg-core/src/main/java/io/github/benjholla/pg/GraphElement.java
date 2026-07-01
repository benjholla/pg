package io.github.benjholla.pg;



import java.util.Objects;

public abstract class GraphElement {
	
	private ElementId id;
	
	private TagSet tags;
	
	// attributes define specialized graph properties
	private AttributeMap attributes;
	
	protected GraphElement() {
		this.id = ElementIdFactory.INSTANCE.create();
		this.tags = new TagSet();
		this.attributes = new AttributeMap();
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
	
	public ElementId getId() {
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
		if (!(obj instanceof GraphElement))
			return false;
		GraphElement other = (GraphElement) obj;
		return Objects.equals(id, other.id);
	}
	
}
