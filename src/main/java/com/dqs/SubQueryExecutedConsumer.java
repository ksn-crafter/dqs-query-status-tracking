package com.dqs;

import com.dqs.query.service.QueryStatusService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class SubQueryExecutedConsumer {

    private final QueryStatusService queryStatusService;

    public SubQueryExecutedConsumer(QueryStatusService queryStatusService) {
        this.queryStatusService = queryStatusService;
    }

    @KafkaListener(topicPattern = "subqueries_executed_.*", groupId = "subquery-executed-consumer")
    public void consume(SubQueryExecuted event) {
        this.queryStatusService.mayBeCompleteTheQuery(event.queryId(), event.subQueryId());
    }
}
