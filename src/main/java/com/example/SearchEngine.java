package com.example;

import java.util.BitSet;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
//Выполняет префиксный поиск и сортировку результатов.
public class SearchEngine {
//запускает поиск для всех входных строк.
    public List<SearchResult> performSearch(List<String> searchTerms, Indexer.IndexedData indexedData) {
        List<SearchResult> results = new ArrayList<>();
        for (String term : searchTerms) {
            long start = System.currentTimeMillis();
            List<Integer> lines = search(term, indexedData.getRoot());
            sortResults(lines, indexedData.isNumeric(), indexedData);
            results.add(new SearchResult(term, lines, System.currentTimeMillis() - start));
        }
        return results;
    }

    public List<Integer> search(String prefix, RadixNode root) {
        RadixNode node = findPrefixNode(root, prefix.toLowerCase());
        return node != null ? convertBitSetToList(node.getLines()) : Collections.emptyList();
    }

    private List<Integer> convertBitSetToList(BitSet bitSet) {
        List<Integer> list = new ArrayList<>();
        for (int i = bitSet.nextSetBit(0); i >= 0; i = bitSet.nextSetBit(i + 1)) {
            list.add(i);
        }
        return list;
    }
//находит узел Radix Tree, соответствующий префиксу.
    private RadixNode findPrefixNode(RadixNode root, String prefix) {
        RadixNode current = root;
        for (char c : prefix.toCharArray()) {
            if (c >= 128) return null;
            current = current.getOrCreateChild(c);
            if (current == null) return null;
        }
        return current;
    }
//сортирует результаты по типу данных (число/строка).
    public void sortResults(List<Integer> lines, boolean isNumeric, Indexer.IndexedData indexedData) {
        if (lines == null || lines.isEmpty()) return;

        Comparator<Integer> comparator = isNumeric
                ? Comparator.comparingDouble(line -> {
            try {
                return Double.parseDouble(indexedData.getValueByLine(line));
            } catch (IOException e) {
                throw new RuntimeException("Ошибка чтения значения для строки " + line, e);
            }
        })
                : Comparator.comparing(line -> {
            try {
                return indexedData.getValueByLine(line).toLowerCase();
            } catch (IOException e) {
                throw new RuntimeException("Ошибка чтения значения для строки " + line, e);
            }
        });

        lines.sort(comparator);
    }
}