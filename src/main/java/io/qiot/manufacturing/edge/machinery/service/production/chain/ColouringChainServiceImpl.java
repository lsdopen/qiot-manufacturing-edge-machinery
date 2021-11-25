/**
 * 
 */
package io.qiot.manufacturing.edge.machinery.service.production.chain;

import java.util.PrimitiveIterator;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.slf4j.Logger;

import io.qiot.manufacturing.all.commons.domain.production.ColorMetricsDTO;
import io.qiot.manufacturing.all.commons.domain.production.ItemDTO;
import io.qiot.manufacturing.all.commons.domain.production.ProductionChainStageEnum;
import io.qiot.manufacturing.all.commons.domain.productline.ProductLineDTO;
import io.qiot.manufacturing.edge.machinery.domain.event.chain.StageCompletedEvent;
import io.quarkus.scheduler.Scheduled;

/**
 * @author andreabattaglia
 *
 */
@ApplicationScoped
//@Typed(ColouringChainServiceImpl.class)
//@ColouringChainQualifier
public class ColouringChainServiceImpl extends AbstractChainService {

    @Inject
    Logger LOGGER;
    
    @Inject
    Event<StageCompletedEvent> event;
    
    private PrimitiveIterator.OfInt redRandomNumberGenerator;
    private PrimitiveIterator.OfInt greenRandomNumberGenerator;
    private PrimitiveIterator.OfInt blueRandomNumberGenerator;
    
    @PostConstruct
    void init() {
        super.init();
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    @Override
    protected ProductionChainStageEnum getStage() {
        return ProductionChainStageEnum.COLORING;
    }

    @Override
    protected Event<StageCompletedEvent> getEvent() {
        return event;
    }

    @Scheduled(every = "2s")
    @Override
    public void simulate() {
        super.doSimulate();
    }

    @Override
    protected void initRandomNumberGenerators(ProductLineDTO productLine) {
        redRandomNumberGenerator=randomGeneratorProducer.intRandomNumberGenerator(productLine.color.redMin, productLine.color.redMax); 
        greenRandomNumberGenerator= randomGeneratorProducer.intRandomNumberGenerator(productLine.color.blueMin, productLine.color.blueMax); 
        blueRandomNumberGenerator= randomGeneratorProducer.intRandomNumberGenerator(productLine.color.greenMin, productLine.color.greenMax); 
    }

    @Override
    protected void generate(ItemDTO item) {
        ColorMetricsDTO metrics = new ColorMetricsDTO();
        metrics.red = redRandomNumberGenerator.nextInt();
        metrics.green = greenRandomNumberGenerator.nextInt();
        metrics.blue = blueRandomNumberGenerator.nextInt();
        item.colorMetrics=metrics;
    }
}
