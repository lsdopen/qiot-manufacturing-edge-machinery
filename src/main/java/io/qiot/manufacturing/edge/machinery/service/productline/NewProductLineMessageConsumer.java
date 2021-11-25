package io.qiot.manufacturing.edge.machinery.service.productline;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.Topic;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.qiot.manufacturing.all.commons.domain.cdi.BootstrapCompletedEventDTO;
import io.qiot.manufacturing.all.commons.domain.productline.ProductLineDTO;
import io.qiot.manufacturing.edge.machinery.domain.event.productline.ProductLineChangedEventDTO;
import io.qiot.manufacturing.edge.machinery.service.machinery.MachineryService;

/**
 * @author andreabattaglia
 *
 */
@ApplicationScoped
public class NewProductLineMessageConsumer implements Runnable {

    @Inject
    Logger LOGGER;

    @Inject
    ObjectMapper MAPPER;

    @Inject
    ActiveMQConnectionFactory connectionFactory;

    @Inject
    MachineryService machineryService;

    @ConfigProperty(name = "qiot.productline.topic.name")
    String topicName;

    @Inject
    Event<ProductLineChangedEventDTO> prodictLineChangedEvent;

    private JMSContext context;

    private JMSConsumer consumer;

    private Topic topic;

    private final ExecutorService scheduler = Executors
            .newSingleThreadExecutor();

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
        
        context.setClientID(machineryService.getMachineryId());

        topic = context.createTopic(topicName);

        consumer = context.createDurableConsumer(topic,
                machineryService.getMachineryId());
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
                String messagePayload = message.getBody(String.class);
                ProductLineDTO productLine = MAPPER.readValue(messagePayload,
                        ProductLineDTO.class);
                LOGGER.debug(
                        "Received new PRODUCTLINE from the Factory Controller: \n {}",
                        productLine);
                ProductLineChangedEventDTO eventDTO = new ProductLineChangedEventDTO();
                eventDTO.productLine = productLine;
                prodictLineChangedEvent.fire(eventDTO);
            } catch (JMSException e) {
                LOGGER.error(
                        "The messaging client returned an error: {} and will be restarted.",
                        e);
                initSubscriber();
            } catch (JsonProcessingException e) {
                LOGGER.error(
                        "The message payload is malformed and the validation request will not be sent: {}",
                        e);
            } catch (Exception e) {
                LOGGER.error("GENERIC ERROR", e);
            }
        }
    }
}