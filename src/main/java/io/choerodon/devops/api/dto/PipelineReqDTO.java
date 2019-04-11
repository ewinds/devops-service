package io.choerodon.devops.api.dto;

import java.util.Date;
import java.util.List;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  16:14 2019/4/8
 * Description:
 */
public class PipelineReqDTO {
    private Long id;
    private String name;
    private String triggerType;
    private List<PipelineUserRelDTO> pipelineUserRelDTOS;
    private List<PipelineStageDTO> pipelineStageDTOS;
    private Long projectId;
    private Long objectVersionNumber;
    private Date lastUpdateDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(String triggerType) {
        this.triggerType = triggerType;
    }

    public List<PipelineUserRelDTO> getPipelineUserRelDTOS() {
        return pipelineUserRelDTOS;
    }

    public void setPipelineUserRelDTOS(List<PipelineUserRelDTO> pipelineUserRelDTOS) {
        this.pipelineUserRelDTOS = pipelineUserRelDTOS;
    }

    public List<PipelineStageDTO> getPipelineStageDTOS() {
        return pipelineStageDTOS;
    }

    public void setPipelineStageDTOS(List<PipelineStageDTO> pipelineStageDTOS) {
        this.pipelineStageDTOS = pipelineStageDTOS;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }
}
