package com.corn.data.dto;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.List;

@SuppressWarnings("unused")
public class PageDTO<T> {
    private List<T> content;
    private int totalPages;
    private long totalElements;

    @JsonCreator
    public PageDTO(List<T> content, int totalPages, long totalElements) {
        this.content = content;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
    }

    public List<T> getContent() {
        return content;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public long getTotalElements() {
        return totalElements;
    }

}