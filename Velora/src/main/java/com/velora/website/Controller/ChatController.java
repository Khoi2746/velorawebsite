package com.velora.website.Controller;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.velora.website.dto.ChatMessage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;

    ChatController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    // Khi Client gửi tin nhắn tới /app/chat/{roomId}
    @MessageMapping("/chat/{roomId}")
    public void processMessage(@DestinationVariable String roomId, @Payload ChatMessage chatMessage) {
        // Thêm thời gian gửi nếu chưa có
        chatMessage.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));

        // Đẩy tin nhắn tới tất cả những ai đang đăng ký kênh /topic/chat/{roomId}
        messagingTemplate.convertAndSend("/topic/chat/" + roomId, chatMessage);
    }
}