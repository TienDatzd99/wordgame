package com.dat.wordgame.server;


import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;


public class WordService {
// Load all words into one pool
private static final List<String> ALL_WORDS = loadAllWords();


public static String pickByRound(int round){
    System.out.println("[WordService] pickByRound called with round=" + round + ", ALL_WORDS size=" + ALL_WORDS.size());
    // Round 1: 3-4 letters (easy)
    // Round 2: 5-6 letters (medium)
    // Round 3: 7-8 letters (hard)
    // Round 4+: 9-10 letters (very hard)
    
    int minLen, maxLen;
    switch(round) {
        case 1:
            minLen = 3; maxLen = 4;
            break;
        case 2:
            minLen = 5; maxLen = 6;
            break;
        case 3:
            minLen = 7; maxLen = 8;
            break;
        default: // Round 4+
            minLen = 9; maxLen = 10;
            break;
    }
    
    System.out.println("[WordService] Looking for words with length " + minLen + "-" + maxLen);
    // Filter words by length range
    List<String> filtered = ALL_WORDS.stream()
        .filter(w -> w.length() >= minLen && w.length() <= maxLen)
        .toList();
    
    System.out.println("[WordService] Filtered words count: " + filtered.size());
    if (filtered.isEmpty()) {
        // Fallback to any word if no words in range
        System.out.println("[WordService] No words found in range, using fallback");
        return ALL_WORDS.get(ThreadLocalRandom.current().nextInt(ALL_WORDS.size()));
    }
    
    String word = filtered.get(ThreadLocalRandom.current().nextInt(filtered.size()));
    System.out.println("[WordService] Picked word: " + word);
    return word;
}


private static List<String> loadAllWords(){
    try {
        // Load all word files and combine them
        List<String> easy = loadFile("words_easy.txt");
        System.out.println("[WordService] Loaded easy words: " + easy.size());
        List<String> medium = loadFile("words_medium.txt");
        System.out.println("[WordService] Loaded medium words: " + medium.size());
        List<String> hard = loadFile("words_hard.txt");
        System.out.println("[WordService] Loaded hard words: " + hard.size());
        List<String> insane = loadFile("words_insane.txt");
        System.out.println("[WordService] Loaded insane words: " + insane.size());
        
        List<String> all = Stream.of(easy, medium, hard, insane)
            .flatMap(List::stream)
            .distinct() // Remove duplicates
            .toList();
        System.out.println("[WordService] Total unique words: " + all.size());
        return all;
    } catch(Exception e) {
        throw new RuntimeException("Failed to load word lists", e);
    }
}


private static List<String> loadFile(String filename){
    try {
        var stream = WordService.class.getClassLoader().getResourceAsStream(filename);
        if (stream == null) {
            System.err.println("Warning: Could not load " + filename + " from classpath");
            return List.of();
        }
        try (var reader = new java.io.BufferedReader(new java.io.InputStreamReader(stream))) {
            return reader.lines()
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(String::toUpperCase) // Normalize to uppercase
                .toList();
        }
    } catch(Exception e) {
        System.err.println("Warning: Could not load " + filename + ": " + e.getMessage());
        return List.of(); // Return empty list if file not found
    }
}
}