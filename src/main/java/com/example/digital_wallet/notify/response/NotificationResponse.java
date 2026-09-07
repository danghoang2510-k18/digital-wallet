package com.example.digital_wallet.notify.response;

import com.example.digital_wallet.notify.entity.NotificationStatus;
import com.example.digital_wallet.notify.entity.NotificationType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.OffsetDateTime;
import java.util.UUID;


@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class NotificationResponse {

    UUID id;

    NotificationType type;

    String title;

    String message;

    NotificationStatus status;

    UUID referenceId;

    OffsetDateTime createdAt;

    OffsetDateTime readAt;

}
