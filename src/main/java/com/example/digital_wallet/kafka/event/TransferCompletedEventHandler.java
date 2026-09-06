package com.example.digital_wallet.kafka.event;

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


        processedEventRepository.save(
                ProcessedEvent.builder()
                        .eventId(eventId)
                        .processedAt(OffsetDateTime.now())
                        .build()
        );
    }
}
