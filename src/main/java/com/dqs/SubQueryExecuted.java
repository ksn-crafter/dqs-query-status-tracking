package com.dqs;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class SubQueryExecuted {

    @JsonProperty
    private String queryId;
    @JsonProperty
    private String subQueryId;
    @JsonProperty
    private LocalDateTime completionTime;

    public SubQueryExecuted() {
    }

    public SubQueryExecuted(String queryId, String subQueryId, LocalDateTime completionTime) {
        this.queryId = queryId;
        this.subQueryId = subQueryId;
        this.completionTime = completionTime;
    }

    public String queryId() {
        return queryId;
    }

    public String subQueryId() {
        return subQueryId;
    }
}
