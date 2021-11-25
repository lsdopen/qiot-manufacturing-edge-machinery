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

import io.qiot.manufacturing.all.commons.domain.production.ItemDTO;
import io.qiot.manufacturing.all.commons.domain.production.PrintingMetricsDTO;
import io.qiot.manufacturing.all.commons.domain.production.ProductionChainStageEnum;
import io.qiot.manufacturing.all.commons.domain.productline.ProductLineDTO;
import io.qiot.manufacturing.edge.machinery.domain.event.chain.StageCompletedEvent;
import io.quarkus.scheduler.Scheduled;

/**
 * @author andreabattaglia
 *
 */
@ApplicationScoped
//@Typed(PrintingChainServiceImpl.class)
//@PrintingChainQualifier
public class PrintingChainServiceImpl extends AbstractChainService {

    @Inject
    Logger LOGGER;
    
    @Inject
    Event<StageCompletedEvent> event;
    
    private PrimitiveIterator.OfDouble printingRandomNumberGenerator;
    
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
        return ProductionChainStageEnum.PRINTING;
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
        printingRandomNumberGenerator=randomGeneratorProducer.doubleRandomNumberGenerator(productLine.print.min, productLine.print.max); 
    }

    @Override
    protected void generate(ItemDTO item) {
        PrintingMetricsDTO metrics=new PrintingMetricsDTO();
        metrics.printing=printingRandomNumberGenerator.nextDouble();
        item.printingMetrics=metrics;
    }
}
