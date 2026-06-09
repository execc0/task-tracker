package org.example.task_tracker.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PageResponseDTO<T> {

    private List<T> content;        // данные
    private int currentPage;        // текущая страница
    private int totalPages;         // всего страниц
    private long totalElements;     // всего записей
    private boolean last;           // последняя страница?

}
