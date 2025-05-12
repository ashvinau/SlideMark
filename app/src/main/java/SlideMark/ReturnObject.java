package SlideMark;

import java.util.Objects;

/**
 * A generic container for returning a value of type T from a method.
 * @param <T> the type of the encapsulated return value
 */
public class ReturnObject<T> {

    /**
     * The encapsulated return value.
     */
    private T value;

    /**
     * Constructs a ReturnObject wrapping the given value.
     * @param value the value to wrap
     */
    public ReturnObject(T value) {
        this.value = value;
    }

    /**
     * Retrieves the wrapped value.
     * @return the encapsulated value
     */
    public T getValue() {
        return value;
    }

    /**
     * Updates the wrapped value.
     * @param value the new value to encapsulate
     */
    public void setValue(T value) {
        this.value = value;
    }

    /**
     * Produces a string representation including the wrapped value.
     * @return a string of the form "ReturnObject[value=...]"
     */
    @Override
    public String toString() {
        return "ReturnObject[value=" + value + "]";
    }

    /**
     * Two ReturnObject instances are equal if their values or references are equal.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true; // Object references are the same
        if (!(o instanceof ReturnObject<?> that)) return false; // False if we are not comparing the same type
        return Objects.equals(value, that.value); // Directly compare values and return
    }

    @Override
    public int hashCode() {
        if (value != null) return value.hashCode();
        return 0;
    }
}

