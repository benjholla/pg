package io.github.benjholla.pg.universe.ephemeral;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;

import io.github.benjholla.pg.api.AttributeValue;
import io.github.benjholla.pg.api.Edge;
import io.github.benjholla.pg.api.EdgeSet;

public class EphemeralEdgeSet extends HashSet<Edge> implements EdgeSet {

    private static final long serialVersionUID = 1L;

    public EphemeralEdgeSet() {
        super();
    }

    public EphemeralEdgeSet(Edge initialEdge) {
        super();
        add(Objects.requireNonNull(initialEdge, "Edge cannot be null"));
    }

    public EphemeralEdgeSet(Edge... initialEdges) {
        super();
        Objects.requireNonNull(initialEdges, "Edge array cannot be null");
        for (Edge e : initialEdges) add(Objects.requireNonNull(e, "Edge cannot be null"));
    }

    public EphemeralEdgeSet(Collection<Edge> initialEdges) {
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
       EphemeralEdgeSet result = new EphemeralEdgeSet();
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
   public EdgeSet filter(String attribute, AttributeValue... values){
       EphemeralEdgeSet result = new EphemeralEdgeSet();
       if(attribute != null && values != null){
           for(Edge edge : this){
               AttributeValue attributeValue = edge.attributes().get(attribute);
               if(attributeValue != null) {
                   for(AttributeValue value : values) {
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
        return "EphemeralEdgeSet [edges=" + super.toString() + "]";
    }

}
