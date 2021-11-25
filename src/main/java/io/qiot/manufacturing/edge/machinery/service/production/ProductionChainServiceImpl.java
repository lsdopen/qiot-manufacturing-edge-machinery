/**
 * 
 */
package io.qiot.manufacturing.edge.machinery.service.production;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.slf4j.Logger;

import io.qiot.manufacturing.all.commons.domain.production.ItemDTO;
import io.qiot.manufacturing.all.commons.domain.productline.ProductLineDTO;
import io.qiot.manufacturing.edge.machinery.domain.event.chain.StageCompletedEvent;
import io.qiot.manufacturing.edge.machinery.domain.event.chain.ValidationFailedEvent;
import io.qiot.manufacturing.edge.machinery.domain.event.chain.ValidationSuccessfullEvent;
import io.qiot.manufacturing.edge.machinery.service.productline.ProductLineService;
import io.qiot.manufacturing.edge.machinery.service.validation.ValidationService;
import io.quarkus.scheduler.Scheduled;

/**
 * @author andreabattaglia
 *
 */
@ApplicationScoped
public class ProductionChainServiceImpl implements ProductionChainService {

    @Inject
    Logger LOGGER;

    /*
     * SERVICES
     */
    @Inject
    ConveyorBeltService conveyorBeltService;

    @Inject
    ProductLineService productLineService;

    @Inject
    ValidationService validationService;

    @Inject
    CountersService countersService;

    /*
     * EVENTS
     */

    @Scheduled(every = "2s")
    // @Override
    public void produce() {
        if (!productLineService.hasProductLine()) {
            LOGGER.warn("No ProductLine available");
            return;
        }

        // setup product line
        ProductLineDTO productLineDTO = productLineService
                .getCurrentProductLine();

        int itemId = countersService.recordNewItem(productLineDTO.id);

        // setup new item
        conveyorBeltService.createNewItem(productLineDTO.id, itemId);

    }

    void onStageCompleted(@Observes StageCompletedEvent event) {
        LOGGER.debug("Recording changes and notifying...");
        ItemDTO item = event.item;
        countersService.recordStageEnd(item.id, item.productLineId, item.stage);
        conveyorBeltService.moveToWaitingQueue(item);
        validationService.validateItem(item);
    };

    void onValidationSuccessfull(@Observes ValidationSuccessfullEvent event) {
        if (!conveyorBeltService.isValidItem(event.productLineId, event.itemId,
                event.stage)) {
            LOGGER.warn(
                    "Received a validation result for an item not belonging "
                            + "to the current production lifecycle. Discarding...");
            return;
        }
        ItemDTO item = conveyorBeltService.moveToNextStage(event.itemId,
                event.stage);
        countersService.recordStageSuccess(item.id, item.productLineId,
                item.stage);
    };

    void onValidationFailed(@Observes ValidationFailedEvent event) {
        ItemDTO item = conveyorBeltService.dropItem(event.itemId, event.stage);
        countersService.recordStageFailure(item.id, item.productLineId,
                item.stage);
    };

}
