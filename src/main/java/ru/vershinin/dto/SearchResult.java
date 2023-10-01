package ru.vershinin.dto;

import lombok.Data;
import ru.vershinin.model.SearchDocument;

import java.util.List;


@Data
public class SearchResult {
    private List<SearchDocument> documents;
    private long totalDocuments; // Общее количество просмотренных документов
    private long foundDocuments;
}
