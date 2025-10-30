package com.dat.wordgame.common;


public class Message {
public MessageType type;
public Object payload; // Will be serialized by Gson
public static Message of(MessageType t, Object p) { var m = new Message(); m.type = t; m.payload = p; return m; }
}