package io.qiot.manufacturing.edge.machinery.domain.event.chain;

import io.qiot.manufacturing.factory.commons.domain.productionvalidation.AbstractProductionChainEventDTO;
import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * @author andreabattaglia
 *
 */
@RegisterForReflection
public class ValidationFailedEvent extends AbstractProductionChainEventDTO {
}
