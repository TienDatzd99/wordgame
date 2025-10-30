package com.dat.wordgame.common;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class Json {
public static final Gson GSON = new GsonBuilder().serializeNulls().create();
public static String encode(Message m){ return GSON.toJson(m); }
public static Message decode(String s){ return GSON.fromJson(s, Message.class); }
}