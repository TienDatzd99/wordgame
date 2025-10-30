package com.dat.wordgame.server;


import com.dat.wordgame.common.*; import com.dat.wordgame.common.Models.*;
import java.util.*; import java.util.concurrent.*; import java.util.stream.*;


public class GameRoom {
private final String id = UUID.randomUUID().toString();
private final String host; private final String opponent; private final LobbyManager lobby;
private volatile int round = 1; private volatile String currentWord; private volatile long roundEndAt;
private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();


public GameRoom(String host, String opponent, LobbyManager lobby){ this.host=host; this.opponent=opponent; this.lobby=lobby; }
public String id(){ return id; } public String host(){ return host; } public String opponent(){ return opponent; }


public Models.RoomBrief brief(){ return new Models.RoomBrief(id, host, opponent, "playing"); }


public void notifyJoin(){
lobby.sendTo(host, Message.of(MessageType.ROOM_JOINED, new RoomState(id, host, opponent, round, "ready")));
lobby.sendTo(opponent, Message.of(MessageType.ROOM_JOINED, new RoomState(id, host, opponent, round, "ready")));
startRound();
}


private void startRound(){
currentWord = WordService.pickByRound(round);
int total = Payloads.timeLimitByLength(currentWord.length());
roundEndAt = System.currentTimeMillis() + total*1000L;
var letters = currentWord.chars().mapToObj(c->(char)c).collect(Collectors.toList());
Collections.shuffle(letters);
var rs = new RoundStart(id, round, "_".repeat(currentWord.length()), letters, total);
lobby.sendToBoth(this, Message.of(MessageType.ROUND_START, rs));


scheduler.scheduleAtFixedRate(() -> tick(total), 1, 1, TimeUnit.SECONDS);
}


private void tick(int total){
int remain = (int)Math.max(0, (roundEndAt - System.currentTimeMillis())/1000L);
lobby.sendToBoth(this, Message.of(MessageType.ROUND_TICK, new RoundTick(id, remain)));
if(remain<=0){ endRound(null, total, 0); }
}


public void onGuess(String from, String guess){
int correct = 0; for(int i=0;i<Math.min(guess.length(), currentWord.length());i++) if(guess.charAt(i)==currentWord.charAt(i)) correct++;
lobby.sendToBoth(this, Message.of(MessageType.GUESS_UPDATE, new GuessUpdate(id, from, correct)));
if(guess.equalsIgnoreCase(currentWord)){
int remain = (int)Math.max(0, (roundEndAt - System.currentTimeMillis())/1000L);
endRound(from, Payloads.timeLimitByLength(currentWord.length()), remain);
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
lobby.sendToBoth(this, Message.of(MessageType.GAME_END, new GameEnd(id, winner, s1, s2)));
} else { round++; startRound(); }
}


public void onDisconnect(String user){ /* you can award win to remaining player */ }
}