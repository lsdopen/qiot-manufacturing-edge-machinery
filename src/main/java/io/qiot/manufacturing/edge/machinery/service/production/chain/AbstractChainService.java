package io.qiot.manufacturing.edge.machinery.service.production.chain;

import java.util.Objects;
import java.util.PrimitiveIterator;
import java.util.UUID;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.slf4j.Logger;

import io.qiot.manufacturing.all.commons.domain.production.ItemDTO;
import io.qiot.manufacturing.all.commons.domain.production.ProductionChainStageEnum;
import io.qiot.manufacturing.all.commons.domain.productline.ProductLineDTO;
import io.qiot.manufacturing.edge.machinery.domain.event.chain.StageCompletedEvent;
import io.qiot.manufacturing.edge.machinery.service.production.ConveyorBeltService;
import io.qiot.manufacturing.edge.machinery.service.productline.ProductLineService;
import io.qiot.ubi.all.utilities.producer.RandomGeneratorProducer;

/**
 * @author andreabattaglia
 *
 */
public abstract class AbstractChainService implements ChainService {

    @Inject
    protected ConveyorBeltService conveyorBeltService;

    @Inject
    protected ProductLineService productLineService;

    @Inject
    protected RandomGeneratorProducer randomGeneratorProducer;

    private PrimitiveIterator.OfLong sleepRandomNumberGenerator;

    protected UUID productLineId;

    void init() {
        sleepRandomNumberGenerator = randomGeneratorProducer
                .longRandomNumberGenerator(1000, 3000);
    }

    public void doSimulate() {
        ItemDTO item = poll(getStage());
        if (Objects.isNull(item)) {
            getLogger().debug("No items available in conveyor belt for stage {}",
                    getStage());
            return;
        }
        getLogger().debug(
                "{} process started for Item #{} and Product Line #{}.",
                getStage(), item.id, item.productLineId);
        checkProductLineId(item.productLineId);
        // if (Objects.isNull(item.productLineId)) {
        // getLogger().debug("No Product Line available.");
        // return;
        // }
        item.stage = getStage();
        try {
            long sleepTime = sleepRandomNumberGenerator.nextLong();
            getLogger().debug("Sleeping for {} millis.", sleepTime);
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        generate(item);
        getLogger().debug(
                "{} process completed for Item #{} and Product Line #{}.",
                getStage(), item.id, item.productLineId);
        notify(item);
    }

    private void notify(ItemDTO item) {
        StageCompletedEvent event = new StageCompletedEvent();
        event.item = item;
        getEvent().fire(event);
    }

    private void checkProductLineId(UUID productLineId) {
        if (Objects.isNull(this.productLineId)
                || (!productLineId.equals(this.productLineId))) {
            ProductLineDTO productLine = productLineService
                    .getProductLine(productLineId);
            initRandomNumberGenerators(productLine);
        }
    }

    protected abstract void initRandomNumberGenerators(
            ProductLineDTO productLine);

    protected abstract ProductionChainStageEnum getStage();

    protected abstract void generate(ItemDTO item);

    protected abstract Event<StageCompletedEvent> getEvent();

    private ItemDTO poll(ProductionChainStageEnum stage) {
        ItemDTO item = conveyorBeltService.nextItemInQueue(stage);
        return item;
    }

    protected abstract Logger getLogger();

}
