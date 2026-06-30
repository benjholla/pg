package io.github.benjholla.pg;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Tags denote set membership
 */
public class TagSet extends HashSet<String> {

    public TagSet() {
        super();
    }

    public TagSet(Collection<? extends String> c) {
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

    @Override
    public String toString() {
        return "TagSet [tags=" + stream().collect(Collectors.joining(", ")) + "]";
    }

}
