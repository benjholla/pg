package io.github.benjholla.pg.io;

import java.util.Arrays;

/**
 * A bespoke, highly specialized, fixed-size open-addressed primitive map for ints.
 *
 * Rationale:
 * This map avoids bringing in third-party dependencies like Fastutil or Eclipse Collections.
 * It leverages the DirectGraphBuffer format's guarantee that the exact totalNodeCount
 * is known beforehand via the magic header, preventing any need for dynamic resizing.
 *
 * It is backed by two contiguous primitive arrays for perfect L1 cache locality
 * during the edge-reading phase. Linear probing is used with `-1` as the empty marker.
 */
public class IntIntMap {

    private static final int EMPTY = -1;

    private final int[] keys;
    private final int[] values;
    private final int capacity;
    private final int mask;

    /**
     * Initializes the arrays. Pre-sizing to a power-of-two capacity ensures a
     * maximum 0.5 load factor for fast linear probing and allows bitwise masking.
     *
     * @param expectedSize The maximum number of entries (nodes) this map will hold.
     */
    public IntIntMap(int expectedSize) {
        // Find the next power of 2 that guarantees at least a 0.5 load factor
        int targetCapacity = Math.max(2, expectedSize * 2);
        int powerOfTwo = 2;
        while (powerOfTwo < targetCapacity) {
            powerOfTwo <<= 1;
        }

        this.capacity = powerOfTwo;
        this.mask = this.capacity - 1; // Used for ultra-fast bitwise modulo

        this.keys = new int[this.capacity];
        this.values = new int[this.capacity];

        // Make 0 a legal ID by filling the keys array with -1
        Arrays.fill(this.keys, EMPTY);
    }

    /**
     * Inserts a key-value pair using linear probing.
     * Assumes keys are non-negative (file IDs).
     */
    public void put(int key, int value) {
        if (key == EMPTY) {
            throw new IllegalArgumentException("Key cannot be " + EMPTY + " (used as empty marker)");
        }

        // Bitwise AND replaces modulo for faster index calculation
        int index = hash(key) & mask;

        while (keys[index] != EMPTY && keys[index] != key) {
            index = (index + 1) & mask;
        }

        keys[index] = key;
        values[index] = value;
    }

    /**
     * Retrieves a value for the given key.
     *
     * @return the value, or EMPTY (-1) if not found.
     */
    public int get(int key) {
        int index = hash(key) & mask;

        while (keys[index] != EMPTY) {
            if (keys[index] == key) {
                return values[index];
            }
            index = (index + 1) & mask;
        }

        return EMPTY; // Not found
    }

    /**
     * A simple hash function to spread out keys.
     */
    private int hash(int key) {
        int h = key;
        h ^= h >>> 16;
        h *= 0x85ebca6b;
        h ^= h >>> 13;
        h *= 0xc2b2ae35;
        h ^= h >>> 16;
        return h; // No need to strip the sign bit; the bitwise mask handles it safely
    }
}
