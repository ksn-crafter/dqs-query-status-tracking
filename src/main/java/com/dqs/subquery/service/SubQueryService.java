package com.dqs.subquery.service;

import com.dqs.query.entity.Status;
import com.dqs.subquery.entity.SubQuery;
import com.dqs.subquery.repository.SubQueryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class SubQueryService {

    private final SubQueryRepository subQueryRepository;

    public SubQueryService(SubQueryRepository subQueryRepository) {
        this.subQueryRepository = subQueryRepository;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void completeSubQuery(String queryId, String subQueryId) {
        Optional<SubQuery> optionalSubQuery = subQueryRepository.findByQueryIdAndSubQueryId(queryId, subQueryId);
        if (optionalSubQuery.isPresent()) {
            System.out.println("Completing the subQuery with subQueryId " + subQueryId);
            SubQuery subQuery = optionalSubQuery.get();
            subQuery.complete();
            subQueryRepository.save(subQuery);
        } else {
            throw new RuntimeException(String.format("SubQuery not found for queryId %s and subQueryId %s", queryId, subQueryId));
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public boolean areAllSubQueriesDone(String queryId) {
        long totalSubQueries = subQueryRepository.findFirstByQueryId(queryId).map(SubQuery::totalSubqueries).get();
        long totalCompletedSubQueries = subQueryRepository.countByQueryIdAndStatus(queryId, Status.Completed);

        System.out.println("queryId " + queryId + ", totalSubQueries " + totalSubQueries + ", totalCompletedSubQueries " + totalCompletedSubQueries);
        return totalSubQueries == totalCompletedSubQueries;
    }
}
