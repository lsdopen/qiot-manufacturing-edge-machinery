package io.qiot.manufacturing.edge.machinery.service.validation;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import io.qiot.manufacturing.all.commons.domain.production.ItemDTO;
import io.qiot.manufacturing.edge.machinery.service.machinery.MachineryService;
import io.qiot.manufacturing.factory.commons.domain.productionvalidation.AbstractValidationRequestEventDTO;
import io.qiot.manufacturing.factory.commons.domain.productionvalidation.ColoringValidationRequestEventDTO;
import io.qiot.manufacturing.factory.commons.domain.productionvalidation.PackagingValidationRequestEventDTO;
import io.qiot.manufacturing.factory.commons.domain.productionvalidation.PrintingValidationRequestEventDTO;
import io.qiot.manufacturing.factory.commons.domain.productionvalidation.WeavingValidationRequestEventDTO;

/**
 * @author andreabattaglia
 *
 */
@ApplicationScoped
public class ValidationServiceImpl implements ValidationService {

    @Inject
    MachineryService machineryService;

    @Inject
    Event<WeavingValidationRequestEventDTO> weavingValidationEvent;
    @Inject
    Event<ColoringValidationRequestEventDTO> coloringValidationEvent;
    @Inject
    Event<PrintingValidationRequestEventDTO> printingValidationEvent;
    @Inject
    Event<PackagingValidationRequestEventDTO> packagingValidationEvent;

    @Override
    public void validateItem(ItemDTO item) {
        switch (item.stage) {
        case WEAVING:
            WeavingValidationRequestEventDTO wev = new WeavingValidationRequestEventDTO();
            abstractPopulate(wev, item);
            wev.data = item.sizeMetrics;
            weavingValidationEvent.fire(wev);
            return;
        case COLORING:
            ColoringValidationRequestEventDTO cev = new ColoringValidationRequestEventDTO();
            abstractPopulate(cev, item);
            cev.data = item.colorMetrics;
            coloringValidationEvent.fire(cev);
            return;
        case PRINTING:
            PrintingValidationRequestEventDTO prev = new PrintingValidationRequestEventDTO();
            abstractPopulate(prev, item);
            prev.data = item.printingMetrics;
            printingValidationEvent.fire(prev);
            return;
        case PACKAGING:
            PackagingValidationRequestEventDTO paev = new PackagingValidationRequestEventDTO();
            abstractPopulate(paev, item);
            paev.data = item.packagingMetrics;
            packagingValidationEvent.fire(paev);
            return;
        default:
            throw new RuntimeException("Not yet implemented");
        }
    }

    private void abstractPopulate(AbstractValidationRequestEventDTO ev,
            ItemDTO item) {
        ev.machineryId = machineryService.getMachineryId();
        ev.itemId = item.id;
        ev.productLineId = item.productLineId;
        ev.stage = item.stage;
    }

}
