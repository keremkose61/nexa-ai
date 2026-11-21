package com.nexa.backend.service;

import com.nexa.backend.dto.response.NotificationResponse;
import com.nexa.backend.entity.Notification;
import com.nexa.backend.entity.User;
import com.nexa.backend.repository.NotificationRepository;
import com.nexa.backend.repository.UserRepository;
import com.nexa.common.exception.ErrorCode;
import com.nexa.common.exception.NexaException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional
    public NotificationResponse createNotification(UUID userId, String title, String message,
                                                   Notification.NotificationType type, String actionUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NexaException(ErrorCode.USER_NOT_FOUND));

        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type != null ? type : Notification.NotificationType.INFO)
                .actionUrl(actionUrl)
                .read(false)
                .build();

        notification = notificationRepository.save(notification);
        return mapToResponse(notification);
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotifications(UUID userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getUnreadNotifications(UUID userId, Pageable pageable) {
        return notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(UUID userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    @Transactional
    public void markAsRead(UUID userId, UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NexaException(ErrorCode.INTERNAL_ERROR, "Notification not found"));

        if (!notification.getUser().getId().equals(userId)) {
            throw new NexaException(ErrorCode.ACCESS_DENIED);
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public int markAllAsRead(UUID userId) {
        return notificationRepository.markAllAsRead(userId);
    }

    public NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .read(notification.isRead())
                .actionUrl(notification.getActionUrl())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
