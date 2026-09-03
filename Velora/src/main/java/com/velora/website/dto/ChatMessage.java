package com.velora.website.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class ChatMessage {
    private String sender;
    private String content;
    private String timestamp;
    private List<Map<String, Object>> products;

    public ChatMessage() {}

    public ChatMessage(String sender, String content, String timestamp) {
        this.sender = sender;
        this.content = content;
        this.timestamp = timestamp;
    }

    public ChatMessage(String sender, String content, String timestamp, List<Map<String, Object>> products) {
        this.sender = sender;
        this.content = content;
        this.timestamp = timestamp;
        this.products = products;
    }
}