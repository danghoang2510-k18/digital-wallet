package com.example.digital_wallet.kafka.producer;


import com.example.digital_wallet.kafka.event.TransactionCompletedEvent;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class TransactionEventProducer {
    ObjectMapper objectMapper;

    static String TOPIC = "wallet.transaction.completed";

    KafkaTemplate<String,String> kafkaTemplate;

    public void publishTransferCompleted(
            TransactionCompletedEvent event
    ) {

        kafkaTemplate.send(
                TOPIC,
                event.getTransactionId().toString(),
                objectMapper.writeValueAsString(event)
        );
    }
}
