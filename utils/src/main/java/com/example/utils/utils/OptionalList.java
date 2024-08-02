package com.example.utils.utils;

import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class OptionalList<T> {


    private final List<T> value;
    private final boolean isPresent;
    private static final OptionalList<?> EMPTY = new OptionalList<>();

    private static <T> OptionalList<T> empty() {
        @SuppressWarnings("unchecked")
        OptionalList<T> t = (OptionalList<T>) EMPTY;
        return t;
    }

    private static <T> boolean isEmpty(List<T> list) {
        return CollectionUtils.isEmpty(list) || list.stream().allMatch(Objects::isNull);
    }

    private OptionalList() {
        this.value = new ArrayList<>();
        this.isPresent = false;
    }

    private OptionalList(List<T> value) {
        this.value = value;
        this.isPresent = !isEmpty(value);
    }


    public static <T> OptionalList<T> of(List<T> list) {
        return new OptionalList<>(list);
    }

    public void ifPresent(ListConsumer<T> consumer) {
        if (isPresent) {
            consumer.accept(value);
        }
    }

    public List<T> get() {
        return value;
    }

    public <U> OptionalList<U> map(Function<? super List<T>, ? extends List<U>> mapper) {

        if (isPresent) {
            List<U> apply = mapper.apply(value);
            return OptionalList.of(apply);
        } else {
            return empty();
        }


    }

    public List<T> orElse(List<T> defaultValue) {
        return isEmpty(value) ? defaultValue : value;
    }
}
