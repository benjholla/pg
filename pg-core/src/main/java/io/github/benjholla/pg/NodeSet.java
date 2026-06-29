package io.github.benjholla.pg;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class NodeSet implements Collection<Node> {
    
    private Set<Node> nodes = new HashSet<>();

    public NodeSet() {}
    
    public NodeSet(Node initialNode) {
        nodes.add(initialNode);
    }
    
    public NodeSet(Node... initialNodes) {
        Collections.addAll(nodes, initialNodes);
    }
    
    public NodeSet(Collection<Node> initialNodes) {
        nodes.addAll(initialNodes);
    }
    
    @Override
    public int size() {
        return nodes.size();
    }

    @Override
    public boolean isEmpty() {
        return nodes.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return nodes.contains(o);
    }

    @Override
    public Iterator<Node> iterator() {
        return nodes.iterator();
    }

    @Override
    public Object[] toArray() {
        return nodes.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return nodes.toArray(a);
    }

    @Override
    public boolean add(Node e) {
        return nodes.add(e);
    }

    @Override
    public boolean remove(Object o) {
        return nodes.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return nodes.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends Node> c) {
        return nodes.addAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return nodes.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return nodes.retainAll(c);
    }

    @Override
    public void clear() {
        nodes.clear();
    }
    
    /**
     * Return any node in the set or empty if none exist
     * @return
     */
    public Optional<Node> one() {
        return nodes.stream().findAny();
    }
    
    /**
     * Returns a node set filtered to nodes with the attribute key and value
     * @param attribute
     * @param value
     * @return
     */
    public NodeSet filter(String attribute){
        NodeSet result = new NodeSet();
        for(Node node : this){
            if(node.hasAttr(attribute)){
                result.add(node);
            }
        }
        return result;
    }
    
    /**
     * Returns a node set filtered to nodes with the attribute key and value
     * @param attribute
     * @param value
     * @return
     */
    public NodeSet filter(String attribute, Object... values){
        NodeSet result = new NodeSet();
        if(attribute != null && values != null){
            for(Node node : this){
                if(node.hasAttr(attribute)) {
                    Object attributeValue = node.attributes().get(attribute);
                    if(attributeValue != null) {
                        for(Object value : values) {
                            if(value != null) {
                                if(Objects.equals(attributeValue, value)) {
                                    result.add(node);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodes);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof NodeSet)) {
            return false;
        }
        NodeSet other = (NodeSet) obj;
        return Objects.equals(nodes, other.nodes);
    }

    @Override
    public String toString() {
        return "NodeSet [nodes=" + nodes + "]";
    }
    
}
