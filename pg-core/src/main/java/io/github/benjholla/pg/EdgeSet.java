package io.github.benjholla.pg;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;

public class EdgeSet extends HashSet<Edge> {
    
    private static final long serialVersionUID = 1L;

    public EdgeSet() {
        super();
    }
    
    public EdgeSet(Edge initialEdge) {
        super();
        add(Objects.requireNonNull(initialEdge, "Edge cannot be null"));
    }
    
    public EdgeSet(Edge... initialEdges) {
        super();
        Objects.requireNonNull(initialEdges, "Edge array cannot be null");
        for (Edge e : initialEdges) add(Objects.requireNonNull(e, "Edge cannot be null"));
    }
    
    public EdgeSet(Collection<Edge> initialEdges) {
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
       EdgeSet result = new EdgeSet();
       for(Edge edge : this){
           if(edge.hasAttr(attribute)){
               result.add(edge);
           }
       }
       return result;
   }
   
   /**
    * Returns a edge set filtered to edges with the attribute key and value
    */
   public EdgeSet filter(String attribute, Object... values){
       EdgeSet result = new EdgeSet();
       if(attribute != null && values != null){
           for(Edge edge : this){
               Object attributeValue = edge.getAttr(attribute);
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
        return "EdgeSet [edges=" + super.toString() + "]";
    }
    
}
