package com.example.digital_wallet.kafka.event;

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
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class TransactionCompletedEventHandler {

    ProcessedEventRepository processedEventRepository;
    NotificationService notificationService;
    WalletService walletService;

    @Transactional
    public void process(TransactionCompletedEvent event) {

        UUID eventId = event.getEventId();

        if (processedEventRepository.existsById(eventId)) {
            log.info("Event {} already processed. Skip.", eventId);
            return;
        }

        if (event instanceof TransferCompletedEvent transferEvent) {

            processTransfer(transferEvent);

        } else if (event instanceof TopUpCompletedEvent topUpEvent) {

            processTopUp(topUpEvent);

        } else if (event instanceof WithdrawCompletedEvent withdrawEvent) {

            processWithdraw(withdrawEvent);

        } else {

            log.warn(
                    "Unsupported different transaction event type: {}",
                    event.getClass().getSimpleName()
            );

            return;
        }

        processedEventRepository.save(
                ProcessedEvent.builder()
                        .eventId(eventId)
                        .processedAt(OffsetDateTime.now())
                        .build()
        );

        log.info(
                "Transaction completed event {} processed successfully.",
                eventId
        );
    }

    private void processTransfer(
            TransferCompletedEvent event
    ) {

        UUID senderId =
                walletService.getUserIdByWalletId(
                        event.getSenderWalletId()
                );

        UUID receiverId =
                walletService.getUserIdByWalletId(
                        event.getReceiverWalletId()
                );

        notificationService.createNotification(
                senderId,
                NotificationType.TRANSFER_SUCCESS,
                "Chuyển tiền thành công",
                "Đã chuyển " + event.getAmount() + " VND.",
                event.getTransactionId()
        );

        notificationService.createNotification(
                receiverId,
                NotificationType.TRANSFER_SUCCESS,
                "Nhận tiền thành công",
                "Đã nhận " + event.getAmount() + " VND.",
                event.getTransactionId()
        );

        log.info(
                "Transfer notification created. transactionId={}",
                event.getTransactionId()
        );
    }

    private void processTopUp(
            TopUpCompletedEvent event
    ) {

        UUID userId =
                walletService.getUserIdByWalletId(
                        event.getWalletId()
                );

        notificationService.createNotification(
                userId,
                NotificationType.TOP_UP_SUCCESS,
                "Nạp tiền thành công",
                "Đã nạp " + event.getAmount() + " VND vào ví.",
                event.getTransactionId()
        );

        log.info(
                "Top up notification created. transactionId={}",
                event.getTransactionId()
        );
    }

    private void processWithdraw(
            WithdrawCompletedEvent event
    ) {

        UUID userId =
                walletService.getUserIdByWalletId(
                        event.getWalletId()
                );

        notificationService.createNotification(
                userId,
                NotificationType.WITHDRAW_SUCCESS,
                "Rút tiền thành công",
                "Đã rút " + event.getAmount() + " VND khỏi ví.",
                event.getTransactionId()
        );

        log.info(
                "Withdraw notification created. transactionId={}",
                event.getTransactionId()
        );
    }
}
