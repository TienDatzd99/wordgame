package com.dat.wordgame.server;


import java.util.*; import com.dat.wordgame.server.Persistence.PlayerRow;


public class RankingService {
public static List<PlayerRow> topPlayers(int k){ return Persistence.topPlayers(k); }
}