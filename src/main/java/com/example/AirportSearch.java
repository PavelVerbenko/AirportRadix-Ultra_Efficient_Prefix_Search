package com.example;

import com.google.gson.GsonBuilder;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
//Главный класс приложения. Обрабатывает аргументы командной строки, управляет процессом индексации и поиска.
@Command(name = "airport-search", mixinStandardHelpOptions = true, version = "1.0")
public class AirportSearch implements Callable<Integer> {
    @Option(names = "--data", required = true) private String dataFilePath;
    @Option(names = "--indexed-column-id", required = true) private int indexedColumnId;
    @Option(names = "--input-file", required = true) private String inputFilePath;
    @Option(names = "--output-file", required = true) private String outputFilePath;

    // Вложенный статический класс с публичными полями
    static class OutputData {
        public final long initTime;
        public final List<SearchResult> result;

        OutputData(long initTime, List<SearchResult> result) {
            this.initTime = initTime;
            this.result = result;
        }
    }

    public static void main(String[] args) {
        new CommandLine(new AirportSearch()).execute(args);
    }

    //точка входа: запускает индексацию, чтение входных данных, поиск и сохранение результатов.
    @Override
    public Integer call() throws Exception {
        long startTime = System.currentTimeMillis();

        Indexer indexer = new Indexer();
        Indexer.IndexedData indexedData = indexer.createIndex(dataFilePath, indexedColumnId);
        long initTime = System.currentTimeMillis() - startTime;

        List<String> searchTerms = Files.readAllLines(Paths.get(inputFilePath))
                .stream()
                .filter(line -> !line.trim().isEmpty())
                .collect(Collectors.toList());

        SearchEngine searchEngine = new SearchEngine();
        List<SearchResult> results = searchEngine.performSearch(searchTerms, indexedData);

        saveResults(outputFilePath, initTime, results);
        return 0;
    }
//сериализует результаты в JSON-файл.
    private void saveResults(String outputPath, long initTime, List<SearchResult> results) throws IOException {
        OutputData output = new OutputData(initTime, results);
        String json = new GsonBuilder().setPrettyPrinting().create().toJson(output);
        Files.writeString(Paths.get(outputPath), json);
    }
}