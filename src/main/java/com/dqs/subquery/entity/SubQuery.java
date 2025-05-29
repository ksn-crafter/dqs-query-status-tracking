package com.dqs.subquery.entity;

import com.dqs.query.entity.Status;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Document(collection = "sub_queries")
public class SubQuery {

    @Id
    private String id;
    private String queryId;
    private String subQueryId;
    private String[] filePaths;
    private Status status;
    private LocalDateTime creationTime;
    private LocalDateTime completionTime;
    private int totalSubqueries;

    public SubQuery() {
    }

    public SubQuery(String id, String queryId, String subQueryId, int totalSubqueries, Status status) {
        this.id = id;
        this.queryId = queryId;
        this.subQueryId = subQueryId;
        this.totalSubqueries = totalSubqueries;
        this.status = status;
    }

    public SubQuery(String id, String queryId, String subQueryId, int totalSubqueries, Status status, LocalDateTime completionTime) {
        this.id = id;
        this.queryId = queryId;
        this.subQueryId = subQueryId;
        this.totalSubqueries = totalSubqueries;
        this.status = status;
        this.completionTime = completionTime;
    }

    public void complete() {
        this.status = Status.Completed;
        this.completionTime = LocalDateTime.now();
    }

    public int totalSubqueries() {
        return totalSubqueries;
    }

    public Status status() {
        return status;
    }

    public LocalDateTime completionTime() {
        return completionTime;
    }
}