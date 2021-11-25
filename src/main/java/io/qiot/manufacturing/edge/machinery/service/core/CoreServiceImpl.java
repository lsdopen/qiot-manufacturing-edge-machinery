/**
 * 
 */
package io.qiot.manufacturing.edge.machinery.service.core;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.jboss.resteasy.plugins.providers.DocumentProvider;
import org.jboss.resteasy.plugins.providers.FormUrlEncodedProvider;
import org.jboss.resteasy.plugins.providers.IIOImageProvider;
import org.jboss.resteasy.plugins.providers.JaxrsFormProvider;
import org.jboss.resteasy.plugins.providers.SourceProvider;
import org.jboss.resteasy.plugins.providers.sse.SseEventProvider;
import org.slf4j.Logger;

import io.qiot.manufacturing.all.commons.domain.cdi.BootstrapCompletedEventDTO;
import io.qiot.manufacturing.edge.machinery.service.machinery.MachineryService;
import io.qiot.manufacturing.edge.machinery.service.production.ProductionChainService;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.quarkus.scheduler.Scheduler;

/**
 * @author andreabattaglia
 *
 */
// @Startup(1)
@ApplicationScoped
@RegisterForReflection(targets = { IIOImageProvider.class,
        SseEventProvider.class, FormUrlEncodedProvider.class,
        SourceProvider.class, DocumentProvider.class, JaxrsFormProvider.class })
public class CoreServiceImpl implements CoreService {

    @Inject
    Logger LOGGER;

    @Inject
    Scheduler scheduler;

    @Inject
    MachineryService machineryService;

    @Inject
    ProductionChainService productionChainService;

    @Inject
    Event<BootstrapCompletedEventDTO> event;

    void onStart(@Observes StartupEvent ev) throws Exception {
        LOGGER.debug("The application is starting...{}");
        scheduler.pause();
        machineryService.checkRegistration();
        event.fire(new BootstrapCompletedEventDTO());
        scheduler.resume();
    }

    void ping() {
        scheduler.pause();
        scheduler.pause("myIdentity");
        if (scheduler.isRunning()) {
            throw new IllegalStateException("This should never happen!");
        }
        scheduler.resume("myIdentity");
        scheduler.resume();
    }
}
