package io.qiot.manufacturing.edge.machinery.domain.event.chain;

import io.qiot.manufacturing.all.commons.domain.production.ItemDTO;
import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * @author andreabattaglia
 *
 */
@RegisterForReflection
public class StageCompletedEvent {
    public ItemDTO item;
}
