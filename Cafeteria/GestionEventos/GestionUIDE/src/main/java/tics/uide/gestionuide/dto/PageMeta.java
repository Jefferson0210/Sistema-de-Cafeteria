package tics.uide.gestionuide.dto;

import org.springframework.data.domain.Page;

/** Metadatos de paginación expuestos en ApiResponse.meta. */
public class PageMeta {
    private final int page;
    private final int size;
    private final int totalPages;
    private final long totalElements;

    public PageMeta(Page<?> p) {
        this.page = p.getNumber();
        this.size = p.getSize();
        this.totalPages = p.getTotalPages();
        this.totalElements = p.getTotalElements();
    }

    public int getPage() { return page; }
    public int getSize() { return size; }
    public int getTotalPages() { return totalPages; }
    public long getTotalElements() { return totalElements; }
}
