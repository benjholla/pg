package dev.chpg.pg.multiverse.universe;

import dev.chpg.pg.api.TagSet;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Objects;

/**
 * A zero-allocation, pass-through viewport into the Universe's columnar edge tag storage.
 */
public final class UniverseEdgeTagProxy extends AbstractSet<String> implements TagSet, UniverseView {

    private final Universe universe;
    private final int edgeId;

    UniverseEdgeTagProxy(Universe universe, int edgeId) {
        this.universe = Objects.requireNonNull(universe, "Universe cannot be null");
        this.edgeId = edgeId;
    }

    @Override public Universe universe() { return this.universe; }
    @Override public int size() { return this.universe.edgeTagCount(this.edgeId); }

    @Override
    public boolean contains(Object o) {
        if (!(o instanceof String)) {
            return false;
        }
        return this.universe.hasEdgeTag(this.edgeId, (String) o);
    }

    @Override
    public boolean add(String tag) {
        Objects.requireNonNull(tag, "Tag cannot be null");
        return this.universe.addEdgeTag(this.edgeId, tag);
    }

    @Override
    public boolean remove(Object o) {
        if (!(o instanceof String)) {
            return false;
        }
        return this.universe.removeEdgeTag(this.edgeId, (String) o);
    }

    @Override public Iterator<String> iterator() { return this.universe.edgeTagsIterator(this.edgeId); }
    @Override public void clear() { this.universe.clearEdgeTags(this.edgeId); }
}
