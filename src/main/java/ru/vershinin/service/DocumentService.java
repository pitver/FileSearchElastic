package ru.vershinin.service;

/*import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Hit;*/

import lombok.extern.slf4j.Slf4j;

import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import ru.vershinin.dto.SearchResult;
import ru.vershinin.model.SearchDocument;
import ru.vershinin.repository.DocumentRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.elasticsearch.index.query.QueryBuilders.matchPhraseQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;

@Slf4j
@Service
public class DocumentService {


    private final DocumentRepository documentRepository;
    private final RestHighLevelClient client;
    private final ElasticsearchOperations elasticsearchOperations;

    public DocumentService(DocumentRepository documentRepository, RestHighLevelClient client, ElasticsearchOperations elasticsearchOperations) {
        this.client = client;
        this.documentRepository = documentRepository;
        this.elasticsearchOperations = elasticsearchOperations;
    }

    public SearchDocument createOrUpdateDocument(SearchDocument searchDocument) {
        return documentRepository.save(searchDocument);
    }

    public List<SearchDocument> findAll() {
        return documentRepository.findAll();
    }

    public Page<SearchDocument> findAllPage(Pageable pageable) {
        return documentRepository.findAll(pageable);
    }

    public List<String> getAllIndex() {
        final NativeSearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(QueryBuilders.matchAllQuery())
                .build();
        var hits = elasticsearchOperations.search(searchQuery, SearchDocument.class);
        return hits.stream()
                .map(SearchHit::getId)
                .collect(Collectors.toList());

    }

    public SearchDocument findByNameSingleHit(String name) {
        final NativeSearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(matchQuery("name", name).operator(Operator.AND))
                .build();
        var b = elasticsearchOperations.search(searchQuery, SearchDocument.class);
        SearchDocument searchDocument = new SearchDocument();
        for (SearchHit<SearchDocument> v : b) {
            searchDocument = v.getContent();
        }

        return searchDocument;
    }

    public SearchDocument findByName(String name) {
        return documentRepository.findByName(name);
    }

    public List<SearchDocument> searchByNameTotalHits(String name) {
        final NativeSearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(matchPhraseQuery("name", name))
                .build();
        var hits = elasticsearchOperations.search(searchQuery, SearchDocument.class);


        return hits.stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
    }

    public List<SearchDocument> searchByPartOfNameTotalHits(String name) {
        // Create a WildcardQueryBuilder for partial matching
        QueryBuilder wildcardQuery = QueryBuilders.wildcardQuery("fileContent", "*" + name + "*");

        // Create a BoolQueryBuilder to combine the wildcard query with other queries if needed
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery().must(wildcardQuery);

        final NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(boolQuery)
                .build();

        var hits = elasticsearchOperations.search(searchQuery, SearchDocument.class);

        return hits.stream()
                .filter(hit -> hit.getContent().getFileContent() != null) // Фильтруем документы без контента
                .map(hit -> {
                    SearchDocument document = hit.getContent();
                    return getSearchDocument(name, document);
                })
                .filter(Objects::nonNull) // Фильтруем null значения
                .collect(Collectors.toList());
    }

    private SearchDocument getSearchDocument(String name, SearchDocument document) {
        String content = document.getFileContent();
        int index = content.indexOf(name); // Находим индекс начала искомого слова

        if (index != -1) { // Проверяем, что индекс найден
            // Обрезаем контент на 100 символов до и после искомого слова
            int start = Math.max(0, Math.min(index - 100, content.length()));
            int end = Math.min(content.length(), index + name.length() + 100);
            String trimmedContent = content.substring(start, end);

            // Создаем новый объект SearchDocument с обрезанным контентом
            SearchDocument trimmedDocument = new SearchDocument();
            trimmedDocument.setName(document.getName());
            trimmedDocument.setPath(document.getPath());
            trimmedDocument.setFileContent(trimmedContent);

            return trimmedDocument;
        } else {
            return null;
        }
    }

    public SearchResult searchByPartOfNameWithStats(String name) {
        int totalFilesProcessed = 0;
        int totalOccurrencesFound = 0;

        // Создаем пустой список для результатов поиска
        List<SearchDocument> searchResults = new ArrayList<>();

        // Create a WildcardQueryBuilder for partial matching
        QueryBuilder wildcardQuery = QueryBuilders.wildcardQuery("fileContent", "*" + name + "*");

        // Create a BoolQueryBuilder to combine the wildcard query with other queries if needed
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery().must(wildcardQuery);

        final NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(boolQuery)
                .build();

        var hits = elasticsearchOperations.search(searchQuery, SearchDocument.class);

        for (SearchHit<SearchDocument> hit : hits) {
            totalFilesProcessed++;

            SearchDocument document = hit.getContent();
            String content = document.getFileContent();

            int index = content.indexOf(name); // Находим индекс начала искомого слова

            if (index != -1) { // Проверяем, что индекс найден
                totalOccurrencesFound++;

                // Обрезаем контент на 100 символов до и после искомого слова
                int start = Math.max(0, Math.min(index - 100, content.length()));
                int end = Math.min(content.length(), index + name.length() + 100);
                String trimmedContent = content.substring(start, end);

                // Создаем новый объект SearchDocument с обрезанным контентом
                SearchDocument trimmedDocument = new SearchDocument();
                trimmedDocument.setName(document.getName());
                trimmedDocument.setPath(document.getPath());
                trimmedDocument.setFileContent(trimmedContent);

                searchResults.add(trimmedDocument);
            }
        }

        // Создаем объект SearchResult, содержащий результаты поиска и статистику
        SearchResult result = new SearchResult();
        result.setSearchResults(searchResults);
        result.setTotalFilesProcessed(totalFilesProcessed);
        result.setTotalOccurrencesFound(totalOccurrencesFound);

        return result;
    }


    public void deleteByPath(String path) {
      //  documentRepository.deleteByPath(path);
        var query=new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.termQuery("path",path))
                .build();

        elasticsearchOperations.delete(query,SearchDocument.class);
        log.info("Document with path {} has been deleted.", path);

    }

    public void deleteAll(List<SearchDocument> list) {
        List<SearchDocument> indexedFiles = new ArrayList<>(list);
        indexedFiles.stream()
                .map(SearchDocument::getPath)
                .forEach(path -> {
                    documentRepository.deleteByPath(path);
                    log.info("Document with path {} has been deleted.", path);
                });
    }
}
