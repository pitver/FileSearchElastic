package ru.vershinin.service.indexCreator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.vershinin.model.SearchDocument;
import ru.vershinin.service.DocumentService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class IndexCreatorService {
    private final DocumentService documentService;
    public static final String ERROR_FILE_PROCESSING = "ошибка при обработке файла {}";


    private void createIndex(String filePath, String content) {
        SearchDocument sd = new SearchDocument();
        File file = new File(filePath);
        var fileName = getFileNameWithoutExtension(file.getName());
        var path = file.getAbsolutePath();
        var extension = getFileExtension(file.getName());
        //sd.setId(UUID.randomUUID().toString());
        sd.setName(fileName);
        sd.setFilePath(path);
        sd.setDocExtension(extension);
        sd.setFileContent(content);
        documentService.createOrUpdateDocument(sd);

    }

    public void prepareContentAndSend(String directoryPath) {

        try (Stream<Path> walk = Files.walk(Paths.get(directoryPath))) {
            List<String> filePaths = walk


                    .filter(Files::isRegularFile)
                    .map(Path::toString)
                    .collect(Collectors.toList());

            for (String filePath : filePaths) {
                StringBuilder content = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.contains("$")) {
                            int delimiterIndex = line.indexOf("$"); // Найти индекс символа '$'
                            if (delimiterIndex != -1) {
                                // Получить левую часть строки
                                String leftPart = line.substring(0, delimiterIndex).trim();
                                // Получить правую часть строки
                                String rightPart = line.substring(delimiterIndex + 1).trim();
                                // Вывести левую и правую части в отдельных сообщениях
                                content.append(leftPart).append("\n");
                                createIndex(filePath, content.toString());
                                //выводит все сообщения кроме последнего
                                content.setLength(0);
                                content.append(rightPart).append("\n");
                                continue;
                            } else {
                                log.info("Символ '$' не найден в строке.");
                            }
                        }
                        content.append(line).append("\n");
                    }
                    createIndex(filePath, content.toString());
                }
            }
        } catch (IOException e) {
            log.error(ERROR_FILE_PROCESSING, e.getMessage());
        }

    }

    public static String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 && dotIndex < fileName.length() - 1 ? fileName.substring(dotIndex + 1) : "";
    }

    private String getFileNameWithoutExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf(".");
        return lastDotIndex != -1 ? fileName.substring(0, lastDotIndex) : fileName;
    }


}
