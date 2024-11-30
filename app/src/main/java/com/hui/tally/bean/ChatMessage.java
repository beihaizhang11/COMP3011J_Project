package com.hui.tally.bean;

public class ChatMessage {
    private String sender;
    private String content;
    private boolean isAI;

    public ChatMessage(String sender, String content, boolean isAI) {
        this.sender = sender;
        this.content = content;
        this.isAI = isAI;
    }

    public String getSender() {
        return sender;
    }

    public String getContent() {
        return content;
    }

    public boolean isAI() {
        return isAI;
    }
} 