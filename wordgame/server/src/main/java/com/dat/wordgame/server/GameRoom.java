package com.dat.wordgame.server;


import com.dat.wordgame.common.*; import com.dat.wordgame.common.Models.*;
import java.util.*; import java.util.concurrent.*; import java.util.stream.*;


public class GameRoom {
private final String id = UUID.randomUUID().toString();
private final String host; private final String opponent; private final LobbyManager lobby;
private volatile int round = 1; private volatile String currentWord; private volatile long roundEndAt;
private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
private Set<String> playersCorrect = new HashSet<>(); // Track who got it correct
private String firstCorrect = null; // Who answered correctly first
private long firstCorrectTime = 0; // Time when first correct answer


public GameRoom(String host, String opponent, LobbyManager lobby){ this.host=host; this.opponent=opponent; this.lobby=lobby; }
public String id(){ return id; } public String host(){ return host; } public String opponent(){ return opponent; }


public Models.RoomBrief brief(){ return new Models.RoomBrief(id, host, opponent, "playing"); }


public void notifyJoin(){
System.out.println("[GameRoom] notifyJoin() called for room " + id);
System.out.println("[GameRoom] Host: " + host + ", Opponent: " + opponent);
lobby.sendTo(host, Message.of(MessageType.ROOM_JOINED, new RoomState(id, host, opponent, round, "ready")));
lobby.sendTo(opponent, Message.of(MessageType.ROOM_JOINED, new RoomState(id, host, opponent, round, "ready")));
System.out.println("[GameRoom] ROOM_JOINED sent to both players, starting round...");
startRound();
}


private void startRound(){
System.out.println("[GameRoom] startRound() called for round " + round);
playersCorrect.clear(); // Reset for new round
firstCorrect = null;
firstCorrectTime = 0;
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
    // Time's up - determine winner
    endRound(firstCorrect, total, 0); 
}
}


public void onGuess(String from, String guess){
int correct = 0; for(int i=0;i<Math.min(guess.length(), currentWord.length());i++) if(guess.charAt(i)==currentWord.charAt(i)) correct++;
lobby.sendToBoth(this, Message.of(MessageType.GUESS_UPDATE, new GuessUpdate(id, from, correct)));

// Check if guess is correct
boolean isCorrect = guess.equalsIgnoreCase(currentWord);

if (isCorrect && !playersCorrect.contains(from)) {
    // First time this player got it correct
    playersCorrect.add(from);
    
    // Track who answered first
    if (firstCorrect == null) {
        firstCorrect = from;
        firstCorrectTime = System.currentTimeMillis();
        System.out.println("[GameRoom] " + from + " answered correctly FIRST!");
    } else {
        System.out.println("[GameRoom] " + from + " also answered correctly!");
    }
    
    // End round if BOTH players got it correct
    if (playersCorrect.size() >= 2) {
        int remain = (int)Math.max(0, (roundEndAt - System.currentTimeMillis())/1000L);
        System.out.println("[GameRoom] Both players correct! Ending round. Winner: " + firstCorrect);
        endRound(firstCorrect, Payloads.timeLimitByLength(currentWord.length()), remain);
    }
}
// If incorrect, just send GUESS_UPDATE (already sent above), client will show red border
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
    
    // Determine opponent who wins by default
    String winner = player.equals(host) ? opponent : host;
    System.out.println("[GameRoom] Opponent " + winner + " wins by surrender");
    
    // Send notification to opponent that they won by surrender
    String notificationMessage = player + " đã đầu hàng! Bạn thắng!";
    Models.Chat winNotification = new Models.Chat(id, "🏆 System", notificationMessage);
    lobby.sendTo(winner, Message.of(MessageType.CHAT, winNotification));
    
    // Calculate remaining time for display
    int msRemaining = Math.max(0, (int)(roundEndAt - System.currentTimeMillis()));
    
    // Force round to 4 to end game immediately
    round = 4;
    
    // Award full points to opponent, zero to surrendering player
    endRound(winner, msRemaining, 0);
}

public void onDisconnect(String user){ /* you can award win to remaining player */ }
}