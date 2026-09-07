package com.example.digital_wallet.notify.service;

import com.example.digital_wallet.common.security.CurrentUserService;
import com.example.digital_wallet.notify.entity.Notification;
import com.example.digital_wallet.notify.entity.NotificationStatus;
import com.example.digital_wallet.notify.entity.NotificationType;
import com.example.digital_wallet.notify.mapper.NotifyMapper;
import com.example.digital_wallet.notify.repository.NotificationRepository;
import com.example.digital_wallet.notify.response.NotificationResponse;
import com.example.digital_wallet.user.repository.UserRepository;
import com.example.digital_wallet.user.service.UserService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class NotificationService {

    NotificationRepository notificationRepository;
    NotifyMapper notifyMapper;
    CurrentUserService currentUserService;
    SimpMessagingTemplate messagingTemplate;
    UserService userService;


    @Transactional
    public NotificationResponse createNotification(
            UUID userId,
            NotificationType type,
            String title,
            String message,
            UUID referenceId
    ) {
        Notification notification = Notification.builder()
                .userId(userId)
                .type(type)
                .title(title)
                .message(message)
                .status(NotificationStatus.UNREAD)
                .referenceId(referenceId)
                .createdAt(OffsetDateTime.now())
                .build();

        notificationRepository.save(notification);

        NotificationResponse response = notifyMapper.toNotificationResponse(notification);
        String username = userService.getUser(userId).getUsername();

        messagingTemplate.convertAndSendToUser(
                username,
                "/queue/notifications",
                response
        );

        return response;
    }

    @Transactional
    public Page<NotificationResponse> getNotifications(
            int page,
            int size
    ) {

        UUID userId = currentUserService.getCurrentUser().getId();

        Pageable pageable = PageRequest.of(page, size);
        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(notifyMapper::toNotificationResponse);
    }

    @Transactional
    public long getUnreadCount() {
        UUID userId = currentUserService.getCurrentUser().getId();
        return notificationRepository.countByUserIdAndStatus(
                userId,
                NotificationStatus.UNREAD
        );
    }



    @Transactional
    public void markAsRead(UUID notificationId) {
        UUID userId = currentUserService.getCurrentUser().getId();

        Notification notification = notificationRepository
                .findById(notificationId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Notification not found"));

        if (!notification.getUserId().equals(userId)) {
            throw new IllegalArgumentException(
                    "You cannot access this notification"
            );
        }

        if (notification.getStatus() == NotificationStatus.READ) {
            return;
        }

        notification.setStatus(NotificationStatus.READ);
        notification.setReadAt(OffsetDateTime.now());
    }


}
