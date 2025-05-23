package com.dqs.query.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "query_descriptions")
public class QueryDescription {

    @Id
    private String queryId;
    private String tenantId;
    private String term;
    private int yearStart;
    private int yearEnd;
    private Status status;
    private LocalDateTime creationTime;
    private LocalDateTime completionTime;

    public QueryDescription() {
    }

    public QueryDescription(String queryId, String tenantId, String term, int yearStart, int yearEnd, Status status, LocalDateTime creationTime) {
        this.queryId = queryId;
        this.tenantId = tenantId;
        this.term = term;
        this.yearStart = yearStart;
        this.yearEnd = yearEnd;
        this.status = status;
        this.creationTime = creationTime;
    }

    public void complete() {
        this.completionTime = LocalDateTime.now();
        this.status = Status.Completed;
    }

    public Status status() {
        return status;
    }
}
