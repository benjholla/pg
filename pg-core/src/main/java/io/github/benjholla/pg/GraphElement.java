package io.github.benjholla.pg;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class GraphElement {
	
	private ElementId id;
	
	private TagSet tags;
	
	// attributes define specialized graph properties
	private Map<String,Object> attributes;
	
	protected GraphElement() {
		this.id = ElementIdFactory.INSTANCE.create();
		this.tags = new TagSet();
		this.attributes = new HashMap<String,Object>();
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
	public Map<String,Object> attributes(){
		return attributes;
	}
	
	/**
	 * Checks if an attribute with the specified name exists.
	 *
	 * @param name the attribute key
	 * @return true if the attribute exists, false otherwise
	 */
	public boolean hasAttr(String name) {
		return attributes.containsKey(name);
	}
	
	/**
	 * Associates the specified value with the specified attribute name.
	 *
	 * @param name the attribute key
	 * @param value the attribute value
	 * @return the previous value associated with name, or null if there was no mapping
	 */
	public Object putAttr(String name, Object value) {
		return attributes.put(name, value);
	}
	
	/**
	 * Returns the value to which the specified attribute name is mapped.
	 *
	 * @param name the attribute key
	 * @return the attribute value, or null if this element contains no mapping for the name
	 */
	public Object getAttr(String name) {
		return attributes.get(name);
	}
	
	/**
	 * Removes the mapping for a specified attribute name if it is present.
	 *
	 * @param name the attribute key
	 * @return the previous value associated with name, or null if there was no mapping
	 */
	public Object removeAttr(String name) {
		return attributes.remove(name);
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
