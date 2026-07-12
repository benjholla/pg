package io.github.benjholla.pg.io;

/**
 * A bespoke, highly specialized, fixed-size open-addressed primitive map for ints.
 *
 * Rationale:
 * This map avoids bringing in third-party dependencies like Fastutil or Eclipse Collections.
 * It leverages the DirectGraphBuffer format's guarantee that the exact totalNodeCount
 * is known beforehand via the magic header, preventing any need for dynamic resizing.
 *
 * It is backed by two contiguous primitive arrays for perfect L1 cache locality
 * during the edge-reading phase. Linear probing is used with `0` as the empty marker.
 */
public class IntIntMap {

    private final int[] keys;
    private final int[] values;
    private final int capacity;

    /**
     * Initializes the arrays. Pre-sizing to capacity ensures a 0.5 load factor
     * to guarantee fast linear probing without needing resizing.
     *
     * @param expectedSize The maximum number of entries (nodes) this map will hold.
     */
    public IntIntMap(int expectedSize) {
        // We multiply by 2 for a 0.5 load factor to minimize collisions.
        this.capacity = Math.max(2, expectedSize * 2);
        this.keys = new int[this.capacity];
        this.values = new int[this.capacity];
    }

    /**
     * Inserts a key-value pair using linear probing.
     * Assumes keys are strictly non-zero (0 is the empty marker).
     */
    public void put(int key, int value) {
        if (key == 0) {
            throw new IllegalArgumentException("Key cannot be 0 (used as empty marker)");
        }

        int index = hash(key) % capacity;

        while (keys[index] != 0 && keys[index] != key) {
            index = (index + 1) % capacity;
        }

        keys[index] = key;
        values[index] = value;
    }

    /**
     * Retrieves a value for the given key.
     *
     * @return the value, or 0 if not found (assuming 0 is not a valid value for our use case).
     */
    public int get(int key) {
        int index = hash(key) % capacity;

        while (keys[index] != 0) {
            if (keys[index] == key) {
                return values[index];
            }
            index = (index + 1) % capacity;
        }

        return 0; // Not found
    }

    /**
     * A simple hash function to spread out keys (which are sequential IDs usually).
     */
    private int hash(int key) {
        // A simple mixing function (Murmur3 style)
        int h = key;
        h ^= h >>> 16;
        h *= 0x85ebca6b;
        h ^= h >>> 13;
        h *= 0xc2b2ae35;
        h ^= h >>> 16;
        // Ensure positive result for modulo
        return h & 0x7FFFFFFF;
    }
}
