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
	
	public TagSet tags(){
		return tags;
	}
	
	public Map<String,Object> attributes(){
		return attributes;
	}
	
	public boolean hasAttr(String name) {
		return attributes.containsKey(name);
	}
	
	public Object putAttr(String name, Object value) {
		return attributes.put(name, value);
	}
	
	public Object getAttr(String name) {
		return attributes.get(name);
	}
	
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
