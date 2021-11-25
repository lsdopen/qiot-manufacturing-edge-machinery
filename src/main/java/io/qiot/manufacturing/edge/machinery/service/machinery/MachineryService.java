package io.qiot.manufacturing.edge.machinery.service.machinery;

import io.qiot.manufacturing.all.commons.exception.DataValidationException;
import io.qiot.manufacturing.all.commons.exception.SubscriptionException;
import io.qiot.manufacturing.edge.machinery.domain.MachineryDataDTO;

/**
 * @author andreabattaglia
 *
 */
public interface MachineryService {

    MachineryDataDTO checkRegistration()
            throws DataValidationException, SubscriptionException;

    String getMachineryId();

    String getMachinerySerial();

    String getMachineryName();

}
