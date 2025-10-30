package com.dat.wordgame.common;
import java.util.*;


public class Models {
// Auth & profile
public record LoginReq(String username, String password) {}
public record LoginOk(String username, int totalPoints) {}


// Lobby
public record PlayerBrief(String name, int points, String status) {}
public record LobbySnapshot(List<PlayerBrief> online, List<PlayerBrief> leaderboard, List<RoomBrief> rooms) {}
public record RoomBrief(String roomId, String host, String opponent, String state) {}


// Room/game
public record RoomState(String roomId, String host, String opponent, int round, String status) {}
public record RoundStart(String roomId, int round, String maskedWord, List<Character> shuffledLetters, int totalTimeSec) {}
public record RoundTick(String roomId, int remainSec) {}
public record RoundEnd(String roomId, String winner, String correctWord, int basePoints, double bonus, int totalAward) {}
public record GameEnd(String roomId, String winner, Summary s1, Summary s2) {}
public record Summary(String player, int roundsWon, double avgTime, int bonusTotal, int pointsGained) {}


// Gameplay
public record GuessSubmit(String roomId, String guess) {}
public record GuessUpdate(String roomId, String player, int correctSlots) {}


// Chat
public record Chat(String roomId, String from, String text) {}


// Error
public record Err(String code, String message) {}
}