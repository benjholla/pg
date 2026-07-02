package io.github.benjholla.pg.heavy;

import io.github.benjholla.pg.api.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;

public class HeavyEdgeSet extends HashSet<Edge> implements EdgeSet {

    private static final long serialVersionUID = 1L;

    public HeavyEdgeSet() {
        super();
    }

    public HeavyEdgeSet(Edge initialEdge) {
        super();
        add(Objects.requireNonNull(initialEdge, "Edge cannot be null"));
    }

    public HeavyEdgeSet(Edge... initialEdges) {
        super();
        Objects.requireNonNull(initialEdges, "Edge array cannot be null");
        for (Edge e : initialEdges) add(Objects.requireNonNull(e, "Edge cannot be null"));
    }

    public HeavyEdgeSet(Collection<Edge> initialEdges) {
        super();
        Objects.requireNonNull(initialEdges, "Edge collection cannot be null");
        for (Edge e : initialEdges) add(Objects.requireNonNull(e, "Edge cannot be null"));
    }

    /**
     * Return any edge in the set or empty if none exist
     */
    public Optional<Edge> one() {
        return stream().findAny();
    }

   /**
    * Returns a edge set filtered to edges with the attribute key and value
    */
   public EdgeSet filter(String attribute){
       HeavyEdgeSet result = new HeavyEdgeSet();
       for(Edge edge : this){
           if(edge.attributes().containsKey(attribute)){
               result.add(edge);
           }
       }
       return result;
   }

   /**
    * Returns a edge set filtered to edges with the attribute key and value
    */
   public EdgeSet filter(String attribute, Object... values){
       HeavyEdgeSet result = new HeavyEdgeSet();
       if(attribute != null && values != null){
           for(Edge edge : this){
               Object attributeValue = edge.attributes().get(attribute);
               if(attributeValue != null) {
                   for(Object value : values) {
                       if(value != null) {
                           if(Objects.equals(attributeValue, value)) {
                               result.add(edge);
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
    public boolean add(Edge e) {
        return super.add(Objects.requireNonNull(e, "Edge cannot be null"));
    }

    @Override
    public boolean addAll(Collection<? extends Edge> c) {
        Objects.requireNonNull(c, "Edge collection cannot be null");
        boolean modified = false;
        for (Edge e : c) {
            if (super.add(Objects.requireNonNull(e, "Edge cannot be null"))) modified = true;
        }
        return modified;
    }

    @Override
    public String toString() {
        return "HeavyEdgeSet [edges=" + super.toString() + "]";
    }

}
