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

    private final Map<UUID, ProductionCountersDTO> productionCounters;

    public CountersServiceImpl() {

        productionCounters = new TreeMap<UUID, ProductionCountersDTO>();
    }
    
    @Override
    public Map<UUID, ProductionCountersDTO> getCounters(){
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
        }
    }

    @Override
    public void recordStageFailure(int itemId, UUID productLineId,
            ProductionChainStageEnum stage) {
        productionCounters.get(productLineId).waitingForValidationCounters
                .get(stage).decrementAndGet();
        productionCounters.get(productLineId).discarded.incrementAndGet();
        logProductLine();
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
}
