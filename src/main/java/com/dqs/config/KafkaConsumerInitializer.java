package com.dqs.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class KafkaConsumerInitializer {

    @Autowired
    private DynamicKafkaConsumerConfiguration dynamicKafkaConsumerConfiguration;

    @PostConstruct
    public void initializeListeners() {
        List<String> tenantIds = List.of("jpmc", "deutsche");
        for (String suffix : tenantIds) {
            dynamicKafkaConsumerConfiguration.registerConsumerForTopic(suffix);
        }
    }
}
