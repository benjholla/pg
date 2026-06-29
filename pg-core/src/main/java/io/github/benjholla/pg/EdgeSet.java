package io.github.benjholla.pg;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;

public class EdgeSet extends HashSet<Edge> {
    
    public EdgeSet() {
        super();
    }
    
    public EdgeSet(Edge initialEdge) {
        super();
        add(initialEdge);
    }
    
    public EdgeSet(Edge... initialEdges) {
        super();
        Collections.addAll(this, initialEdges);
    }
    
    public EdgeSet(Collection<Edge> initialEdges) {
        super(initialEdges);
    }
    
    /**
     * Return any edge in the set or empty if none exist
     * @return
     */
    public Optional<Edge> one() {
        return stream().findAny();
    }
    
   /**
    * Returns a edge set filtered to edges with the attribute key and value
    * @param attribute
    * @param value
    * @return
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
    * @param attribute
    * @param value
    * @return
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
    public String toString() {
        return "EdgeSet [edges=" + super.toString() + "]";
    }
    
}
