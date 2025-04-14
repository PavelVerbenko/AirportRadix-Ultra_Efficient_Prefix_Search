package com.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

//Создает индекс (Radix Tree) для указанной колонки CSV-файла.
public class Indexer {
    private static final int MAX_PREFIX_LENGTH = 3;
    //читает CSV, строит Radix Tree, определяет тип данных колонки.
    public IndexedData createIndex(String filePath, int columnId) throws IOException {
        RadixNode root = new RadixNode();
        boolean isNumeric = true;

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(filePath))) {
            String line;
            int lineNum = 0;

            while ((line = reader.readLine()) != null) {
                if (lineNum % 1000 == 0) System.gc(); // Принудительный сборщик мусора
                if (line.trim().isEmpty()) continue;

                lineNum++;
                String[] cols = parseCsvLine(line);
                if (cols.length < columnId) continue;

                String value = cols[columnId - 1].replace("\"", "").trim().toLowerCase();
                if (value.isEmpty()) continue;

                if (isNumeric && !isNumeric(value)) isNumeric = false;
                insertToRadixTree(root, value, lineNum);
            }
        }
        return new IndexedData(root, isNumeric, filePath, columnId);
    }
    //добавляет значение в Radix Tree.
    private void insertToRadixTree(RadixNode root, String value, int lineNum) {
        RadixNode current = root;
        int limit = Math.min(value.length(), MAX_PREFIX_LENGTH);

        for (int i = 0; i < limit; i++) {
            char c = value.charAt(i);
            current = current.getOrCreateChild(c);
            if (current == null) break;
        }
        if (current != null) current.addLine(lineNum);
    }
    //парсит строку CSV с учетом экранированных кавычек.
    private static String[] parseCsvLine(String line) {
        List<String> cols = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder current = new StringBuilder();

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                // Обработка двойных кавычек
                if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                cols.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        cols.add(current.toString().trim());
        return cols.toArray(new String[0]);
    }

    private boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) return false;
        return str.matches("^[-+]?\\d+(\\.\\d+)?([eE][-+]?\\d+)?$");
    }

    public static class IndexedData {
        private final RadixNode root;
        private final boolean isNumeric;
        private final String filePath;
        private final int columnId;

        public IndexedData(RadixNode root, boolean isNumeric, String filePath, int columnId) {
            this.root = root;
            this.isNumeric = isNumeric;
            this.filePath = filePath;
            this.columnId = columnId;
        }

        public RadixNode getRoot() { return root; }
        public boolean isNumeric() { return isNumeric; }

        public String getValueByLine(int lineNum) throws IOException {
            try (BufferedReader reader = Files.newBufferedReader(Paths.get(filePath))) {
                String line = reader.lines().skip(lineNum - 1).findFirst().orElseThrow();
                String[] cols = parseCsvLine(line);
                return cols[columnId - 1].replace("\"", "").trim();
            }
        }
    }
}