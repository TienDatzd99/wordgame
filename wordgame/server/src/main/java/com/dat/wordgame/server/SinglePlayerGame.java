package com.dat.wordgame.server;

import com.dat.wordgame.common.*;
import com.dat.wordgame.common.Models.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

public class SinglePlayerGame {
    private final String id = UUID.randomUUID().toString();
    private final String player;
    private final LobbyManager lobby;
    private volatile int round = 1;
    private volatile String currentWord;
    private volatile long roundEndAt;
    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private boolean playerCorrect = false;
    private int totalScore = 0;
    
    public SinglePlayerGame(String player, LobbyManager lobby) {
        this.player = player;
        this.lobby = lobby;
    }
    
    public String id() { return id; }
    public String player() { return player; }
    
    public void start() {
        System.out.println("[SinglePlayerGame] Starting solo game for " + player);
        lobby.sendTo(player, Message.of(MessageType.ROOM_JOINED, 
            new RoomState(id, player, "Solo Mode", round, "ready")));
        startRound();
    }
    
    private void startRound() {
        System.out.println("[SinglePlayerGame] Starting round " + round + " for " + player);
        playerCorrect = false;
        currentWord = WordService.pickByRound(round);
        System.out.println("[SinglePlayerGame] Word: " + currentWord);
        
        int total = Payloads.timeLimitByLength(currentWord.length());
        roundEndAt = System.currentTimeMillis() + total * 1000L;
        
        var letters = currentWord.chars().mapToObj(c -> (char) c).collect(Collectors.toList());
        Collections.shuffle(letters);
        
        var rs = new RoundStart(id, round, currentWord, letters, total);
        lobby.sendTo(player, Message.of(MessageType.ROUND_START, rs));
        
        scheduler.scheduleAtFixedRate(() -> tick(total), 1, 1, TimeUnit.SECONDS);
    }
    
    private void tick(int total) {
        int remain = (int) Math.max(0, (roundEndAt - System.currentTimeMillis()) / 1000L);
        lobby.sendTo(player, Message.of(MessageType.ROUND_TICK, new RoundTick(id, remain)));
        
        if (remain <= 0) {
            endRound(playerCorrect ? player : null, total, 0);
        }
    }
    
    public void onGuess(String guess) {
        int correct = 0;
        for (int i = 0; i < Math.min(guess.length(), currentWord.length()); i++) {
            if (guess.charAt(i) == currentWord.charAt(i)) correct++;
        }
        
        lobby.sendTo(player, Message.of(MessageType.GUESS_UPDATE, 
            new GuessUpdate(id, player, correct)));
        
        boolean isCorrect = guess.equalsIgnoreCase(currentWord);
        if (isCorrect && !playerCorrect) {
            playerCorrect = true;
            System.out.println("[SinglePlayerGame] Player answered correctly!");
            
            // In solo mode, end round immediately when correct
            int remain = (int) Math.max(0, (roundEndAt - System.currentTimeMillis()) / 1000L);
            endRound(player, Payloads.timeLimitByLength(currentWord.length()), remain);
        }
    }
    
    private void endRound(String winner, int total, int remain) {
        scheduler.shutdownNow();
        scheduler = Executors.newSingleThreadScheduledExecutor();
        
        boolean win = winner != null;
        int base = Payloads.baseScore(win);
        double bonus = (remain / (double) total) * 3.0;
        int award = base + (int) Math.round(bonus);
        
        if (win) {
            totalScore += award;
            Persistence.addPoints(winner, award);
        }
        
        lobby.sendTo(player, Message.of(MessageType.ROUND_END, 
            new RoundEnd(id, winner, currentWord, base, bonus, award)));
        
        if (round >= 4) {
            // Game over
            var summary = new Summary(player, totalScore, 0, 0, 0);
            lobby.sendTo(player, Message.of(MessageType.GAME_END, 
                new GameEnd(id, winner != null ? winner : "Game Over", summary, null)));
            
            // Broadcast snapshot to update leaderboard
            lobby.snapshot();
            System.out.println("[SinglePlayerGame] Solo game ended, score: " + totalScore);
        } else {
            round++;
            startRound();
        }
    }
    
    public void onDisconnect() {
        scheduler.shutdownNow();
    }
}
