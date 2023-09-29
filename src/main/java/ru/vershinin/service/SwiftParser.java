package ru.vershinin.service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SwiftParser {
    public static void main(String[] args) {
        String fileName = "src/main/resources/1.out"; // Замените на путь к вашему файлу .ask

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            StringBuilder currentMessage = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.contains("$")) {
                    // Найден разделитель "$", выводим текущее сообщение
                    if (currentMessage.length() > 0) {
                        System.out.println("Swift Message:");
                        System.out.println(currentMessage.toString().trim());
                        System.out.println("----------------------------------------");
                        currentMessage.setLength(0);
                    }
                } else {
                    currentMessage.append(line).append("\n");
                }
            }

            // Выводим последнее сообщение (если есть)
            if (currentMessage.length() > 0) {
                System.out.println("Swift Message:");
                System.out.println(currentMessage.toString().trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

