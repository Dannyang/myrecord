package com.example.utils.utils;

import java.util.List;
import java.util.Objects;

@FunctionalInterface
public interface ListConsumer<T> {
    /**
     * Performs this operation on the given argument.
     *
     * @param value the input argument
     */
    void accept(List<T> value);

    default ListConsumer<T> andThen(ListConsumer<T> after) {
        Objects.requireNonNull(after);
        return (List<T> t) -> { accept(t);
            after.accept(t); };
    }
}
