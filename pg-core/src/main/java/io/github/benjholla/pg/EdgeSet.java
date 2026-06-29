package io.github.benjholla.pg;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class EdgeSet implements Collection<Edge> {
    
    private Set<Edge> edges = new HashSet<>();

    public EdgeSet() {}
    
    public EdgeSet(Edge initialEdge) {
        edges.add(initialEdge);
    }
    
    public EdgeSet(Edge... initialEdges) {
        Collections.addAll(edges, initialEdges);
    }
    
    public EdgeSet(Collection<Edge> initialEdges) {
        edges.addAll(initialEdges);
    }
    
    @Override
    public int size() {
        return edges.size();
    }

    @Override
    public boolean isEmpty() {
        return edges.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return edges.contains(o);
    }

    @Override
    public Iterator<Edge> iterator() {
        return edges.iterator();
    }

    @Override
    public Object[] toArray() {
        return edges.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return edges.toArray(a);
    }

    @Override
    public boolean add(Edge e) {
        return edges.add(e);
    }

    @Override
    public boolean remove(Object o) {
        return edges.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return edges.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends Edge> c) {
        return edges.addAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return edges.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return edges.retainAll(c);
    }

    @Override
    public void clear() {
        edges.clear();
    }
    
    /**
     * Return any edge in the set or empty if none exist
     * @return
     */
    public Optional<Edge> one() {
        return edges.stream().findAny();
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
    public int hashCode() {
        return Objects.hash(edges);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof EdgeSet)) {
            return false;
        }
        EdgeSet other = (EdgeSet) obj;
        return Objects.equals(edges, other.edges);
    }

    @Override
    public String toString() {
        return "EdgeSet [edges=" + edges + "]";
    }
    
}
