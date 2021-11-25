/**
 * 
 */
package io.qiot.manufacturing.edge.machinery.service.productline;

import java.util.Objects;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.JMSProducer;
import javax.jms.Queue;
import javax.jms.Session;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author andreabattaglia
 *
 */
@ApplicationScoped
public class LatestProductLineRequestMessageProducer {

    @Inject
    Logger LOGGER;

    // @Inject
    // ConnectionFactory connectionFactory;

    @Inject
    ActiveMQConnectionFactory connectionFactory;

    @Inject
    ObjectMapper MAPPER;

    @ConfigProperty(name = "qiot.productline.request.queue-prefix")
    String latestProductLineRequestQueueName;

    private JMSContext context;

    private JMSProducer producer;

    private Queue queue;

    @PostConstruct
    void init() {
        LOGGER.debug(
                "Bootstrapping latest product line request event producer...");
        doInit();

        LOGGER.debug("Bootstrap completed");

    }

    private void doInit() {
        if (Objects.nonNull(context))
            context.close();
        context = connectionFactory.createContext(Session.AUTO_ACKNOWLEDGE);

        producer = context.createProducer();

        queue = context.createQueue(latestProductLineRequestQueueName);
    }

    void requestLatestProductLine(String machineryId) {
        LOGGER.debug(
                "Sending out a request for the latest product line available");
        try {
            String messagePayload = machineryId;

            producer.send(queue, messagePayload);
        } catch (Exception e) {
            LOGGER.error("GENERIC ERROR", e);
        }

    }
}
