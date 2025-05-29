package com.dqs;

import com.dqs.query.entity.QueryDescription;
import com.dqs.query.entity.Status;
import com.dqs.query.repository.QueryRepository;
import com.dqs.query.service.QueryStatusService;
import com.dqs.subquery.entity.SubQuery;
import com.dqs.subquery.repository.SubQueryRepository;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static java.time.Duration.ofSeconds;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;


@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, topics = {"sub_query_executed_jpmc", "sub_query_executed_deutsche"})
class SubQueryExecutedConsumerIntegrationTest {

    private KafkaTemplate<String, SubQueryExecuted> kafkaTemplate;

    @Autowired
    private QueryStatusService queryStatusService;

    @Autowired
    private SubQueryRepository subQueryRepository;

    @Autowired
    private QueryRepository queryRepository;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

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

    @BeforeEach
    void setup() {
        Map<String, Object> senderProps = KafkaTestUtils.producerProps(embeddedKafkaBroker);
        senderProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        senderProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        ProducerFactory<String, SubQueryExecuted> producerFactory = new DefaultKafkaProducerFactory<>(senderProps);
        kafkaTemplate = new KafkaTemplate<>(producerFactory);
    }

    @Test
    void markQueryAsCompleted() {
        subQueryRepository.deleteAllById(List.of("700", "800"));
        queryRepository.deleteByQueryId("query-510");

        queryRepository.save(new QueryDescription("query-510", "Deutsche", "Historical", 2001, 2007, Status.InProgress, LocalDateTime.now()));
        subQueryRepository.save(new SubQuery("700", "query-510", "subquery-1", 2, Status.InProgress));
        subQueryRepository.save(new SubQuery("800", "query-510", "subquery-2", 2, Status.InProgress));

        kafkaTemplate.send("sub_query_executed_deutsche", new SubQueryExecuted("query-510", "subquery-2", LocalDateTime.now()));
        kafkaTemplate.send("sub_query_executed_deutsche", new SubQueryExecuted("query-510", "subquery-1", LocalDateTime.now()));

        await().atMost(ofSeconds(5)).untilAsserted(() -> {
            SubQuery subQuery = subQueryRepository.findByQueryIdAndSubQueryId("query-510", "subquery-2").get();
            assertThat(subQuery.status()).isEqualTo(Status.Completed);
            assertThat(subQuery.completionTime()).isNotNull();

            QueryDescription queryDescription = queryRepository.findByQueryId("query-510").get();
            assertThat(queryDescription.status()).isEqualTo(Status.Completed);
        });
    }

    @Test
    void shouldNotMarkQueryAsCompletedGivenAllItsSubQueriesAreNotCompleted() {
        subQueryRepository.deleteAllById(List.of("1000", "1001", "1002"));
        queryRepository.deleteByQueryId("query-710");

        queryRepository.save(new QueryDescription("query-710", "JPMC", "Historical", 2001, 2007, Status.InProgress, LocalDateTime.now()));
        subQueryRepository.save(new SubQuery("1000", "query-710", "subquery-1", 3, Status.Completed, LocalDateTime.now()));
        subQueryRepository.save(new SubQuery("1001", "query-710", "subquery-2", 3, Status.InProgress));
        subQueryRepository.save(new SubQuery("1002", "query-710", "subquery-3", 4, Status.InProgress));

        SubQueryExecuted event = new SubQueryExecuted("query-710", "subquery-3", LocalDateTime.now());
        kafkaTemplate.send("sub_query_executed_jpmc", event);

        await().atMost(ofSeconds(5)).untilAsserted(() -> {
            SubQuery subQuery = subQueryRepository.findByQueryIdAndSubQueryId("query-710", "subquery-3").get();
            assertThat(subQuery.status()).isEqualTo(Status.Completed);
            assertThat(subQuery.completionTime()).isNotNull();

            QueryDescription queryDescription = queryRepository.findByQueryId("query-710").get();
            assertThat(queryDescription.status()).isEqualTo(Status.InProgress);
        });
    }
}
