package ru.vershinin.service;


import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import ru.vershinin.dto.SearchResult;
import ru.vershinin.model.SearchDocument;
import ru.vershinin.repository.DocumentRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DocumentService {
    public static final String DATA_FORMAT = "MMyyyy";
    public static final String FILE_CONTENT = "fileContent";

    private final DocumentRepository documentRepository;

    private final ElasticsearchOperations elasticsearchOperations;
    private final ElasticsearchRestTemplate elasticsearchRestTemplate;


    public DocumentService(DocumentRepository documentRepository, ElasticsearchOperations elasticsearchOperations, ElasticsearchRestTemplate elasticsearchRestTemplate) {
        this.documentRepository = documentRepository;
        this.elasticsearchOperations = elasticsearchOperations;
        this.elasticsearchRestTemplate = elasticsearchRestTemplate;
    }


    public void createOrUpdateDocument(SearchDocument document) {
        // Поиск существующего документа по fileContent
        Optional<SearchDocument> existingDocument = findByFileContent(document.getFileContent());

        // Если документ с таким fileContent существует, то обновляем его
        existingDocument.ifPresent(searchDocument -> document.setId(searchDocument.getId()));

        // Сохраняем (обновляем) документ
        elasticsearchRestTemplate.save(document);
    }

    public Optional<SearchDocument> findByFileContent(String fileContent) {
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.matchQuery(FILE_CONTENT, fileContent))
                .build();

        SearchHits<SearchDocument> searchHits = elasticsearchRestTemplate.search(searchQuery, SearchDocument.class);

        if (searchHits.getTotalHits() > 0) {
            // Возвращаем первый найденный документ
            return Optional.of(searchHits.getSearchHit(0).getContent());
        }

        return Optional.empty();
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

    public List<SearchDocument> findByName(String name) {
        return documentRepository.findByName(name);
    }

    public SearchResult searchDocumentsByCriteriaWithStatistics(
            String extension,
            LocalDate startDate,
            LocalDate endDate,
            String word,
            String word1,
            String word2
    ) {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        SearchResult result = new SearchResult();

        if (extension != null) {
            QueryBuilder extensionQuery = QueryBuilders.termQuery("docExtension", extension);
            boolQuery.filter(extensionQuery);
        }

        checkData(startDate, endDate, boolQuery);

        if (word != null && word1 != null && word2 != null) {
            QueryBuilder wordQuery = QueryBuilders.wildcardQuery(FILE_CONTENT, "*" + word + "*");
            QueryBuilder wordQuery1 = QueryBuilders.wildcardQuery(FILE_CONTENT, "*" + word1 + "*");
            QueryBuilder wordQuery2 = QueryBuilders.wildcardQuery(FILE_CONTENT, "*" + word2 + "*");
            boolQuery.should(wordQuery);
            boolQuery.should(wordQuery1);
            boolQuery.should(wordQuery2);
        } else if (word != null && word1 != null) {
            QueryBuilder wordQuery = QueryBuilders.wildcardQuery(FILE_CONTENT, "*" + word + "*");
            QueryBuilder wordQuery1 = QueryBuilders.wildcardQuery(FILE_CONTENT, "*" + word1 + "*");
            boolQuery.should(wordQuery);
            boolQuery.should(wordQuery1);
        } else if (word != null) {
            QueryBuilder wordQuery = QueryBuilders.wildcardQuery(FILE_CONTENT, "*" + word + "*");
            boolQuery.must(wordQuery);
        }

        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(boolQuery)
                .build();

        SearchHits<SearchDocument> searchHits = elasticsearchRestTemplate.search(searchQuery, SearchDocument.class);

        result.setDocuments(searchHits.get().map(SearchHit::getContent).collect(Collectors.toList()));
        result.setTotalDocuments(getTotalIndex(startDate, endDate));
        result.setFoundDocuments(searchHits.getTotalHits());

        return result;
    }

    public Long getTotalIndex(LocalDate startDate,
                              LocalDate endDate) {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        checkData(startDate, endDate, boolQuery);

        // Повторно выполняем запрос, чтобы получить общее количество найденных документов только по дате
        NativeSearchQuery dateQuery = new NativeSearchQueryBuilder()
                .withQuery(boolQuery)
                .build();

        SearchHits<SearchDocument> dateSearchHits = elasticsearchRestTemplate.search(dateQuery, SearchDocument.class);
        return dateSearchHits.getTotalHits();
    }

    private void checkData(LocalDate startDate, LocalDate endDate, BoolQueryBuilder boolQuery) {
        if (startDate != null && endDate != null) {
            String startString = startDate.format(DateTimeFormatter.ofPattern(DATA_FORMAT));
            String endString = endDate.format(DateTimeFormatter.ofPattern(DATA_FORMAT));
            QueryBuilder dateRangeQuery = QueryBuilders.rangeQuery("name")
                    .from(startString)
                    .to(endString)
                    .includeLower(true)
                    .includeUpper(true);
            boolQuery.filter(dateRangeQuery);

        } else if (startDate != null) {
            String startString = startDate.format(DateTimeFormatter.ofPattern(DATA_FORMAT));
            QueryBuilder dateQuery = QueryBuilders.termQuery("name", startString);
            boolQuery.filter(dateQuery);

        }
    }
}


