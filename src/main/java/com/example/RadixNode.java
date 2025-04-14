package com.example;

import java.util.BitSet;
//Узел Radix Tree.
//Хранит дочерние узлы в массиве RadixNode[128] (только для ASCII-символов).
//Номера строк сохраняются в BitSet для минимизации использования памяти.
public class RadixNode {
    private final RadixNode[] children = new RadixNode[128];
    private final BitSet lines = new BitSet();
    //добавляет номер строки в BitSet.
    public void addLine(int line) {
        lines.set(line);
    }

    public BitSet getLines() {
        return lines;
    }
    //возвращает дочерний узел или создает новый.
    public RadixNode getOrCreateChild(char c) {
        if (c >= 128) return null; // Игнорируем не-ASCII
        if (children[c] == null) {
            children[c] = new RadixNode();
        }
        return children[c];
    }
}