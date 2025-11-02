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
public static int baseScore(boolean win){ return win?3:0; }
public static int totalScore(boolean win, int remain, int total){
int base = baseScore(win);
double bonus = (remain/(double)total)*3.0; // per spec
return base + (int)Math.round(bonus);
}
}