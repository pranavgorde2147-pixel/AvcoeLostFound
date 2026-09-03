package com.avcoe.lostfound.service;

import com.avcoe.lostfound.dto.NotificationResponse;
import com.avcoe.lostfound.dto.PaginatedResponse;
import com.avcoe.lostfound.entity.Notification;
import com.avcoe.lostfound.entity.User;
import com.avcoe.lostfound.repository.NotificationRepository;
import com.avcoe.lostfound.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository,
                               SimpMessagingTemplate messagingTemplate) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    private User getCurrentUser() {
        String email = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow();
    }

    public void createNotification(User user, String title, String message,
                                    Notification.NotificationType type, Long referenceId, String referenceType) {
        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .referenceId(referenceId)
                .referenceType(referenceType)
                .read(false)
                .build();
        notification = notificationRepository.save(notification);

        NotificationResponse response = mapToResponse(notification);
        messagingTemplate.convertAndSendToUser(user.getEmail(), "/queue/notifications", response);
    }

    public PaginatedResponse<NotificationResponse> getMyNotifications(int page, int size) {
        User user = getCurrentUser();
        var notifPage = notificationRepository.findByUserIdOrderByCreatedAtDesc(
                user.getId(), PageRequest.of(page, size));

        List<NotificationResponse> content = notifPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PaginatedResponse.of(content, page, size, notifPage.getTotalElements());
    }

    public long getUnreadCount() {
        User user = getCurrentUser();
        return notificationRepository.countByUserIdAndReadFalse(user.getId());
    }

    public void markAllAsRead() {
        User user = getCurrentUser();
        notificationRepository.markAllAsRead(user.getId());
    }

    private NotificationResponse mapToResponse(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setTitle(notification.getTitle());
        response.setMessage(notification.getMessage());
        response.setType(notification.getType().name());
        response.setReferenceId(notification.getReferenceId());
        response.setReferenceType(notification.getReferenceType());
        response.setRead(notification.isRead());
        response.setCreatedAt(notification.getCreatedAt());
        return response;
    }
}
