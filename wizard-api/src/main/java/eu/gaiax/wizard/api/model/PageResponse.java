package eu.gaiax.wizard.api.model;

import com.smartsensesolutions.java.commons.sort.Sort;
import org.springframework.data.domain.Page;

import java.io.Serializable;

public class PageResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private Iterable<T> content;
    private Pageable pageable;

    public static <C> PageResponse<C> of(Iterable<C> content, Integer currentPage, Integer numberOfElement, Integer size, Integer totalPages, Long totalElements, Sort sort) {
        PageResponse<C> page = new PageResponse<>();
        page.content = content;
        page.setPageable(new Pageable(size, totalPages, currentPage, numberOfElement, totalElements, sort));
        return page;
    }

    public static <C, D> PageResponse<C> of(Iterable<C> content, Page<D> page, Sort sort) {
        PageResponse<C> pageResponse = new PageResponse<>();
        pageResponse.content = content;
        pageResponse.setPageable(new Pageable(page.getSize(), page.getTotalPages(), page.getNumber(), page.getNumberOfElements(), page.getTotalElements(), sort));
        return pageResponse;
    }

    public static <C> PageResponse<C> of(Page<C> page, Sort sort) {
        return of(page.getContent(), page.getNumber(), page.getNumberOfElements(), page.getSize(), page.getTotalPages(), page.getTotalElements(), sort);
    }

    public static <C> PageResponse<C> of(Page<C> page) {
        return of(page, (Sort) null);
    }

    public Iterable<T> getContent() {
        return this.content;
    }

    public void setContent(Iterable<T> content) {
        this.content = content;
    }

    public Pageable getPageable() {
        return this.pageable;
    }

    public void setPageable(Pageable pageable) {
        this.pageable = pageable;
    }

    public static class Pageable implements Serializable {

        private static final Long serialVersionUID = 1L;
        private Integer pageSize;
        private Integer totalPages;
        private Integer pageNumber;
        private Integer numberOfElements;
        private Integer size;
        private Long totalElements;
        private Sort sort;

        public Pageable(Integer pageSize, Integer totalPages, Integer pageNumber, Integer numberOfElements, Long totalElements, Sort sort) {
            this.pageSize = pageSize;
            this.totalPages = totalPages;
            this.pageNumber = pageNumber;
            this.numberOfElements = numberOfElements;
            this.totalElements = totalElements;
            this.sort = sort;
        }

        public Integer getPageSize() {
            return this.pageSize;
        }

        public void setPageSize(Integer pageSize) {
            this.pageSize = pageSize;
        }

        public Integer getTotalPages() {
            return this.totalPages;
        }

        public void setTotalPages(Integer totalPages) {
            this.totalPages = totalPages;
        }

        public Integer getPageNumber() {
            return this.pageNumber;
        }

        public void setPageNumber(Integer pageNumber) {
            this.pageNumber = pageNumber;
        }

        public Integer getNumberOfElements() {
            return this.numberOfElements;
        }

        public void setNumberOfElements(Integer numberOfElements) {
            this.numberOfElements = numberOfElements;
        }

        public Long getTotalElements() {
            return this.totalElements;
        }

        public void setTotalElements(Long totalElements) {
            this.totalElements = totalElements;
        }

        public Sort getSort() {
            return this.sort;
        }

        public void setSort(Sort sort) {
            this.sort = sort;
        }

        public Integer getSize() {
            return this.size;
        }

        public void setSize(Integer size) {
            this.size = size;
        }
    }
}
