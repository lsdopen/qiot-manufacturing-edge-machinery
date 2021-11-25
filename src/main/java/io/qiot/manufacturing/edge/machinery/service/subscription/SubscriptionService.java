package io.qiot.manufacturing.edge.machinery.service.subscription;

import java.util.UUID;

import io.qiot.manufacturing.all.commons.exception.SubscriptionException;

public interface SubscriptionService {

    UUID subscribe(String serial, String name, String ksPassword)
            throws SubscriptionException;

    void resetFactoryData();

}