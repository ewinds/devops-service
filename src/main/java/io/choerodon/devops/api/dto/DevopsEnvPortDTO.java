package io.choerodon.devops.api.dto;

/**
 * @author lizongwei
 * @date 2019/7/2
 */
public class DevopsEnvPortDTO {

    private String resourceName;

    private String portName;

    private Double portValue;

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getPortName() {
        return portName;
    }

    public void setPortName(String portName) {
        this.portName = portName;
    }

    public Double getPortValue() {
        return portValue;
    }

    public void setPortValue(Double portValue) {
        this.portValue = portValue;
    }
}
