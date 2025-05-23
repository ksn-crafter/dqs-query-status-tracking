package com.dqs.configuration;

import com.dqs.SubQueryExecuted;
import com.dqs.SubQueryExecutedConsumer;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class DynamicKafkaConsumerConfiguration {

    @Autowired
    private ConsumerFactory<String, SubQueryExecuted> consumerFactory;

    @Autowired
    private SubQueryExecutedConsumer subQueryExecutedConsumer;

    private final List<KafkaMessageListenerContainer<String, SubQueryExecuted>> containers = new ArrayList<>();

    public void registerConsumerForTopic(String tenantId) {
        String topic = "subqueries_executed_" + tenantId;
        String groupId = "subquery-executed-consumer-" + tenantId;

        ContainerProperties containerProps = new ContainerProperties(topic);
        containerProps.setGroupId(groupId);
        containerProps.setMessageListener((MessageListener<String, SubQueryExecuted>) record -> {
            SubQueryExecuted event = record.value();
            subQueryExecutedConsumer.consume(event);
        });

        KafkaMessageListenerContainer<String, SubQueryExecuted> container =
                new KafkaMessageListenerContainer<>(consumerFactory, containerProps);

        container.start();
        containers.add(container);
    }

    @PreDestroy
    public void stopAllContainers() {
        for (KafkaMessageListenerContainer<?, ?> container : containers) {
            container.stop();
        }
    }
}
