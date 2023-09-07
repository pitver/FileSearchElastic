package ru.vershinin.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.vershinin.model.SearchDocument;
import ru.vershinin.service.DocumentService;
import ru.vershinin.service.IndexCreator;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ElasticSearchController {


    private final DocumentService elasticSearchQuery;
    private final IndexCreator indexCreator;

    @GetMapping("/findByNameSingleHit")
    public ResponseEntity<Object> findByNameSingleHit(@RequestParam String name) {
        var documents = elasticSearchQuery.findByNameSingleHit(name);
        return new ResponseEntity<>(documents, HttpStatus.OK);
    }

    @GetMapping("/searchByNameTotalHits")
    public ResponseEntity<Object> searchByNameTotalHits(@RequestParam String name) {
      //  var documents = elasticSearchQuery.searchByPartOfNameTotalHits(name);
        var documents = elasticSearchQuery.searchByPartOfNameWithStats(name);
        return new ResponseEntity<>(documents, HttpStatus.OK);
    }

    @GetMapping("/scanDirectory")
    public ResponseEntity<Object> scanDirectory(@RequestParam String dir) {
        indexCreator.createIndexesFromDirectory(dir);
        return new ResponseEntity<>("ok", HttpStatus.OK);
    }

    @GetMapping("/searchDocument")
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
}