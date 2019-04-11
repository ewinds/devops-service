package io.choerodon.devops.infra.common.util.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  20:40 2019/4/9
 * Description:
 */
public enum WorkFlowStatus {
    RUNNING,
    PENDINGCHECK,
    SUCCESS,
    FAILED,
    STOP,
    DELETED;

    private static HashMap<String, WorkFlowStatus> valuesMap = new HashMap<>(6);

    static {
        WorkFlowStatus[] var0 = values();

        for (WorkFlowStatus status : var0) {
            valuesMap.put(status.toValue(), status);
        }

    }

    WorkFlowStatus() {
    }

    @JsonCreator
    public static WorkFlowStatus forValue(String value) {
        return valuesMap.get(value);
    }

    @JsonValue
    public String toValue() {
        return this.name().toLowerCase();
    }

    @Override
    public String toString() {
        return this.name().toLowerCase();
    }
}
