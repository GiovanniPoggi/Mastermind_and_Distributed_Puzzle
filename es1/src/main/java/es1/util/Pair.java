package es1.util;

/**
 * Class that manage the Pair of the Code of the Game.
 * @param <K>
 * @param <V>
 */
public class Pair<K, V> {

    /**
     * Fields that represents Key and Values of the Pair.
     */
    private final K key;
    private final V values;

    /**
     * Constructor of the Class.
     * @param key of the Pair.
     * @param values of the Pair.
     */
    public Pair(K key, V values) {
        this.key = key;
        this.values = values;
    }

    /**
     * Method that Get the Key of the Pair.
     * @return Key of the Pair.
     */
    public K getKey() {
        return key;
    }

    /**
     * Method that Get the Values of the Pair.
     * @return values of the Pair.
     */
    public V getValue() {
        return values;
    }
}
