package io.choerodon.devops.infra.dto;

import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;
import javax.persistence.*;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 *
 * @author wanghao
 * @Date 2020/4/2 17:00
 */
@ModifyAudit
@VersionAudit
@Table(name = "cicd_stage")
public class CiCdStageDTO extends AuditDomain {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ApiModelProperty("阶段名称")
    private String name;
    @ApiModelProperty("阶段所属流水线id")
    private Long ciPipelineId;
    @ApiModelProperty("阶段顺序")
    private Long sequence;
    @ApiModelProperty("阶段类型")
    private String type;
    @ApiModelProperty("触发方式")
    private String triggerType;
    @ApiModelProperty("是否并行")
    @Column(name = "is_parallel")
    private Boolean parallel;
    @ApiModelProperty("项目ID")
    private Long projectId;

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

    public Long getCiPipelineId() {
        return ciPipelineId;
    }

    public void setCiPipelineId(Long ciPipelineId) {
        this.ciPipelineId = ciPipelineId;
    }

    public Long getSequence() {
        return sequence;
    }

    public void setSequence(Long sequence) {
        this.sequence = sequence;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(String triggerType) {
        this.triggerType = triggerType;
    }

    public Boolean getParallel() {
        return parallel;
    }

    public void setParallel(Boolean parallel) {
        this.parallel = parallel;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    @Override
    public String toString() {
        return "CiCdStageDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", ciPipelineId=" + ciPipelineId +
                ", sequence=" + sequence +
                ", type='" + type + '\'' +
                ", triggerType='" + triggerType + '\'' +
                ", parallel=" + parallel +
                ", projectId=" + projectId +
                '}';
    }

}