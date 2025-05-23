package com.dqs;

import com.dqs.query.service.QueryStatusService;
import org.springframework.stereotype.Component;

@Component
public class SubQueryExecutedConsumer {

    private final QueryStatusService queryStatusService;

    public SubQueryExecutedConsumer(QueryStatusService queryStatusService) {
        this.queryStatusService = queryStatusService;
    }

    public void consume(SubQueryExecuted subQueryExecuted) {
        this.queryStatusService.mayBeCompleteTheQuery(subQueryExecuted.queryId(), subQueryExecuted.subQueryId());
    }
}
