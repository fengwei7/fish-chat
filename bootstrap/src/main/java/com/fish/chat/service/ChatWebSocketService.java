package com.fish.chat.service;

public interface ChatWebSocketService {

    public void sendMessageToUser(String userId, String type, String message);
    public void broadcastMessage(String type, String message);
}
