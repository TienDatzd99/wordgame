package com.dat.wordgame.common;


public class Payloads {
public static int timeLimitByLength(int len){
    // Time limit based on word length:
    // 3-4 letters: 15 seconds (easy)
    // 5-6 letters: 20 seconds (medium)
    // 7-8 letters: 25 seconds (hard)
    // 9-10 letters: 30 seconds (very hard)
    if(len <= 4) return 15;
    if(len <= 6) return 20;
    if(len <= 8) return 25;
    return 30;
}
public static int baseScore(boolean win){ return win?3:0; } // Legacy method for compatibility

public static int baseScoreByRound(int round) {
    // New scoring system based on difficulty (round)
    switch(round) {
        case 1: return 50;   // Easy
        case 2: return 100;  // Medium  
        case 3: return 150;  // Hard
        case 4: return 200;  // Insane
        default: return 50;  // Default to easy
    }
}

public static int totalScore(boolean win, int remain, int total){
int base = baseScore(win);
double bonus = (remain/(double)total)*3.0; // per spec
return base + (int)Math.round(bonus);
}

public static int calculateScore(boolean win, int completionTimeMs, int totalTimeMs, int round) {
    if (!win) return 0;
    
    int baseScore = baseScoreByRound(round);
    
    // Calculate bonus based on completion time (faster = higher bonus)
    // Formula: (Time left / Total time) × Base score
    int timeLeftMs = totalTimeMs - completionTimeMs;
    double timeRatio = Math.max(0, timeLeftMs) / (double)totalTimeMs;
    int bonus = (int)Math.round(timeRatio * baseScore);
    
    return baseScore + bonus;
}
}