package ru.vershinin.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import ru.vershinin.model.SearchDocument;

import java.util.List;

public interface DocumentRepository extends ElasticsearchRepository<SearchDocument,String> {

    List<SearchDocument> findByName(String name);
    List<SearchDocument> findAll();
    Page<SearchDocument>findAll(Pageable pageable);


}
