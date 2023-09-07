package ru.vershinin.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import ru.vershinin.model.SearchDocument;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class IndexCreator {
    private final DocumentService documentService;

    public IndexCreator(DocumentService documentService) {
        this.documentService = documentService;
    }


    public void createIndexesFromDirectory(String directoryPath) {
        List<SearchDocument> indexedFiles = new ArrayList<>(documentService.findAll());
        int addedCount = 0;
        int updatedCount = 0;

        try (Stream<Path> walk = Files.walk(Paths.get(directoryPath))) {
            List<String> filePaths = walk
                    .filter(Files::isRegularFile)
                    .map(Path::toString)
                    .collect(Collectors.toList());

            for (String filePath : filePaths) {
                File file = new File(filePath);
                String fileName = file.getName();
                // String fileContent = getFileContent(file);
                String fileContent = getFileContentT(file);

                SearchDocument searchDocument = indexedFiles.stream()
                        .filter(doc -> doc.getPath().equals(filePath))
                        .findFirst()
                        .orElse(null);

                if (searchDocument == null) {
                    // Файл отсутствует в индексе Elasticsearch, создаем новый документ
                    searchDocument = new SearchDocument();
                    searchDocument.setName(fileName);
                    searchDocument.setPath(filePath);
                    searchDocument.setFileContent(fileContent);

                    var response = documentService.createOrUpdateDocument(searchDocument);
                    // log.info("File {} has been added to index.", response.getPath());
                    addedCount++;
                } else {
                    // Файл присутствует в индексе Elasticsearch, обновляем контент файла
                    searchDocument.setFileContent(fileContent);

                    var response = documentService.createOrUpdateDocument(searchDocument);
                    //  log.info("File {} has been updated in index.", response.getPath());
                    updatedCount++;
                }
            }

            // Удаление файлов, отсутствующих в директории, из индекса Elasticsearch
            indexedFiles.removeIf(file -> filePaths.contains(file.getPath()));

            documentService.deleteAll(indexedFiles);
           /* for (SearchDocument document : indexedFiles) {
                try {
                    documentService.deleteByPath(document.getPath());
                    log.info("File {} has been removed from index.", document.getPath());
                } catch (Exception e) {
                    log.error("Error while deleting document index: {}", document.getPath());
                }
            }*/
        } catch (IOException e) {
            log.error(e.getMessage());
        }

        log.info("Total added indexes: {}", addedCount);
        log.info("Total updated indexes: {}", updatedCount);
    }


    private String getFileContent(File file) {
        try (PDDocument document = PDDocument.load(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);

        } catch (IOException e) {
            log.error("Error while reading file content: {}", file.getAbsolutePath());
            return "";
        }
    }

    private String getFileContentT(File file) throws IOException {
        try (var lines = Files.lines(Paths.get(file.getPath()))) {
            return lines.collect(Collectors.joining("\n"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "content";

    }
}