package io.qiot.manufacturing.edge.machinery.service.production;

import java.util.Map;
import java.util.UUID;

import io.qiot.manufacturing.all.commons.domain.production.ProductionChainStageEnum;
import io.qiot.manufacturing.edge.machinery.domain.ProductionCountersDTO;

/**
 * @author andreabattaglia
 *
 */
public interface CountersService {
    int recordNewItem(UUID productLineId);

    void recordStageEnd(int itemId, UUID productLineId, ProductionChainStageEnum stage);

    void recordStageSuccess(int itemId, UUID productLineId, ProductionChainStageEnum stage);

    void recordStageFailure(int itemId, UUID productLineId, ProductionChainStageEnum stage);

    Map<UUID, ProductionCountersDTO> getCounters();
}
