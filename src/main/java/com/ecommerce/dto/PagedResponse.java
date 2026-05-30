package com.ecommerce.dto;

import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
public class PagedResponse<T> {

    private final List<T> content;
    private final int     page;           // current page number (0-indexed)
    private final int     size;           // items per page
    private final long    totalElements;  // total records in DB
    private final int     totalPages;     // total number of pages
    private final boolean last;           // true if this is the last page

    // Takes Spring's Page<T> and extracts only the fields you actually want
    public PagedResponse(Page<T> page) {
        this.content       = page.getContent();
        this.page          = page.getNumber();
        this.size          = page.getSize();
        this.totalElements = page.getTotalElements();
        this.totalPages    = page.getTotalPages();
        this.last          = page.isLast();
    }
}