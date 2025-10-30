package com.dat.wordgame.server;


import java.nio.file.Files;
 import java.nio.file.Path;
 import java.util.List;
import java.util.concurrent.ThreadLocalRandom;


public class WordService {
private static final List<String> EASY = load("words_easy.txt");
private static final List<String> MED = load("words_medium.txt");
private static final List<String> HARD = load("words_hard.txt");
private static final List<String> INSANE = load("words_insane.txt");


public static String pickByRound(int round){
List<String> src = switch(round){
case 1 -> EASY; case 2 -> MED; case 3 -> HARD; default -> INSANE; };
return src.get(ThreadLocalRandom.current().nextInt(src.size()));
}


private static List<String> load(String r){
try { return Files.readAllLines(Path.of("server/src/main/resources/"+r)).stream().map(String::trim)
.filter(s->!s.isBlank()).toList(); } catch(Exception e){ throw new RuntimeException(e); }
}
}