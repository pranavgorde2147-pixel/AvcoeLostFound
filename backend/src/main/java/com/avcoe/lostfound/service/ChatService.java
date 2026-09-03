package com.avcoe.lostfound.service;

import com.avcoe.lostfound.dto.*;
import com.avcoe.lostfound.entity.*;
import com.avcoe.lostfound.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatService(ConversationRepository conversationRepository, MessageRepository messageRepository,
                       UserRepository userRepository, ItemRepository itemRepository,
                       NotificationService notificationService, SimpMessagingTemplate messagingTemplate) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.notificationService = notificationService;
        this.messagingTemplate = messagingTemplate;
    }

    private User getCurrentUser() {
        String email = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow();
    }

    public List<ConversationResponse> getMyConversations() {
        User user = getCurrentUser();
        List<Conversation> conversations = conversationRepository.findByParticipantId(user.getId());

        return conversations.stream().map(conv -> {
            ConversationResponse response = new ConversationResponse();
            response.setId(conv.getId());
            response.setTitle(conv.getTitle());
            response.setItemId(conv.getItem().getId());
            response.setItemName(conv.getItem().getItemName());

            List<ParticipantResponse> participants = conv.getParticipants().stream().map(p -> {
                ParticipantResponse pr = new ParticipantResponse();
                pr.setId(p.getId());
                pr.setFullName(p.getFullName());
                pr.setProfileImageUrl(p.getProfileImageUrl());
                pr.setRole(p.getRole().name());
                return pr;
            }).collect(Collectors.toList());
            response.setParticipants(participants);

            Page<Message> lastMsgPage = messageRepository.findByConversationIdOrderByCreatedAtAsc(
                    conv.getId(), PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "createdAt")));
            if (!lastMsgPage.isEmpty()) {
                Message lastMsg = lastMsgPage.getContent().get(0);
                response.setLastMessage(lastMsg.getContent());
            }

            response.setLastMessageAt(conv.getLastMessageAt());
            response.setCreatedAt(conv.getCreatedAt());
            return response;
        }).collect(Collectors.toList());
    }

    public List<MessageResponse> getConversationMessages(Long conversationId, int page, int size) {
        User user = getCurrentUser();

        if (!conversationRepository.isParticipant(conversationId, user.getId())) {
            throw new IllegalArgumentException("Access denied");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId, pageable);

        messageRepository.markAsRead(conversationId, user.getId());

        return messages.getContent().stream().map(msg -> {
            MessageResponse response = new MessageResponse();
            response.setId(msg.getId());
            response.setConversationId(msg.getConversation().getId());
            response.setSenderId(msg.getSender().getId());
            response.setSenderName(msg.getSender().getFullName());
            response.setContent(msg.getContent());
            response.setMessageType(msg.getMessageType().name());
            response.setRead(msg.isRead());
            response.setCreatedAt(msg.getCreatedAt());
            return response;
        }).collect(Collectors.toList());
    }

    public MessageResponse sendMessage(Long conversationId, SendMessageRequest request) {
        User sender = getCurrentUser();

        if (!conversationRepository.isParticipant(conversationId, sender.getId())) {
            throw new IllegalArgumentException("Access denied");
        }

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

        Message.MessageType msgType = Message.MessageType.TEXT;
        if (request.getMessageType() != null) {
            try {
                msgType = Message.MessageType.valueOf(request.getMessageType().toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }

        Message message = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .content(request.getContent())
                .messageType(msgType)
                .read(false)
                .build();
        message = messageRepository.save(message);

        conversation.setLastMessageAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        MessageResponse response = mapMessageResponse(message);

        conversation.getParticipants().forEach(participant -> {
            if (!participant.getId().equals(sender.getId())) {
                messagingTemplate.convertAndSendToUser(
                        participant.getEmail(),
                        "/queue/messages",
                        response
                );

                notificationService.createNotification(
                        participant,
                        "New Message",
                        sender.getFullName() + " sent you a message about \"" + conversation.getItem().getItemName() + "\"",
                        Notification.NotificationType.MESSAGE_RECEIVED,
                        conversation.getId(),
                        "CONVERSATION"
                );
            }
        });

        return response;
    }

    public void sendVerificationQuestion(Long conversationId, String question) {
        User sender = getCurrentUser();

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

        Message message = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .content("🔐 Ownership Verification\n\nTo help verify ownership, please answer:\n\n\"" + question + "\"")
                .messageType(Message.MessageType.VERIFICATION_QUESTION)
                .read(false)
                .build();
        message = messageRepository.save(message);

        conversation.setLastMessageAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        MessageResponse response = mapMessageResponse(message);
        conversation.getParticipants().forEach(participant -> {
            if (!participant.getId().equals(sender.getId())) {
                messagingTemplate.convertAndSendToUser(participant.getEmail(), "/queue/messages", response);
            }
        });
    }

    private MessageResponse mapMessageResponse(Message message) {
        MessageResponse response = new MessageResponse();
        response.setId(message.getId());
        response.setConversationId(message.getConversation().getId());
        response.setSenderId(message.getSender().getId());
        response.setSenderName(message.getSender().getFullName());
        response.setContent(message.getContent());
        response.setMessageType(message.getMessageType().name());
        response.setRead(message.isRead());
        response.setCreatedAt(message.getCreatedAt());
        return response;
    }
}
