/**
 * 
 */
package io.qiot.manufacturing.edge.machinery.service.machinery;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.qiot.manufacturing.all.commons.exception.DataValidationException;
import io.qiot.manufacturing.all.commons.exception.SubscriptionException;
import io.qiot.manufacturing.edge.machinery.domain.MachineryDataDTO;
import io.qiot.manufacturing.edge.machinery.service.subscription.SubscriptionService;

/**
 * @author andreabattaglia
 *
 */
@ApplicationScoped
public class MachineryServiceImpl implements MachineryService {

    @Inject
    Logger LOGGER;

    @ConfigProperty(name = "qiot.datafile.path")
    String dataFilePathString;

    @Inject
    ObjectMapper MAPPER;

    @Inject
    SubscriptionService subscriptionService;

    @ConfigProperty(name = "qiot.data.reset")
    boolean DO_RESET;

    @ConfigProperty(name = "qiot.machinery.serial")
    String MACHINERY_SERIAL;
    @ConfigProperty(name = "qiot.machinery.name")
    String MACHINERY_NAME;

    @ConfigProperty(name = "qiot.runtime.ks.password")
    String ksPassword;
    @ConfigProperty(name = "qiot.runtime.ts.password")
    String tsPassword;

    private Path dataFilePath;
    private MachineryDataDTO machineryData;

    @PostConstruct
    void init() {
        dataFilePath = Paths.get(dataFilePathString);
        if (DO_RESET)
            resetFactoryData();
    }

    private void resetFactoryData() {
        try {
            if (!Files.deleteIfExists(dataFilePath))
                LOGGER.debug(
                        "Machinery data file does not exists. Nothing to delete");
        } catch (IOException e) {
            LOGGER.error("Failed resetting Factory data: {}",
                    dataFilePathString);
        }
        subscriptionService.resetFactoryData();
    }

    public MachineryDataDTO checkRegistration()
            throws DataValidationException, SubscriptionException {
        Path dataFilePath = Paths.get(dataFilePathString);
        if (Files.exists(dataFilePath)) {
            LOGGER.info(
                    "Device is already registered. Loading data from persistent volume...");
            try {
                String datafileContent = Files.readString(dataFilePath);
                machineryData = MAPPER.readValue(datafileContent,
                        MachineryDataDTO.class);
            } catch (Exception e) {
                LOGGER.error(
                        "An error occurred loading the machinery data file.",
                        e);
                throw new DataValidationException(e);
            }
            LOGGER.info("Data loaded successfully: {}", machineryData);
        } else {
            LOGGER.info(
                    "Device is not registered. Stepping through the registration process...");

            machineryData = new MachineryDataDTO();
            machineryData.serial = MACHINERY_SERIAL;
            machineryData.name = MACHINERY_NAME;
            UUID machineryId = null;
            // if (ProfileManager.getActiveProfile()
            // .equals(LaunchMode.DEVELOPMENT.getDefaultProfile())) {
            // machineryId = UUID.randomUUID().toString();
            // }
            // else
            machineryId = subscriptionService.subscribe(MACHINERY_SERIAL,
                    MACHINERY_NAME, ksPassword);

            machineryData.id = machineryId.toString();

            LOGGER.info("Received machinery ID: {}", machineryId);
            try {
                Files.createFile(dataFilePath);

                String factoryDataString = MAPPER
                        .writeValueAsString(machineryData);
                Files.writeString(dataFilePath, factoryDataString);
            } catch (Exception e) {
                LOGGER.error("An error occurred saving data to volume.", e);
                throw new DataValidationException(e);
            }

            LOGGER.info("Data Created successfully: {}", machineryData);
        }
        return machineryData;
    }

    @Override
    public String getMachineryId() {
        return machineryData.id;
    }

    @Override
    public String getMachinerySerial() {
        return machineryData.serial;
    }

    @Override
    public String getMachineryName() {
        return machineryData.name;
    }

    public String getTrustStorePassword() {
        return tsPassword;
    }

    public String getKeyStorePassword() {
        return ksPassword;
    }

}
