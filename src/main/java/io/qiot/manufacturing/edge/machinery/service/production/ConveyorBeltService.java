package io.qiot.manufacturing.edge.machinery.service.production;

import java.util.UUID;

import io.qiot.manufacturing.all.commons.domain.production.ItemDTO;
import io.qiot.manufacturing.all.commons.domain.production.ProductionChainStageEnum;

/**
 * @author andreabattaglia
 *
 */
public interface ConveyorBeltService {
    void createNewItem(UUID productLineId, int itemId);

    ItemDTO nextItemInQueue(ProductionChainStageEnum stage);

    void moveToWaitingQueue(ItemDTO item);

    ItemDTO dropItem(int itemId, ProductionChainStageEnum stage);

    ItemDTO moveToNextStage(int itemId, ProductionChainStageEnum stage);

    boolean isValidItem(UUID productLineId, int itemId, ProductionChainStageEnum stage);
}
