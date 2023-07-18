package ru.vershinin.model;

import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@ToString
@Document(indexName = "document")
public class SearchDocument {


    @Field(type = FieldType.Text, name = "name")
    private String name;

    @Id
    @Field(type = FieldType.Keyword, name = "path")
    private String path;

    @Field(type = FieldType.Text, name = "fileContent")
    private String fileContent;
}
