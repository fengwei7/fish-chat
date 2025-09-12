package com.fish.chat.service;

public interface ChatWebSocketService {

    void sendMessageToUser(String userId, String type, String message);

    void broadcastMessage(String type, String message);
}
