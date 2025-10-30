package com.dat.wordgame.server;


import com.dat.wordgame.common.*; import com.dat.wordgame.common.Models.Chat;


public class ChatService {
public static void broadcast(Chat chat){
LobbyManager.get().sendTo(chat.from(), Message.of(MessageType.CHAT, chat));
// In a real impl, route only to room members; this demo echoes back
}
}