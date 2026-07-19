package com.velora.website.dto; // Đảm bảo đúng package của dự án em

public class ChatMessage {
    private String sender;
    private String content;
    private String timestamp;

    // Constructor rỗng
    public ChatMessage() {}

    // Constructor đầy đủ
    public ChatMessage(String sender, String content, String timestamp) {
        this.sender = sender;
        this.content = content;
        this.timestamp = timestamp;
    }

    // Getter và Setter (Cực kỳ quan trọng để Spring Boot nó map dữ liệu)
    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}