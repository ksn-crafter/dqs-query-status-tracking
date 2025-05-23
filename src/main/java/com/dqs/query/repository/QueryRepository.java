package com.dqs.query.repository;

import com.dqs.query.entity.QueryDescription;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QueryRepository extends MongoRepository<QueryDescription, String> {

    Optional<QueryDescription> findByQueryId(String queryId);

    void deleteByQueryId(String queryId);
}
