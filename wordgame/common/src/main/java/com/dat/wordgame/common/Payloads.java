package com.dat.wordgame.common;


public class Payloads {
public static int timeLimitByLength(int len){
if(len<=4) return 10; if(len<=6) return 13; if(len<=8) return 16; return 19;
}
public static int baseScore(boolean win){ return win?3:0; }
public static int totalScore(boolean win, int remain, int total){
int base = baseScore(win);
double bonus = (remain/(double)total)*3.0; // per spec
return base + (int)Math.round(bonus);
}
}