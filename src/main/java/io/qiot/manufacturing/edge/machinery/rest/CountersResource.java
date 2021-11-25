/**
 * 
 */
package io.qiot.manufacturing.edge.machinery.rest;

import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;

import io.qiot.manufacturing.edge.machinery.domain.ProductionCountersDTO;
import io.qiot.manufacturing.edge.machinery.service.production.CountersService;

/**
 * @author andreabattaglia
 *
 */
@Path("/counters")
public class CountersResource {

    @Inject
    Logger LOGGER;
    @Inject
    CountersService countersService;
    
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<UUID, ProductionCountersDTO> getFactoryId() {
        return countersService.getCounters();
    }
}
