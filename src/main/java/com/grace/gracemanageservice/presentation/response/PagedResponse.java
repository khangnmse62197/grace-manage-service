package com.grace.gracemanageservice.presentation.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponse<T> {

    private List<T> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean last;
    private boolean first;

    public static <T> PagedResponse<T> of(
            List<T> content,
            int pageNumber,
            int pageSize,
            long totalElements) {

        int totalPages = (int) Math.ceil((double) totalElements / pageSize);
        boolean isLast = pageNumber >= totalPages - 1;
        boolean isFirst = pageNumber == 0;

        return PagedResponse.<T>builder()
            .content(content)
            .pageNumber(pageNumber)
            .pageSize(pageSize)
            .totalElements(totalElements)
            .totalPages(totalPages)
            .last(isLast)
            .first(isFirst)
            .build();
    }
}

