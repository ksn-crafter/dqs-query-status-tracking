package com.dqs.subquery.service;

import com.dqs.query.entity.Status;
import com.dqs.subquery.entity.SubQuery;
import com.dqs.subquery.repository.SubQueryRepository;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, topics = {"subqueries_executed_jpmc"})
class SubQueryServiceIntegrationTest {

    @Autowired
    private SubQueryService subqueryService;

    @Autowired
    private SubQueryRepository subQueryRepository;

    private static final MongodExecutable mongodExecutable;

    private static final int mongoPort;

    static {
        try {
            mongoPort = Network.getFreeServerPort();
            MongodConfig config = MongodConfig.builder()
                    .version(Version.Main.V6_0)
                    .net(new Net(mongoPort, Network.localhostIsIPv6()))
                    .build();
            mongodExecutable = MongodStarter.getDefaultInstance().prepare(config);
            mongodExecutable.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @DynamicPropertySource
    static void setMongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", () -> "mongodb://localhost:" + mongoPort + "/dqs");
    }

    @AfterAll
    public void stopEmbeddedMongo() {
        mongodExecutable.stop();
    }

    @Test
    void completeASubQuery() {
        subQueryRepository.deleteAllById(List.of("1", "2"));
        subQueryRepository.save(new SubQuery("1", "query-1", "subquery-1", 2, Status.Completed));
        subQueryRepository.save(new SubQuery("2", "query-1", "subquery-2", 2, Status.InProgress));

        subqueryService.completeSubQuery("query-1", "subquery-2");

        boolean allDone = subqueryService.areAllSubQueriesDone("query-1");

        assertThat(allDone).isTrue();
    }

    @Test
    void attemptToCompleteSubqueryWithAnInvalidQueryId() {
        assertThrows(RuntimeException.class, () -> {
            subqueryService.completeSubQuery("anything", "subquery-2");
        });
    }

    @Test
    void allSubQueriesAreNotDone() {
        subQueryRepository.deleteAllById(List.of("1", "2"));
        subQueryRepository.save(new SubQuery("1", "query-1", "subquery-1", 2, Status.Completed));
        subQueryRepository.save(new SubQuery("2", "query-1", "subquery-2", 2, Status.InProgress));

        boolean allDone = subqueryService.areAllSubQueriesDone("query-1");

        assertThat(allDone).isFalse();
    }

    @Test
    void allSubQueriesAreNotDoneDespiteCompletingOneSubQuery() {
        subQueryRepository.deleteAllById(List.of("101", "201", "301"));
        subQueryRepository.save(new SubQuery("101", "query-10", "subquery-10", 3, Status.Completed));
        subQueryRepository.save(new SubQuery("201", "query-10", "subquery-20", 3, Status.InProgress));
        subQueryRepository.save(new SubQuery("301", "query-10", "subquery-30", 3, Status.InProgress));

        subqueryService.completeSubQuery("query-10", "subquery-30");

        boolean allDone = subqueryService.areAllSubQueriesDone("query-10");

        assertThat(allDone).isFalse();
    }

    @Test
    void allSubQueriesAreDone() {
        subQueryRepository.deleteAllById(List.of("102", "202", "302"));
        subQueryRepository.save(new SubQuery("102", "query-15", "subquery-10", 3, Status.Completed));
        subQueryRepository.save(new SubQuery("202", "query-15", "subquery-20", 3, Status.InProgress));
        subQueryRepository.save(new SubQuery("302", "query-15", "subquery-30", 3, Status.InProgress));

        subqueryService.completeSubQuery("query-15", "subquery-20");
        subqueryService.completeSubQuery("query-15", "subquery-30");

        boolean allDone = subqueryService.areAllSubQueriesDone("query-15");

        assertThat(allDone).isTrue();
    }
}