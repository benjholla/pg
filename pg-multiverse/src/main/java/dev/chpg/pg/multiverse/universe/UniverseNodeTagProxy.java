package dev.chpg.pg.multiverse.universe;

import dev.chpg.pg.api.TagSet;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Objects;

/**
 * A zero-allocation, pass-through viewport into the Universe's columnar node tag storage.
 */
public final class UniverseNodeTagProxy extends AbstractSet<String> implements TagSet, UniverseView {

    private final Universe universe;
    private final int nodeId;

    UniverseNodeTagProxy(Universe universe, int nodeId) {
        this.universe = Objects.requireNonNull(universe, "Universe cannot be null");
        this.nodeId = nodeId;
    }

    @Override public Universe universe() { return this.universe; }
    @Override public int size() { return this.universe.nodeTagCount(this.nodeId); }

    @Override
    public boolean contains(Object o) {
        if (!(o instanceof String)) {
            return false;
        }
        return this.universe.hasNodeTag(this.nodeId, (String) o);
    }

    @Override
    public boolean add(String tag) {
        Objects.requireNonNull(tag, "Tag cannot be null");
        return this.universe.addNodeTag(this.nodeId, tag);
    }

    @Override
    public boolean remove(Object o) {
        if (!(o instanceof String)) {
            return false;
        }
        return this.universe.removeNodeTag(this.nodeId, (String) o);
    }

    @Override public Iterator<String> iterator() { return this.universe.nodeTagsIterator(this.nodeId); }
    @Override public void clear() { this.universe.clearNodeTags(this.nodeId); }
}
