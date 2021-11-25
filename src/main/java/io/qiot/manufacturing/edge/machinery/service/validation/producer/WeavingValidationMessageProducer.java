package io.qiot.manufacturing.edge.machinery.service.validation.producer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.qiot.manufacturing.all.commons.domain.production.ProductionChainStageEnum;
import io.qiot.manufacturing.factory.commons.domain.productionvalidation.WeavingValidationRequestEventDTO;

/**
 * @author andreabattaglia
 *
 */
@ApplicationScoped
public class WeavingValidationMessageProducer
        extends AbstractValidationMessageProducer {

    @ConfigProperty(name = "qiot.production.chain.validation.weaving.queue")
    String validationQueueName;

    @Inject
    Logger LOGGER;
    
    @Inject
    ObjectMapper MAPPER;

    @PostConstruct
    void init() {
        super.doInit();
    }

    @PreDestroy
    void destroy() {
        context.close();
    }

    public void requestValidation(
            @Observes WeavingValidationRequestEventDTO event) {
//        String payload;
//        try {
//            payload = MAPPER.writeValueAsString(event);
//        getLogger().debug("Message payload: {}", payload);
//        } catch (JsonProcessingException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
        super.doRequestValidation(event);
    }

    @Override
    protected String getValidationQueueName() {
        return validationQueueName;
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    @Override
    protected ProductionChainStageEnum getStage() {
        return ProductionChainStageEnum.WEAVING;
    }
}