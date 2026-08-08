package org.example.task_tracker.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResponseDTO<T> {

    private List<T> content;        // данные
    private int currentPage;        // текущая страница
    private int totalPages;         // всего страниц
    private long totalElements;     // всего записей
    private boolean last;           // последняя страница?

    public PageResponseDTO(Page<T> page) {
        this.content = page.getContent();
        this.currentPage = page.getNumber();
        this.totalPages = page.getTotalPages();
        this.totalElements = page.getTotalElements();
        this.last = page.isLast();
    }

}
