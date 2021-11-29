/**
 * 
 */
package io.qiot.manufacturing.edge.machinery.service.validation.producer;

import java.util.Objects;

import javax.inject.Inject;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.slf4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.qiot.manufacturing.all.commons.domain.production.ProductionChainStageEnum;
import io.qiot.manufacturing.edge.machinery.service.machinery.MachineryService;
import io.qiot.manufacturing.factory.commons.domain.productionvalidation.AbstractValidationRequestEventDTO;
import io.qiot.manufacturing.factory.commons.util.producer.ValidationReplyToQueueNameProducer;

/**
 * @author andreabattaglia
 *
 */
public abstract class AbstractValidationMessageProducer {

    // @Inject
    // ConnectionFactory connectionFactory;

    @Inject
    ConnectionFactory connectionFactory;

    @Inject
    MachineryService machineryService;

    @Inject
    ValidationReplyToQueueNameProducer replyToQueueNameProducer;

    @Inject
    ObjectMapper MAPPER;

    protected JMSContext context;

    protected JMSProducer producer;

    protected Queue queue;

    protected String replyToQueueName;

    protected Queue replyToQueue;

    protected void doInit() {
        if (Objects.nonNull(context))
            context.close();
        context = connectionFactory.createContext(Session.AUTO_ACKNOWLEDGE);

        producer = context.createProducer();

        queue = context.createQueue(getValidationQueueName());

        replyToQueueName = replyToQueueNameProducer
                .getReplyToQueueName(machineryService.getMachineryId());

        replyToQueue = context.createQueue(replyToQueueName);
        producer.setJMSReplyTo(replyToQueue);
    }

    protected void doRequestValidation(
            AbstractValidationRequestEventDTO event) {
        getLogger().debug("{} stage validation request received.", getStage());
        try {
            String payload = MAPPER.writeValueAsString(event);
            getLogger().debug("Message payload: {}", payload);
            TextMessage message = context.createTextMessage();
            message.setJMSDeliveryMode(DeliveryMode.PERSISTENT);
            message.setText(payload);
            message.setJMSReplyTo(replyToQueue);
            producer.send(queue, message);
        } catch (JMSException e) {
            getLogger().error(
                    "The messaging client returned an error: {} and will be restarted.",
                    e);
            doInit();
        } catch (JsonProcessingException e) {
            getLogger().error(
                    "The message payload is malformed and the validation request will not be sent: {}",
                    e);
        }
    }

    abstract protected String getValidationQueueName();

    abstract protected Logger getLogger();

    abstract protected ProductionChainStageEnum getStage();

}
