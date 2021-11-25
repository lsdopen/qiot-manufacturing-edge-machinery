package io.qiot.manufacturing.edge.machinery.domain.event.productline;

import io.qiot.manufacturing.all.commons.domain.productline.ProductLineDTO;
import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * @author andreabattaglia
 *
 */
@RegisterForReflection
public class ProductLineChangedEventDTO {
    public ProductLineDTO productLine;
}
