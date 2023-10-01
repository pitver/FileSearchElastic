package ru.vershinin.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.vershinin.model.SearchDocument;
import ru.vershinin.service.DocumentService;
import ru.vershinin.service.indexCreator.IndexCreatorService;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
public class ElasticSearchController {


    private final DocumentService elasticSearchQuery;
    private final IndexCreatorService indexCreator;


    @GetMapping("/scanDirectory")
    public ResponseEntity<Object> scanDirectory(@RequestParam String dir) {
        indexCreator.prepareContentAndSend(dir);
        return new ResponseEntity<>("ok", HttpStatus.OK);
    }

    @GetMapping("/searchAllDocument")
    public ResponseEntity<Page<SearchDocument>> searchAllDocument(Pageable pageable) {
        var documents = elasticSearchQuery.findAllPage(pageable);
        return new ResponseEntity<>(documents, HttpStatus.OK);
    }

    @GetMapping("/getAllIndex")
    public ResponseEntity<Object> getAllIndex() {
        var documents = elasticSearchQuery.getAllIndex();
        return new ResponseEntity<>(documents, HttpStatus.OK);
    }

    @GetMapping("/findByName")
    public ResponseEntity<Object> findByName(@RequestParam String name) {
        var documents = elasticSearchQuery.findByName(name);
        return new ResponseEntity<>(documents, HttpStatus.OK);
    }

    @GetMapping("/fiter")
    public ResponseEntity<Object> findByName(@RequestParam(required = false) String extension,
                                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                             @RequestParam(required = false) String word,
                                             @RequestParam(required = false) String word1,
                                             @RequestParam(required = false) String word2) {
        var documents = elasticSearchQuery.searchDocumentsByCriteriaWithStatistics(extension, startDate, endDate, word,word1,word2);
        return new ResponseEntity<>(documents, HttpStatus.OK);
    }
}