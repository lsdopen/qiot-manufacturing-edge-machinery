package io.qiot.manufacturing.edge.machinery.service.production;

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.std.StdJdkSerializers;

import io.qiot.manufacturing.all.commons.domain.production.ProductionChainStageEnum;
import io.qiot.manufacturing.edge.machinery.domain.ProductionCountersDTO;
import io.quarkus.runtime.annotations.RegisterForReflection;

// JMS
import org.eclipse.microprofile.config.inject.ConfigProperty;
import javax.jms.ConnectionFactory;
import java.util.Objects;
import javax.jms.JMSContext;
import javax.jms.JMSProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.annotation.PostConstruct;

/**
 * @author andreabattaglia
 *
 */
@ApplicationScoped
@RegisterForReflection(targets = {
        StdJdkSerializers.AtomicIntegerSerializer.class,
        StdJdkSerializers.AtomicBooleanSerializer.class,
        StdJdkSerializers.AtomicLongSerializer.class })
public class CountersServiceImpl implements CountersService {

    @Inject
    Logger LOGGER;

    @Inject
    ObjectMapper MAPPER;

    // JMS
    @Inject
    ConnectionFactory connectionFactory;
    private JMSContext context;
    private JMSProducer producer;
    private Queue queue;
    @ConfigProperty(name = "qiot.productline.metrics.queue-prefix")
    String latestProductLineMetricsQueueName;

    @PostConstruct
    void init() {
        LOGGER.debug("Initiating ProductLine metrics emitter");
        metricsInit();
        LOGGER.debug("ProductLine metrics initiation complete");
    }

    private void metricsInit() {
        if (Objects.nonNull(context))
            context.close();
        context = connectionFactory.createContext(Session.AUTO_ACKNOWLEDGE);

        producer = context.createProducer();

        queue = context.createQueue(latestProductLineMetricsQueueName);
    }

    private final Map<UUID, ProductionCountersDTO> productionCounters;

    public CountersServiceImpl() {

        productionCounters = new TreeMap<UUID, ProductionCountersDTO>();
    }

    @Override
    public Map<UUID, ProductionCountersDTO> getCounters() {
        return productionCounters;
    }

    @Override
    public int recordNewItem(UUID productLineId) {
        try {
            if (!productionCounters.containsKey(productLineId))
                productionCounters.put(productLineId,
                        new ProductionCountersDTO(productLineId));
            int id = productionCounters.get(productLineId).totalItems
                    .incrementAndGet();
            // TODO: improve state transition here
            productionCounters.get(productLineId).stageCounters
                    .get(ProductionChainStageEnum.WEAVING).incrementAndGet();

            return id;
        } finally {
            logProductLine();
            emitProductLineMetrics();
        }
    }

    // @Override
    // public void recordStageBegin(int itemId, UUID productLineId,
    // ProductionChainStageEnum stage) {
    // productionCounters.get(productLineId).stageCounters
    // .get(stage).incrementAndGet();
    // }

    @Override
    public void recordStageEnd(int itemId, UUID productLineId,
            ProductionChainStageEnum stage) {
        productionCounters.get(productLineId).stageCounters.get(stage)
                .decrementAndGet();
        productionCounters.get(productLineId).waitingForValidationCounters
                .get(stage).incrementAndGet();
        logProductLine();
        emitProductLineMetrics();
    }

    @Override
    public void recordStageSuccess(int itemId, UUID productLineId,
            ProductionChainStageEnum stage) {
        try {
            productionCounters.get(productLineId).waitingForValidationCounters
                    .get(stage).decrementAndGet();
            if (stage == ProductionChainStageEnum.PACKAGING) {
                productionCounters.get(productLineId).completed
                        .incrementAndGet();
                return;
            }

            ProductionChainStageEnum nextStage = ProductionChainStageEnum
                    .values()[stage.ordinal() + 1];
            productionCounters.get(productLineId).stageCounters.get(nextStage)
                    .incrementAndGet();
        } finally {
            logProductLine();
            emitProductLineMetrics();
        }
    }

    @Override
    public void recordStageFailure(int itemId, UUID productLineId,
            ProductionChainStageEnum stage) {
        productionCounters.get(productLineId).waitingForValidationCounters
                .get(stage).decrementAndGet();
        productionCounters.get(productLineId).discarded.incrementAndGet();
        logProductLine();
        emitProductLineMetrics();
    }

    void logProductLine() {
        if (LOGGER.isDebugEnabled())
            try {
                String json = MAPPER.writerWithDefaultPrettyPrinter()
                        .writeValueAsString(productionCounters);
                LOGGER.info("Production summary:\n\n{}", json);
            } catch (JsonProcessingException e) {
                LOGGER.error(
                        "An error occurred printing the production summary.",
                        e);
            }
    }

    void emitProductLineMetrics() {
        LOGGER.debug("Emitting production summary metrics");
        try {
            String metricsPayload = MAPPER.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(productionCounters);

            producer.send(queue, metricsPayload);
        } catch (Exception e) {
            LOGGER.error("GENERIC ERROR", e);
        }

    }

}
