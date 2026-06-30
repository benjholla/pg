package io.github.benjholla.pg;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;

public class NodeSet extends HashSet<Node> {
    
    private static final long serialVersionUID = 1L;

    public NodeSet() {
        super();
    }
    
    public NodeSet(Node initialNode) {
        super();
        add(Objects.requireNonNull(initialNode, "Node cannot be null"));
    }
    
    public NodeSet(Node... initialNodes) {
        super();
        Objects.requireNonNull(initialNodes, "Node array cannot be null");
        for (Node n : initialNodes) add(Objects.requireNonNull(n, "Node cannot be null"));
    }
    
    public NodeSet(Collection<Node> initialNodes) {
        super();
        Objects.requireNonNull(initialNodes, "Node collection cannot be null");
        for (Node n : initialNodes) add(Objects.requireNonNull(n, "Node cannot be null"));
    }
    
    /**
     * Return any node in the set or empty if none exist
     * @return
     */
    public Optional<Node> one() {
        return stream().findAny();
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
                Object attributeValue = node.getAttr(attribute);
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
        return result;
    }


    @Override
    public boolean add(Node e) {
        return super.add(Objects.requireNonNull(e, "Node cannot be null"));
    }

    @Override
    public boolean addAll(Collection<? extends Node> c) {
        Objects.requireNonNull(c, "Node collection cannot be null");
        boolean modified = false;
        for (Node e : c) {
            if (super.add(Objects.requireNonNull(e, "Node cannot be null"))) modified = true;
        }
        return modified;
    }

    @Override
    public String toString() {
        return "NodeSet [nodes=" + super.toString() + "]";
    }
    
}
