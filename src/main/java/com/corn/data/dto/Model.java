package com.corn.data.dto;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Generic POJO for carrying a single value
 *
 * @author Oleg Zaidullin
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class Model<T> {

    public T getValue() { return value; }

    private T value;

    @JsonCreator
    public Model(T value) {
        this.value = value;
    }
}

