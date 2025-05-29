package com.dqs.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class KafkaConsumerInitializer {

    @Autowired
    private DynamicKafkaConsumerConfiguration dynamicKafkaConsumerConfiguration;

    @Value("${tenants}")
    private String tenants;

    @PostConstruct
    public void initializeListeners() {
        List<String> tenants = Arrays.stream(this.tenants.split(",")).map(String::trim).map(String::toLowerCase).toList();
        for (String suffix : tenants) {
            dynamicKafkaConsumerConfiguration.registerConsumerForTopic(suffix);
        }
    }
}
