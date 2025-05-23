package com.dqs.subquery.entity;

import com.dqs.query.entity.Status;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;

@Document(collection = "sub_queries")
public class SubQuery {

    @Id
    private String id;
    private String queryId;
    private String subQueryId;
    private long totalSubqueries;
    private Status status;

    public SubQuery() {
    }

    public SubQuery(String id, String queryId, String subQueryId, long totalSubqueries, Status status) {
        this.id = id;
        this.queryId = queryId;
        this.subQueryId = subQueryId;
        this.totalSubqueries = totalSubqueries;
        this.status = status;
    }

    public void complete() {
        this.status = Status.Completed;
    }

    public long totalSubqueries() {
        return totalSubqueries;
    }

    public Status status() {
        return status;
    }
}