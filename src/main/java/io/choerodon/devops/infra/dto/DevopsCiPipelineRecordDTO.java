package io.choerodon.devops.infra.dto;

import java.util.Date;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import io.swagger.annotations.ApiModelProperty;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * @author wanghao
 * @Date 2020/4/2 17:04
 */
@ModifyAudit
@VersionAudit
@Table(name = "devops_ci_pipeline_record")
public class DevopsCiPipelineRecordDTO extends AuditDomain {
    public static final String FIELD_ID = "id";
    public static final String FIELD_GITLAB_PIPELINE_ID = "gitlab_pipeline_id";
    public static final String FIELD_CI_PIPELINE_ID = "ci_pipeline_id";
    public static final String FIELD_GITLAB_PROJECT_ID = "gitlab_project_id";
    public static final String FIELD_COMMIT_SHA = "commit_sha";
    public static final String FIELD_GITLAB_TRIGGER_REF = "gitlab_trigger_ref";
    public static final String FIELD_STATUS = "status";
    public static final String FIELD_TRIGGER_USER_ID = "trigger_user_id";
    public static final String FIELD_CREATED_DATE = "created_date";
    public static final String FIELD_FINISHED_DATE = "finished_date";
    public static final String FIELD_DURATION_SECONDS = "duration_seconds";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ApiModelProperty("gitlab流水线记录id")
    private Long gitlabPipelineId;
    @ApiModelProperty("流水线id")
    private Long ciPipelineId;
    @ApiModelProperty("gitlabProjectId")
    private Long gitlabProjectId;
    @ApiModelProperty("gitlab commit sha")
    private String commitSha;
    @ApiModelProperty("触发分支")
    private String gitlabTriggerRef;
    @ApiModelProperty("流水线状态")
    private String status;
    @ApiModelProperty("触发用户")
    private Long triggerUserId;
    @ApiModelProperty("创建时间")
    private Date createdDate;
    @ApiModelProperty("结束时间")
    private Date finishedDate;
    @ApiModelProperty("执行耗时")
    private Long durationSeconds;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGitlabPipelineId() {
        return gitlabPipelineId;
    }

    public void setGitlabPipelineId(Long gitlabPipelineId) {
        this.gitlabPipelineId = gitlabPipelineId;
    }

    public Long getCiPipelineId() {
        return ciPipelineId;
    }

    public void setCiPipelineId(Long ciPipelineId) {
        this.ciPipelineId = ciPipelineId;
    }

    public String getCommitSha() {
        return commitSha;
    }

    public void setCommitSha(String commitSha) {
        this.commitSha = commitSha;
    }

    public String getGitlabTriggerRef() {
        return gitlabTriggerRef;
    }

    public void setGitlabTriggerRef(String gitlabTriggerRef) {
        this.gitlabTriggerRef = gitlabTriggerRef;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getTriggerUserId() {
        return triggerUserId;
    }

    public void setTriggerUserId(Long triggerUserId) {
        this.triggerUserId = triggerUserId;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getFinishedDate() {
        return finishedDate;
    }

    public void setFinishedDate(Date finishedDate) {
        this.finishedDate = finishedDate;
    }

    public Long getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Long durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public Long getGitlabProjectId() {
        return gitlabProjectId;
    }

    public void setGitlabProjectId(Long gitlabProjectId) {
        this.gitlabProjectId = gitlabProjectId;
    }
}
