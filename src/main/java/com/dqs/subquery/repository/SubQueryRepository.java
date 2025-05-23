package com.dqs.subquery.repository;

import com.dqs.query.entity.Status;
import com.dqs.subquery.entity.SubQuery;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubQueryRepository extends MongoRepository<SubQuery, String>  {

    // Get any one subquery for a queryId
    Optional<SubQuery> findFirstByQueryId(String queryId);

    Optional<SubQuery> findByQueryIdAndSubQueryId(String queryId, String subQueryId);

    long countByQueryIdAndStatus(String queryId, Status status);
}
