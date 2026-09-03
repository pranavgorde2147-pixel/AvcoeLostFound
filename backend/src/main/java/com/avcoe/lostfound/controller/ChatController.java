package com.avcoe.lostfound.controller;

import com.avcoe.lostfound.dto.*;
import com.avcoe.lostfound.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/conversations")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping
    public ResponseEntity<List<ConversationResponse>> getMyConversations() {
        List<ConversationResponse> response = chatService.getMyConversations();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{conversationId}/messages")
    public ResponseEntity<?> getMessages(@PathVariable Long conversationId,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "50") int size) {
        try {
            List<MessageResponse> response = chatService.getConversationMessages(conversationId, page, size);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{conversationId}/messages")
    public ResponseEntity<?> sendMessage(@PathVariable Long conversationId,
                                         @Valid @RequestBody SendMessageRequest request) {
        try {
            MessageResponse response = chatService.sendMessage(conversationId, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{conversationId}/verification-question")
    public ResponseEntity<?> sendVerificationQuestion(@PathVariable Long conversationId,
                                                      @RequestBody Map<String, String> request) {
        try {
            chatService.sendVerificationQuestion(conversationId, request.get("question"));
            return ResponseEntity.ok(Map.of("message", "Verification question sent"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
