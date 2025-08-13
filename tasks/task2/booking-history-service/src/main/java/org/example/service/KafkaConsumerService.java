package org.example.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.entity.BookingHistory;
import org.example.repository.BookingHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerService.class);

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private BookingHistoryRepository bookingHistoryRepository;

    @KafkaListener(topics = "booking")
    public void listen(ConsumerRecord<String, String> record) throws JsonProcessingException {
        log.info("Received message: " + record.value() + " from topic: " + record.topic());

        String booking = record.value().replace("Booking", "");

        BookingHistory bookingHistory = objectMapper.readValue(booking, BookingHistory.class);

        bookingHistoryRepository.save(bookingHistory);

    }

}
