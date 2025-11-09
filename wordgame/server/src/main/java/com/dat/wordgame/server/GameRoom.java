package com.dat.wordgame.server;


import java.util.Collections;
 import java.util.HashMap;
import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.dat.wordgame.common.Message;
import com.dat.wordgame.common.MessageType;
import com.dat.wordgame.common.Models;
import com.dat.wordgame.common.Models.GameEnd;
import com.dat.wordgame.common.Models.GuessUpdate;
import com.dat.wordgame.common.Models.RoomState;
import com.dat.wordgame.common.Models.RoundEnd;
import com.dat.wordgame.common.Models.RoundStart;
import com.dat.wordgame.common.Models.RoundTick;
import com.dat.wordgame.common.Models.Summary;
import com.dat.wordgame.common.Payloads;


public class GameRoom {
private final String id = UUID.randomUUID().toString();
private final String host; private String opponent; private final LobbyManager lobby;
private volatile int round = 1; private volatile String currentWord; private volatile long roundEndAt;
private volatile long roundStartAt; // Track when round started
private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
private Set<String> playersCorrect = new HashSet<>(); // Track who got it correct
private String firstCorrect = null; // Who answered correctly first
private long firstCorrectTime = 0; // Time when first correct answer
private Map<String, Long> completionTimes = new HashMap<>(); // Track each player's completion time
private Map<String, Integer> gameScores = new HashMap<>(); // Track total game scores for each player
private boolean gameEnded = false; // Flag to prevent saving match result twice


public GameRoom(String host, String opponent, LobbyManager lobby){ this.host=host; this.opponent=opponent; this.lobby=lobby; }
public GameRoom(String host, LobbyManager lobby){ this.host=host; this.opponent=null; this.lobby=lobby; }
public void addOpponent(String opponent) { this.opponent = opponent; }
public String id(){ return id; } public String host(){ return host; } public String opponent(){ return opponent; }


public Models.RoomBrief brief(){ return new Models.RoomBrief(id, host, opponent, "playing"); }


public void notifyJoin(){
System.out.println("[GameRoom] notifyJoin() called for room " + id);
System.out.println("[GameRoom] Host: " + host + ", Opponent: " + opponent);
lobby.sendTo(host, Message.of(MessageType.ROOM_JOINED, new RoomState(id, host, opponent, round, "ready")));
lobby.sendTo(opponent, Message.of(MessageType.ROOM_JOINED, new RoomState(id, host, opponent, round, "ready")));
System.out.println("[GameRoom] ROOM_JOINED sent to both players, waiting 1 second before starting round...");

// Đợi 1 giây để cả hai client kịp chuyển màn hình trước khi bắt đầu round
scheduler.schedule(() -> {
    System.out.println("[GameRoom] Starting round after delay...");
    startRound();
}, 1, TimeUnit.SECONDS);
}


private void startRound(){
System.out.println("[GameRoom] startRound() called for round " + round);
playersCorrect.clear(); // Reset for new round
completionTimes.clear(); // Reset completion times
firstCorrect = null;
firstCorrectTime = 0;
roundStartAt = System.currentTimeMillis(); // Track round start time
currentWord = WordService.pickByRound(round);
System.out.println("[GameRoom] Word picked: " + currentWord + ", length: " + currentWord.length());
int total = Payloads.timeLimitByLength(currentWord.length());
roundEndAt = System.currentTimeMillis() + total*1000L;
var letters = currentWord.chars().mapToObj(c->(char)c).collect(Collectors.toList());
Collections.shuffle(letters);
// Show the actual word instead of masked word
var rs = new RoundStart(id, round, currentWord, letters, total);
System.out.println("[GameRoom] Sending ROUND_START, time: " + total);
lobby.sendToBoth(this, Message.of(MessageType.ROUND_START, rs));
System.out.println("[GameRoom] ROUND_START sent!");


scheduler.scheduleAtFixedRate(() -> tick(total), 1, 1, TimeUnit.SECONDS);
}


private void tick(int total){
int remain = (int)Math.max(0, (roundEndAt - System.currentTimeMillis())/1000L);
lobby.sendToBoth(this, Message.of(MessageType.ROUND_TICK, new RoundTick(id, remain)));
if(remain<=0){ 
    // Time's up - use new scoring system
    endRoundWithNewScoring();
}
}


public void onGuess(String from, String guess){
int correct = 0; for(int i=0;i<Math.min(guess.length(), currentWord.length());i++) if(guess.charAt(i)==currentWord.charAt(i)) correct++;
lobby.sendToBoth(this, Message.of(MessageType.GUESS_UPDATE, new GuessUpdate(id, from, correct)));

// Check if guess is correct
boolean isCorrect = guess.equalsIgnoreCase(currentWord);

if (isCorrect && !playersCorrect.contains(from)) {
    // Record completion time for this player
    long completionTime = System.currentTimeMillis();
    completionTimes.put(from, completionTime - roundStartAt); // Store time taken in ms
    
    // First time this player got it correct
    playersCorrect.add(from);
    
    // Track who answered first
    if (firstCorrect == null) {
        firstCorrect = from;
        firstCorrectTime = completionTime;
        System.out.println("[GameRoom] " + from + " answered correctly FIRST in " + (completionTime - roundStartAt) + "ms!");
    } else {
        System.out.println("[GameRoom] " + from + " also answered correctly in " + (completionTime - roundStartAt) + "ms!");
    }
    
    // End round if BOTH players got it correct
    if (playersCorrect.size() >= 2) {
        System.out.println("[GameRoom] Both players correct! Ending round. Winner: " + firstCorrect);
        endRoundWithNewScoring();
    }
}
// If incorrect, just send GUESS_UPDATE (already sent above), client will show red border
}

    private void endRoundWithNewScoring() {
        scheduler.shutdownNow(); scheduler = Executors.newSingleThreadScheduledExecutor();
        
        String winner = firstCorrect;
        String correct = currentWord;
        int totalTimeMs = Payloads.timeLimitByLength(currentWord.length()) * 1000;
        
        if (winner != null) {
            // Calculate score for winner using new system
            long completionTimeMs = completionTimes.get(winner);
            int award = Payloads.calculateScore(true, (int)completionTimeMs, totalTimeMs, round);
            
            // Update game score tracking
            gameScores.put(winner, gameScores.getOrDefault(winner, 0) + award);
            
            // For display purposes, calculate base and bonus
            int base = Payloads.baseScoreByRound(round);
            double timeRatio = Math.max(0, totalTimeMs - completionTimeMs) / (double)totalTimeMs;
            double bonus = timeRatio * base;
            
            System.out.println("[GameRoom] Winner: " + winner + " | Round: " + round + " | Time: " + completionTimeMs + "ms/" + totalTimeMs + "ms | Score: " + base + "+" + (int)bonus + "=" + award);
            System.out.println("[GameRoom] Game scores: " + host + "=" + gameScores.getOrDefault(host, 0) + ", " + opponent + "=" + gameScores.getOrDefault(opponent, 0));
            
            Persistence.addPoints(winner, award);
            lobby.sendToBoth(this, Message.of(MessageType.ROUND_END, new RoundEnd(id, winner, correct, base, bonus, award)));
        } else {
            // No winner (time out)
            lobby.sendToBoth(this, Message.of(MessageType.ROUND_END, new RoundEnd(id, null, correct, 0, 0, 0)));
        }
        
        if(round >= 4) {
            // Game ended - determine final winner by total scores
            int hostScore = gameScores.getOrDefault(host, 0);
            int opponentScore = gameScores.getOrDefault(opponent, 0);
            
            String finalWinner;
            if (hostScore > opponentScore) {
                finalWinner = host;
            } else if (opponentScore > hostScore) {
                finalWinner = opponent;
            } else {
                finalWinner = "Hòa";
            }
            
            // Thêm 50 điểm cho người thắng game
            if (!finalWinner.equals("Hòa")) {
                int gameWinBonus = 50;
                Persistence.addPoints(finalWinner, gameWinBonus);
                System.out.println("[GameRoom] Game winner " + finalWinner + " receives bonus: +" + gameWinBonus + " points");
            }
            
            System.out.println("[GameRoom] Final game result: " + host + "(" + hostScore + ") vs " + opponent + "(" + opponentScore + ") -> Winner: " + finalWinner);
            
            // Lưu lịch sử đấu vào database (cả khi hòa)
            System.out.println("[GameRoom] Saving match result to database...");
            Persistence.saveMatchResult(host, opponent, finalWinner, hostScore, opponentScore, 0, 0);
            System.out.println("[GameRoom] Match result saved successfully!");
            
            gameEnded = true; // Mark game as ended to prevent duplicate saves
            
            var s1 = new Summary(host, 0, 0, 0, 0);
            var s2 = new Summary(opponent, 0, 0, 0, 0);
            lobby.sendToBoth(this, Message.of(MessageType.GAME_END, new GameEnd(id, finalWinner, s1, s2)));
            
            lobby.snapshot();
            System.out.println("[GameRoom] Game ended, broadcasted lobby snapshot to update leaderboard");
        } else { 
            round++; 
            startRound(); 
        }
    }

    private void endRound(String winner, int total, int remain){
        scheduler.shutdownNow(); scheduler = Executors.newSingleThreadScheduledExecutor();
        boolean win = winner!=null;
        int base = Payloads.baseScore(win);
        double bonus = (remain/(double)total)*3.0;
        int award = base + (int)Math.round(bonus);
        String correct = currentWord;


        if(win){ Persistence.addPoints(winner, award); }
        lobby.sendToBoth(this, Message.of(MessageType.ROUND_END, new RoundEnd(id, winner, correct, base, bonus, award)));


        if(round>=4){
            // Simple game end after 4 rounds (you can add win-by-difference rule)
            var s1 = new Summary(host, 0, 0, 0, 0); // TODO: track stats per player
            var s2 = new Summary(opponent, 0, 0, 0, 0);
            String finalWinner = winner != null ? winner : "Hòa"; // Fix NPE
            lobby.sendToBoth(this, Message.of(MessageType.GAME_END, new GameEnd(id, finalWinner, s1, s2)));
            
            // Broadcast updated lobby snapshot so leaderboard refreshes
            lobby.snapshot();
            System.out.println("[GameRoom] Game ended, broadcasted lobby snapshot to update leaderboard");
        } else { round++; startRound(); }
    }
public void onSurrender(String player) {
    System.out.println("[GameRoom] Player " + player + " surrendered");
    
    // Prevent processing surrender if game already ended
    if (gameEnded) {
        System.out.println("[GameRoom] Game already ended, ignoring surrender");
        return;
    }
    
    // Determine opponent who wins by default
    String winner = player.equals(host) ? opponent : host;
    System.out.println("[GameRoom] Opponent " + winner + " wins by surrender");
    
    // Send notification to opponent that they won by surrender
    String notificationMessage = player + " đã đầu hàng! Bạn thắng!";
    Models.Chat winNotification = new Models.Chat(id, "🏆 System", notificationMessage);
    lobby.sendTo(winner, Message.of(MessageType.CHAT, winNotification));
    
    System.out.println("[GameRoom] Sent surrender notification to " + winner);
    
    // Award winner full points using new scoring system (instant completion = max bonus)
    int totalTimeMs = Payloads.timeLimitByLength(currentWord.length()) * 1000;
    int award = Payloads.calculateScore(true, 0, totalTimeMs, round); // 0 completion time = max score
    int base = Payloads.baseScoreByRound(round);
    double bonus = base; // Full bonus for surrender win
    
    System.out.println("[GameRoom] Surrender win - Winner: " + winner + " | Round: " + round + " | Score: " + base + "+" + (int)bonus + "=" + award);
    
    // Award points and send round end
    Persistence.addPoints(winner, award);
    
    // Update game scores for proper winner determination
    gameScores.put(winner, gameScores.getOrDefault(winner, 0) + award);
    System.out.println("[GameRoom] Game scores after surrender: " + gameScores);
    
    lobby.sendToBoth(this, Message.of(MessageType.ROUND_END, new RoundEnd(id, winner, currentWord, base, bonus, award)));
    
    // Force round to 4 to end game immediately
    round = 4;
    
    // End game with proper total score comparison
    int hostScore = gameScores.getOrDefault(host, 0);
    int opponentScore = gameScores.getOrDefault(opponent, 0);
    String finalWinner;
    if (hostScore > opponentScore) {
        finalWinner = host;
    } else if (opponentScore > hostScore) {
        finalWinner = opponent;
    } else {
        finalWinner = null; // Actual tie
    }
    
    System.out.println("[GameRoom] Final game winner (surrender): " + finalWinner + " | Host: " + hostScore + ", Opponent: " + opponentScore);
    
    // Lưu lịch sử đấu vào database (cả khi null/hòa)
    System.out.println("[GameRoom] Saving surrender match result to database...");
    String winnerToSave = (finalWinner != null) ? finalWinner : "Hòa";
    Persistence.saveMatchResult(host, opponent, winnerToSave, hostScore, opponentScore, 0, 0);
    System.out.println("[GameRoom] Surrender match result saved successfully!");
    
    gameEnded = true; // Mark game as ended to prevent duplicate saves
    
    var s1 = new Summary(host, 0, 0, 0, 0);
    var s2 = new Summary(opponent, 0, 0, 0, 0);
    lobby.sendToBoth(this, Message.of(MessageType.GAME_END, new GameEnd(id, finalWinner, s1, s2)));
    
    lobby.snapshot();
    System.out.println("[GameRoom] Game ended due to surrender, broadcasted lobby snapshot");
}

public void onDisconnect(String user){ /* you can award win to remaining player */ }
}