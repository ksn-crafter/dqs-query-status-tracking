package com.dqs;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SubQueryExecuted {

    @JsonProperty
    private String queryId;
    @JsonProperty
    private String subQueryId;

    public SubQueryExecuted() {
    }

    public SubQueryExecuted(String queryId, String subQueryId) {
        this.queryId = queryId;
        this.subQueryId = subQueryId;
    }

    public String queryId() {
        return queryId;
    }

    public String subQueryId() {
        return subQueryId;
    }
}
