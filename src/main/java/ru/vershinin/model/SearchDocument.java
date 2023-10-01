package ru.vershinin.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@ToString
@Document(indexName = "document")
public class SearchDocument {

    @Id
    private String id; // Уникальный идентификатор (необязательный)

    @Field(type = FieldType.Text, name = "name")
    private String name; // Имя файла

    @Field(type = FieldType.Text, name = "docExtension")
    private String docExtension; // Расширение файла

    @Field(type = FieldType.Keyword, name = "filePath")
    private String filePath; // Путь к файлу

    @Field(type = FieldType.Text, name = "fileContent")
    private String fileContent; // Содержание файла
}
