package me.zikani.labs.articulated.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class KafkaArticleConsumer implements Runnable {
    private final KafkaConsumer<String, String> consumer;
    private final List<String> topics;
    private final AtomicBoolean shutdown;
    private final CountDownLatch shutdownLatch;

    public KafkaArticleConsumer(String topicName, Properties kafkaConfig) {
        this.topics = Collections.singletonList(topicName);
        this.consumer = new KafkaConsumer<>(kafkaConfig);
        this.consumer.subscribe(Collections.singletonList(topicName));
        this.shutdownLatch = new CountDownLatch(1);
        this.shutdown = new AtomicBoolean(false);
    }


    public void process(ConsumerRecord<String, String> record) {
        LoggerFactory.getLogger(getClass()).info("Processed data from kafka {}", record.value());
    }

    public void run() {
        try {
            consumer.subscribe(topics);

            while (!shutdown.get()) {
                ConsumerRecords<String, String> records = consumer.poll(500);
                records.forEach(record -> process(record));
            }
        } finally {
            consumer.close();
            shutdownLatch.countDown();
        }
    }

    public void shutdown() throws InterruptedException {
        shutdown.set(true);
        shutdownLatch.await();
    }
}
