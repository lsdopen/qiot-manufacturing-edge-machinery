package io.qiot.manufacturing.edge.machinery.service.productline;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.jms.ConnectionFactory;
import javax.jms.IllegalStateRuntimeException;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.Session;

import org.slf4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.qiot.manufacturing.all.commons.domain.cdi.BootstrapCompletedEventDTO;
import io.qiot.manufacturing.all.commons.domain.productline.ProductLineDTO;
import io.qiot.manufacturing.edge.machinery.domain.event.productline.ProductLineChangedEventDTO;
import io.qiot.manufacturing.edge.machinery.service.machinery.MachineryService;
import io.qiot.manufacturing.factory.commons.util.producer.ProductLineReplyToQueueNameProducer;

/**
 * @author andreabattaglia
 *
 */
@ApplicationScoped
public class LatestProductLineMessageConsumer implements Runnable {

    @Inject
    Logger LOGGER;

    @Inject
    ObjectMapper MAPPER;

    @Inject
    ConnectionFactory connectionFactory;

    @Inject
    MachineryService machineryService;

    @Inject
    ProductLineReplyToQueueNameProducer productLineReplyToQueueNameProducer;

    @Inject
    Event<ProductLineChangedEventDTO> prodictLineChangedEvent;

    private JMSContext context;

    private JMSConsumer consumer;

    private String replyToQueueName;

    private Queue replyToQueue;

    private final ExecutorService scheduler = Executors.newSingleThreadExecutor();

    void init(@Observes BootstrapCompletedEventDTO event) {
        LOGGER.debug("Bootstrapping new product line durable subscriber...");
        initSubscriber();

        scheduler.submit(this);
        LOGGER.debug("Bootstrap completed");
    }

    private void initSubscriber() {
        if (Objects.nonNull(context))
            context.close();
        context = connectionFactory.createContext(Session.AUTO_ACKNOWLEDGE);

        replyToQueueName = productLineReplyToQueueNameProducer.getReplyToQueueName(machineryService.getMachineryId());

        replyToQueue = context.createQueue(replyToQueueName);

        consumer = context.createConsumer(replyToQueue);
    }

    @PreDestroy
    void destroy() {
        scheduler.shutdown();
        context.close();
    }

    @Override
    public void run() {
        while (true) {
            try {
                Message message = consumer.receive();

                LOGGER.debug("\n\nmessage. receive body: {}", message.getBody(String.class));

                String messagePayload = message.getBody(String.class);
                LOGGER.debug("\n\nmessagePayload received for latestProductLineMessageConsumer: \n {}\n\n",
                        messagePayload);
                if (Objects.isNull(messagePayload)) {
                    LOGGER.warn("Empty message payload. Discarding: {}", messagePayload);
                }
                ProductLineDTO productLine = MAPPER.readValue(messagePayload, ProductLineDTO.class);
                LOGGER.debug("Received latest PRODUCTLINE available from the Factory Controller: \n {}", productLine);
                ProductLineChangedEventDTO eventDTO = new ProductLineChangedEventDTO();
                eventDTO.productLine = productLine;
                prodictLineChangedEvent.fire(eventDTO);
            } catch (JMSException | IllegalStateRuntimeException e) {
                LOGGER.error("The messaging client returned an error: {} and will be restarted.", e);
                initSubscriber();
            } catch (JsonProcessingException e) {
                LOGGER.error("The message payload is malformed and the validation request will not be sent: {}", e);
            } catch (Exception e) {
                LOGGER.error("GENERIC ERROR", e);
            }
        }
    }
}