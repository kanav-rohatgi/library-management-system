package com.example.library.controller;

import com.example.library.service.LibraryException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

final class ApiPaging {
    private ApiPaging() {
    }

    static Pageable withAllowedSort(Pageable pageable, Set<String> allowed) {
        Sort sort = pageable.getSort();
        for (Sort.Order order : sort) {
            if (!allowed.contains(order.getProperty())) {
                throw LibraryException.invalid("Unsupported sort field: " + order.getProperty());
            }
        }
        if (sort.getOrderFor("id") == null) {
            sort = sort.and(Sort.by("id"));
        }
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }
}
