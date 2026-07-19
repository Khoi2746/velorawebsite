package com.velora.website.dto; 

public class ChatMessage {
    // --- Dùng cho luồng WebSocket ---
    private String sender;
    private String content; 
    private String timestamp;

    // --- Bổ sung thêm dùng cho luồng REST API ---
    private String message;     // Hàm /tu-van bên Vue đang ném biến "message" lên
    private String maPhienChat; // Phân biệt các phòng chat
    private String tenKhach;    // Bắt tên để quăng lên Admin hiển thị

    // Constructor rỗng
    public ChatMessage() {}

    // Constructor đầy đủ cho WebSocket
    public ChatMessage(String sender, String content, String timestamp) {
        this.sender = sender;
        this.content = content;
        this.timestamp = timestamp;
    }

    // --- Getter và Setter ---
    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getMaPhienChat() { return maPhienChat; }
    public void setMaPhienChat(String maPhienChat) { this.maPhienChat = maPhienChat; }

    public String getTenKhach() { return tenKhach; }
    public void setTenKhach(String tenKhach) { this.tenKhach = tenKhach; }
}