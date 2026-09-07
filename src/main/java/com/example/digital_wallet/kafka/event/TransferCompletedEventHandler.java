package com.example.digital_wallet.kafka.event;

import com.example.digital_wallet.notify.entity.Notification;
import com.example.digital_wallet.notify.entity.NotificationType;
import com.example.digital_wallet.notify.service.NotificationService;
import com.example.digital_wallet.wallet.service.WalletService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@Slf4j
public class TransferCompletedEventHandler {
    ProcessedEventRepository processedEventRepository;
    NotificationService notificationService;
    WalletService walletService;

    @Transactional
    public void process(
            TransferCompletedEvent event
    ) throws InterruptedException {

        UUID eventId = event.getEventId();


        if (processedEventRepository.existsById(eventId)) {
            log.info(
                    "Event {} already processed. Skip.",
                    eventId
            );
            return;
        }

        UUID senderId = walletService.getUserIdByWalletId(event.getSenderWalletId());
        UUID receiverId = walletService.getUserIdByWalletId(event.getReceiverWalletId());
        notificationService.createNotification(
                senderId,
                NotificationType.TRANSFER_SUCCESS,
                "Chuyển tiền thành công",
                "Đã chuyển " + event.getAmount() + "VND.",
                event.getTransactionId()

        );


        notificationService.createNotification(
                receiverId,
                NotificationType.TRANSFER_SUCCESS,
                "Nhận tiền thành công",
                "Đã nhận " + event.getAmount() + "VND.",
                event.getTransactionId()

        );

        processedEventRepository.save(
                ProcessedEvent.builder()
                        .eventId(eventId)
                        .processedAt(OffsetDateTime.now())
                        .build()
        );



    }
}
