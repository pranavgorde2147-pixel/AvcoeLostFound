package com.avcoe.lostfound.websocket;

import com.avcoe.lostfound.dto.MessageResponse;
import com.avcoe.lostfound.dto.SendMessageRequest;
import com.avcoe.lostfound.service.ChatService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class WebSocketChatController {

    private final ChatService chatService;

    public WebSocketChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @MessageMapping("/chat/{conversationId}")
    @SendTo("/topic/conversation/{conversationId}")
    public MessageResponse sendMessage(@DestinationVariable Long conversationId,
                                       @Payload SendMessageRequest request,
                                       Principal principal) {
        if (principal == null) {
            throw new IllegalArgumentException("Authentication required");
        }
        return chatService.sendMessage(conversationId, request, principal.getName());
    }
}
