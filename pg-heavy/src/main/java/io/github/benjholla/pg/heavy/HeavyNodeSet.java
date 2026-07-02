package io.github.benjholla.pg.heavy;

import java.util.Collection;
import java.util.HashSet;
import io.github.benjholla.pg.api.AttributeValue;
import java.util.Objects;
import java.util.Optional;

import io.github.benjholla.pg.api.Node;
import io.github.benjholla.pg.api.NodeSet;

public class HeavyNodeSet extends HashSet<Node> implements NodeSet {

    private static final long serialVersionUID = 1L;

    public HeavyNodeSet() {
        super();
    }

    public HeavyNodeSet(Node initialNode) {
        super();
        add(Objects.requireNonNull(initialNode, "Node cannot be null"));
    }

    public HeavyNodeSet(Node... initialNodes) {
        super();
        Objects.requireNonNull(initialNodes, "Node array cannot be null");
        for (Node n : initialNodes) add(Objects.requireNonNull(n, "Node cannot be null"));
    }

    public HeavyNodeSet(Collection<Node> initialNodes) {
        super();
        Objects.requireNonNull(initialNodes, "Node collection cannot be null");
        for (Node n : initialNodes) add(Objects.requireNonNull(n, "Node cannot be null"));
    }

    /**
     * Return any node in the set or empty if none exist
     */
    public Optional<Node> one() {
        return stream().findAny();
    }

    /**
     * Returns a node set filtered to nodes with the attribute key and value
     */
    public NodeSet filter(String attribute){
        HeavyNodeSet result = new HeavyNodeSet();
        for(Node node : this){
           if(node.attributes().containsKey(attribute)){
                result.add(node);
            }
        }
        return result;
    }

    /**
     * Returns a node set filtered to nodes with the attribute key and value
     */
    public NodeSet filter(String attribute, AttributeValue... values){
        HeavyNodeSet result = new HeavyNodeSet();
        if(attribute != null && values != null){
            for(Node node : this){
               AttributeValue attributeValue = node.attributes().get(attribute);
                if(attributeValue != null) {
                    for(AttributeValue value : values) {
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
        return "HeavyNodeSet [nodes=" + super.toString() + "]";
    }

}
