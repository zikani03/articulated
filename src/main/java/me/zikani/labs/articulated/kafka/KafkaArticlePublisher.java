package me.zikani.labs.articulated.kafka;

import me.zikani.labs.articulated.model.Article;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.List;
import java.util.Properties;

public class KafkaArticlePublisher {
    private final String topicName;
    private final Properties kafkaClientConfig;

    public KafkaArticlePublisher(String topicName, Properties kafkaClientConfig) {
        this.topicName = topicName;
        this.kafkaClientConfig = kafkaClientConfig;
    }

    public int publish(final List<Article> articles) {
        Producer<String, String> producer = new KafkaProducer<String, String>(this.kafkaClientConfig);
        int n = 0;
        for(var article: articles) {
            var future = producer.send(new ProducerRecord<>(topicName, article.getId(), article.getBody()));
            if (future.isDone()) {
                n++;
            }
        }

        producer.close();
        return n;
    }
}
