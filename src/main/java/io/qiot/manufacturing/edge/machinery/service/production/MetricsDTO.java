package io.qiot.manufacturing.edge.machinery.service.production;

import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.UUID;

import javax.inject.Inject;

import io.qiot.manufacturing.edge.machinery.domain.ProductionCountersDTO;
import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * @author lsdopen
 *
 */
@RegisterForReflection
public class MetricsDTO {
    private String machineSerial;
    private String machineName;
    private final Map<UUID, ProductionCountersDTO> productionCounters;

    public MetricsDTO(String machineSerial, String machineName) {
        this.machineSerial = machineSerial;
        this.machineName = machineName;
        productionCounters = new TreeMap<UUID, ProductionCountersDTO>();
    }

    /**
     * @return the productLineId
     */
    public String getMachineSerial() {
        return machineSerial;
    }

    /**
     * @return the productLineId
     */
    public String getMachineName() {
        return machineName;
    }

    public Map<UUID, ProductionCountersDTO> getProductionCounters() {
        return productionCounters;
    }

    @Override
    public int hashCode() {
        return Objects.hash(machineSerial);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MetricsDTO other = (MetricsDTO) obj;
        return Objects.equals(machineSerial, other.machineSerial);
    }

}
