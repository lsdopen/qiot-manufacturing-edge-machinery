package io.qiot.manufacturing.edge.machinery.service.productline;

import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.slf4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.qiot.manufacturing.all.commons.domain.cdi.BootstrapCompletedEventDTO;
import io.qiot.manufacturing.all.commons.domain.productline.ProductLineDTO;
import io.qiot.manufacturing.edge.machinery.domain.event.productline.ProductLineChangedEventDTO;
import io.qiot.manufacturing.edge.machinery.service.machinery.MachineryService;
import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * @author andreabattaglia
 *
 */
@ApplicationScoped
@RegisterForReflection(targets={ ProductLineDTO.class})
class ProductLineServiceImpl implements ProductLineService {

    private ReadWriteLock readWriteLock;
    private final Lock readLock;
    private final Lock writeLock;

    private ProductLineDTO currentProductLine;
    private Map<UUID, ProductLineDTO> productLines;

    @Inject
    Event<Void> productLineEvent;

    @Inject
    Logger LOGGER;

    @Inject
    ObjectMapper MAPPER;

    @Inject
    MachineryService machineryService;

    @Inject
    LatestProductLineRequestMessageProducer latestProductLineRequestMessageProducer;

    ProductLineServiceImpl() {
        productLines = new TreeMap<UUID, ProductLineDTO>();
        this.readWriteLock = new ReentrantReadWriteLock();
        this.readLock = readWriteLock.readLock();
        this.writeLock = readWriteLock.writeLock();
    }

    void onStart(@Observes BootstrapCompletedEventDTO event) {
        latestProductLineRequestMessageProducer
                .requestLatestProductLine(machineryService.getMachineryId());
    }

    void newProductLine(@Observes ProductLineChangedEventDTO event)
            throws JsonProcessingException {
        if(Objects.isNull(event.productLine))
            return;
        LOGGER.info("Received notification about a new Product Line:\n\n{}",
                event.productLine.id);
        ProductLineDTO productLine = event.productLine;
        handleNewProductLine(productLine);
    }

    private void handleNewProductLine(ProductLineDTO productLine) {
        writeLock.lock();
        try {
            currentProductLine = productLine;
            productLines.put(productLine.id, productLine);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean hasProductLine() {
        return currentProductLine != null;
    }

    @Override
    public ProductLineDTO getCurrentProductLine() {
        readLock.lock();
        try {
            return currentProductLine;
        } finally {
            readLock.unlock();
        }

    }

    @Override
    public ProductLineDTO getProductLine(UUID productLineId) {
        readLock.lock();
        try {
            return productLines.get(productLineId);
        } finally {
            readLock.unlock();
        }
    }
}