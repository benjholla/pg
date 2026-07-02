package io.github.benjholla.pg.heavy;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

import io.github.benjholla.pg.api.TagSet;

/**
 * Tags denote set membership
 */
public class HeavyTagSet extends HashSet<String> implements TagSet {

    private static final long serialVersionUID = 1L;

    public HeavyTagSet() {
        super();
    }

    public HeavyTagSet(Collection<? extends String> c) {
        super(c);
    }


    @Override
    public boolean add(String e) {
        return super.add(Objects.requireNonNull(e, "Tag cannot be null"));
    }

    @Override
    public boolean addAll(Collection<? extends String> c) {
        Objects.requireNonNull(c, "Tag collection cannot be null");
        boolean modified = false;
        for (String e : c) {
            if (super.add(Objects.requireNonNull(e, "Tag cannot be null"))) modified = true;
        }
        return modified;
    }

}
