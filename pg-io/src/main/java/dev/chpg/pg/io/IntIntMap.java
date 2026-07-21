package dev.chpg.pg.io;

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
        if (expectedSize < 0) {
            throw new IllegalArgumentException("expectedSize cannot be negative: " + expectedSize);
        }
        if (expectedSize > (1 << 29)) {
            throw new IllegalArgumentException("expectedSize is too large: " + expectedSize);
        }

        // Find the next power of 2 that guarantees at least a 0.5 load factor.
        // A load factor of <= 0.5 is critical to prevent linear probing performance degradation.
        int targetCapacity = Math.max(2, expectedSize * 2);
        int powerOfTwo = 2;
        while (powerOfTwo < targetCapacity) {
            powerOfTwo <<= 1;
        }

        this.capacity = powerOfTwo;
        this.mask = this.capacity - 1; // Used for ultra-fast bitwise modulo (e.g., hash & mask)

        this.keys = new int[this.capacity];
        this.values = new int[this.capacity];

        // Ensure 0 is a valid key (since Node IDs often start at 0) by using -1 as the empty marker
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
     * A highly avalanching integer hash function based on MurmurHash3's finalizer.
     *
     * Rationale:
     * Graph node IDs are often highly sequential (e.g., 0, 1, 2, 3).
     * Sequential keys can cluster badly in open-addressed maps. This finalizer
     * uses the MurmurHash3 mix constants (0x85ebca6b and 0xc2b2ae35) to fiercely
     * scatter sequential integers across the available bucket space, drastically
     * reducing linear probing collision chains.
     */
    private int hash(int key) {
        int h = key;
        h ^= h >>> 16;
        h *= 0x85ebca6b;
        h ^= h >>> 13;
        h *= 0xc2b2ae35;
        h ^= h >>> 16;
        return h; // No need to strip the sign bit; the power-of-two bitwise mask handles it safely
    }
}
