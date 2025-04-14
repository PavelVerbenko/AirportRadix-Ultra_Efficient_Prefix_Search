// SearchResult.java
package com.example;

import java.util.List;
//DTO для хранения результатов поиска.
public class SearchResult {
    // эт строка для запроса
    private final String search;
    //список найденных номеров строк
    private final List<Integer> result;
    //время выполнения поиска
    private final long time;

    public SearchResult(String search, List<Integer> result, long time) {
        this.search = search;
        this.result = result;
        this.time = time;
    }

    public String getSearch() { return search; }
    public List<Integer> getResult() { return result; }
    public long getTime() { return time; }
}