package com.cafeteria.app.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/** Construye un Pageable a partir de page/size/sort ("campo,dir"). */
public final class PageUtils {

    private PageUtils() {}

    public static Pageable of(int page, int size, String sort) {
        if (sort == null || sort.isBlank()) {
            return PageRequest.of(page, size);
        }
        String[] parts = sort.split(",");
        Sort.Direction dir = (parts.length > 1 && parts[1].trim().equalsIgnoreCase("desc"))
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        return PageRequest.of(page, size, Sort.by(dir, parts[0].trim()));
    }
}
