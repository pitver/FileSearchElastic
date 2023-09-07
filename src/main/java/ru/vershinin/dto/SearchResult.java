package ru.vershinin.dto;

import ru.vershinin.model.SearchDocument;

import java.util.List;

public class SearchResult {
    private List<SearchDocument> searchResults;
    private int totalFilesProcessed;
    private int totalOccurrencesFound;

    // Геттеры и сеттеры для полей

    public List<SearchDocument> getSearchResults() {
        return searchResults;
    }

    public void setSearchResults(List<SearchDocument> searchResults) {
        this.searchResults = searchResults;
    }

    public int getTotalFilesProcessed() {
        return totalFilesProcessed;
    }

    public void setTotalFilesProcessed(int totalFilesProcessed) {
        this.totalFilesProcessed = totalFilesProcessed;
    }

    public int getTotalOccurrencesFound() {
        return totalOccurrencesFound;
    }

    public void setTotalOccurrencesFound(int totalOccurrencesFound) {
        this.totalOccurrencesFound = totalOccurrencesFound;
    }
}
