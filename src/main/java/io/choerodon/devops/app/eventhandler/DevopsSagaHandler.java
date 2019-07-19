package io.choerodon.devops.app.eventhandler;

import static io.choerodon.devops.app.eventhandler.constants.SagaTopicCodeConstants.DEVOPS_CREATE_GITLAB_TEMPLATE_PROJECT;
import static io.choerodon.devops.app.eventhandler.constants.SagaTopicCodeConstants.DEVOPS_GITLAB_PIPELINE;
import static io.choerodon.devops.app.eventhandler.constants.SagaTopicCodeConstants.DEVOPS_SET_APPLICATION_TEMPLATE_ERROR;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import io.choerodon.asgard.saga.SagaDefinition;
import io.choerodon.asgard.saga.annotation.SagaTask;
import io.choerodon.core.notify.NoticeSendDTO;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.api.vo.iam.entity.*;
import io.choerodon.devops.app.eventhandler.payload.DevOpsAppImportPayload;
import io.choerodon.devops.app.eventhandler.payload.DevOpsAppPayload;
import io.choerodon.devops.app.eventhandler.payload.DevOpsUserPayload;
import io.choerodon.devops.app.eventhandler.payload.GitlabProjectPayload;
import io.choerodon.devops.domain.application.repository.*;
import io.choerodon.devops.app.service.impl.UpdateUserPermissionService;
import io.choerodon.devops.app.service.impl.UpdateAppUserPermissionServiceImpl;
import io.choerodon.devops.infra.util.GitUserNameUtil;
import io.choerodon.devops.infra.enums.PipelineNoticeType;
import io.choerodon.devops.infra.enums.WorkFlowStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * Creator: Runge
 * Date: 2018/7/27
 * Time: 10:06
 * Description: Saga msg by DevOps self
 */
@Component
public class DevopsSagaHandler {
    private static final String TEMPLATE = "template";
    private static final String APPLICATION = "application";
    private static final String STATUS_FIN = "finished";
    private static final String STATUS_FAILED = "failed";
    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsSagaHandler.class);

    private final Gson gson = new Gson();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;
    @Autowired
    private DevopsGitService devopsGitService;
    @Autowired
    private ApplicationTemplateService applicationTemplateService;
    @Autowired
    private ApplicationService applicationService;
    @Autowired
    private DevopsGitlabPipelineService devopsGitlabPipelineService;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private ApplicationTemplateRepository applicationTemplateRepository;
    @Autowired
    private DevopsEnvironmentRepository devopsEnvironmentRepository;
    @Autowired
    private ApplicationInstanceService applicationInstanceService;
    @Autowired
    private PipelineTaskRecordRepository taskRecordRepository;
    @Autowired
    private PipelineStageRecordRepository stageRecordRepository;
    @Autowired
    private PipelineService pipelineService;
    @Autowired
    private PipelineRecordRepository pipelineRecordRepository;


    /**
     * devops创建环境
     */
    @SagaTask(code = "devopsCreateEnv",
            description = "devops创建环境",
            sagaCode = "devops-create-env",
            maxRetryCount = 3,
            seq = 1)
    public String devopsCreateEnv(String data) {
        GitlabProjectPayload gitlabProjectPayload = gson.fromJson(data, GitlabProjectPayload.class);
        try {
            devopsEnvironmentService.handleCreateEnvSaga(gitlabProjectPayload);
        } catch (Exception e) {
            devopsEnvironmentService.setEnvErrStatus(data, gitlabProjectPayload.getIamProjectId());
            throw e;
        }
        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository
                .baseQueryByClusterIdAndCode(gitlabProjectPayload.getClusterId(), gitlabProjectPayload.getPath());
        if (devopsEnvironmentE.getFailed() != null && devopsEnvironmentE.getFailed()) {
            devopsEnvironmentE.initFailed(false);
            devopsEnvironmentRepository.baseUpdate(devopsEnvironmentE);
        }
        return data;
    }

    /**
     * 环境创建失败
     */
    @SagaTask(code = "devopsCreateEnvError",
            description = "set  DevOps app status error",
            sagaCode = "devops-set-env-err",
            maxRetryCount = 3,
            seq = 1)
    public String setEnvErr(String data) {
        GitlabProjectPayload gitlabProjectPayload = gson.fromJson(data, GitlabProjectPayload.class);
        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository
                .baseQueryByClusterIdAndCode(gitlabProjectPayload.getClusterId(), gitlabProjectPayload.getPath());
        devopsEnvironmentE.initFailed(true);
        devopsEnvironmentRepository.baseUpdate(devopsEnvironmentE);
        return data;
    }

    /**
     * GitOps 事件处理
     */
    @SagaTask(code = "devopsGitOps",
            description = "gitops",
            sagaCode = "devops-sync-gitops",
            concurrentLimitNum = 1,
            maxRetryCount = 3,
            concurrentLimitPolicy = SagaDefinition.ConcurrentLimitPolicy.TYPE_AND_ID,
            seq = 1)
    public String gitops(String data) {
        PushWebHookVO pushWebHookVO = null;
        try {
            pushWebHookVO = objectMapper.readValue(data, PushWebHookVO.class);
        } catch (IOException e) {
            LOGGER.info(e.getMessage());
        }
        devopsGitService.fileResourceSync(pushWebHookVO);
        return data;
    }

    /**
     * GitOps 事件处理
     */
    @SagaTask(code = "devopsOperationGitlabProject",
            description = "devops create GitLab project",
            sagaCode = "devops-create-gitlab-project",
            maxRetryCount = 3,
            seq = 1)
    public String createApp(String data) {
        DevOpsAppPayload devOpsAppPayload = gson.fromJson(data, DevOpsAppPayload.class);
        if (devOpsAppPayload.getType().equals(APPLICATION)) {
            try {
                applicationService.operationApplication(devOpsAppPayload);
            } catch (Exception e) {
                applicationService.setAppErrStatus(data, devOpsAppPayload.getIamProjectId());
                throw e;
            }
        }
        return data;
    }

    /**
     * GitOps 事件处理
     */
    @SagaTask(code = "devopsCreateGitlabProject",
            description = "Devops从外部代码平台导入到gitlab项目",
            sagaCode = "devops-import-gitlab-project",
            maxRetryCount = 3,
            seq = 1)
    public String importApp(String data) {
        DevOpsAppImportPayload devOpsAppImportPayload = gson.fromJson(data, DevOpsAppImportPayload.class);
        if (devOpsAppImportPayload.getType().equals(APPLICATION)) {
            try {
                applicationService.operationApplicationImport(devOpsAppImportPayload);
            } catch (Exception e) {
                applicationService.setAppErrStatus(data, devOpsAppImportPayload.getIamProjectId());
                throw e;
            }
            ApplicationE applicationE = applicationRepository.query(devOpsAppImportPayload.getAppId());
            if (applicationE.getFailed() != null && applicationE.getFailed()) {
                applicationE.setFailed(false);
                if (1 != applicationRepository.update(applicationE)) {
                    LOGGER.error("update application set create success status error");
                }
            }
//            gitlabRepository.batchAddProjectVariable(applicationE.getGitlabProjectE().getId(), TypeUtil.objToInteger(devOpsAppImportPayload.getGitlabUserId()),
//                    applicationService.setVariableDTO(applicationE.getHarborConfigE().getId(),applicationE.getChartConfigE().getId()));
        }
        return data;
    }

    /**
     * GitOps 用户权限分配处理
     */
    @SagaTask(code = "devopsUpdateGitlabUsers",
            description = "devops update gitlab users",
            sagaCode = "devops-update-gitlab-users",
            maxRetryCount = 3,
            seq = 1)
    public String updateGitlabUser(String data) {
        DevOpsUserPayload devOpsUserPayload = gson.fromJson(data, DevOpsUserPayload.class);
        try {
            UpdateUserPermissionService updateUserPermissionService = new UpdateAppUserPermissionServiceImpl();
            updateUserPermissionService
                    .updateUserPermission(devOpsUserPayload.getIamProjectId(), devOpsUserPayload.getAppId(),
                            devOpsUserPayload.getIamUserIds(), devOpsUserPayload.getOption());
        } catch (Exception e) {
            LOGGER.error("update gitlab users {} error", devOpsUserPayload.getIamUserIds());
            throw e;
        }
        return data;
    }

    /**
     * GitOps 应用创建失败处理
     */
    @SagaTask(code = "devopsCreateGitlabProjectErr",
            description = "set  DevOps app status error",
            sagaCode = "devops-create-app-fail",
            maxRetryCount = 3,
            seq = 1)
    public String setAppErr(String data) {
        DevOpsAppPayload devOpsAppPayload = gson.fromJson(data, DevOpsAppPayload.class);
        ApplicationE applicationE = applicationRepository.query(devOpsAppPayload.getAppId());
        applicationE.setFailed(true);
        if (1 != applicationRepository.update(applicationE)) {
            LOGGER.error("update application {} set create failed status error", applicationE.getCode());
        }
        return data;
    }

    /**
     * GitOps 应用模板创建失败处理
     */
    @SagaTask(code = "devopsCreateGitlabProjectTemplateErr",
            description = "set  DevOps app template status error",
            sagaCode = DEVOPS_SET_APPLICATION_TEMPLATE_ERROR,
            maxRetryCount = 3,
            seq = 1)
    public String setAppTemplateErr(String data) {
        DevOpsAppPayload devOpsAppPayload = gson.fromJson(data, DevOpsAppPayload.class);
        ApplicationTemplateE applicationTemplateE = applicationTemplateRepository.baseQueryByCode(
                devOpsAppPayload.getOrganizationId(), devOpsAppPayload.getPath());
        applicationTemplateE.setFailed(true);
        applicationTemplateRepository.baseUpdate(applicationTemplateE);
        return data;
    }

    /**
     * GitOps 模板事件处理
     */
    @SagaTask(code = "devopsOperationGitlabTemplateProject",
            description = "devops create GitLab template project",
            sagaCode = DEVOPS_CREATE_GITLAB_TEMPLATE_PROJECT,
            maxRetryCount = 3,
            seq = 1)
    public String createTemplate(String data) {
        GitlabProjectPayload gitlabProjectEventDTO = gson.fromJson(data, GitlabProjectPayload.class);
        if (gitlabProjectEventDTO.getType().equals(TEMPLATE)) {
            try {
                applicationTemplateService.operationApplicationTemplate(gitlabProjectEventDTO);
            } catch (Exception e) {
                applicationTemplateService.setAppTemplateErrStatus(data, gitlabProjectEventDTO.getOrganizationId());
                throw e;
            }
            ApplicationTemplateE applicationTemplateE = applicationTemplateRepository.baseQueryByCode(
                    gitlabProjectEventDTO.getOrganizationId(), gitlabProjectEventDTO.getPath());
            if (applicationTemplateE.getFailed() != null && applicationTemplateE.getFailed()) {
                applicationTemplateE.setFailed(false);
                applicationTemplateRepository.baseUpdate(applicationTemplateE);
            }
        }
        return data;
    }

    /**
     * GitOps 事件处理
     */
    @SagaTask(code = "devopsGitlabPipeline",
            description = "gitlab-pipeline",
            sagaCode = DEVOPS_GITLAB_PIPELINE,
            maxRetryCount = 3,
            concurrentLimitPolicy = SagaDefinition.ConcurrentLimitPolicy.TYPE_AND_ID,
            seq = 1)
    public String gitlabPipeline(String data) {
        PipelineWebHookDTO pipelineWebHookDTO = null;
        try {
            pipelineWebHookDTO = objectMapper.readValue(data, PipelineWebHookDTO.class);
        } catch (IOException e) {
            LOGGER.info(e.getMessage());
        }
        devopsGitlabPipelineService.handleCreate(pipelineWebHookDTO);
        return data;
    }

    @SagaTask(code = "devops-pipeline-create-instance",
            description = "devops pipeline instance",
            sagaCode = "devops-pipeline-auto-deploy-instance",
            concurrentLimitPolicy = SagaDefinition.ConcurrentLimitPolicy.TYPE_AND_ID,
            maxRetryCount = 3,
            seq = 1)
    public void pipelineAutoDeployInstance(String data) {
        ApplicationDeployVO applicationDeployVO = gson.fromJson(data, ApplicationDeployVO.class);
        Long taskRecordId = applicationDeployVO.getRecordId();
        Long stageRecordId = taskRecordRepository.baseQueryRecordById(taskRecordId).getStageRecordId();
        PipelineStageRecordE stageRecordE = stageRecordRepository.queryById(stageRecordId);
        PipelineTaskRecordE taskRecordE = taskRecordRepository.baseQueryRecordById(taskRecordId);
        Long pipelineRecordId = stageRecordE.getPipelineRecordId();
        try {
            ApplicationInstanceVO applicationInstanceVO = applicationInstanceService.createOrUpdate(applicationDeployVO);
            if (!pipelineRecordRepository.baseQueryById(pipelineRecordId).getStatus().equals(WorkFlowStatus.FAILED.toValue()) || stageRecordE.getIsParallel() == 1) {
                if(!taskRecordE.getStatus().equals(WorkFlowStatus.FAILED.toValue())) {
                    PipelineTaskRecordE pipelineTaskRecordE = new PipelineTaskRecordE(applicationInstanceVO.getId(), WorkFlowStatus.SUCCESS.toString());
                    pipelineTaskRecordE.setId(applicationDeployVO.getRecordId());
                    taskRecordRepository.baseCreateOrUpdateRecord(pipelineTaskRecordE);
                    LOGGER.info("create pipeline auto deploy instance success");
                }
            }
        } catch (Exception e) {
            PipelineTaskRecordE pipelineTaskRecordE = new PipelineTaskRecordE();
            pipelineTaskRecordE.setId(applicationDeployVO.getRecordId());
            pipelineTaskRecordE.setStatus(WorkFlowStatus.FAILED.toValue());
            taskRecordRepository.baseCreateOrUpdateRecord(pipelineTaskRecordE);
            pipelineService.updateStatus(pipelineRecordId, stageRecordId, WorkFlowStatus.FAILED.toValue(), e.getMessage());
            NoticeSendDTO.User user = new NoticeSendDTO.User();
            user.setEmail(GitUserNameUtil.getEmail());
            user.setId(GitUserNameUtil.getUserId().longValue());
            pipelineService.sendSiteMessage(pipelineRecordId, PipelineNoticeType.PIPELINEFAILED.toValue(), Collections.singletonList(user), new HashMap<>());
            LOGGER.error("error create pipeline auto deploy instance {}", e);
        }
    }

    /**
     * devops创建分支
     */
    @SagaTask(code = "devopsCreateBranch",
            description = "devops创建分支",
            sagaCode = "devops-create-branch",
            maxRetryCount = 3,
            seq = 1)
    public String devopsCreateBranch(String data) {
        BranchSagaPayLoad branchSagaDTO = gson.fromJson(data, BranchSagaPayLoad.class);
        devopsGitService.createBranchBySaga(branchSagaDTO);
        return data;
    }


    /**
     * devops创建实例
     */
    @SagaTask(code = "devopsCreateInstance",
            description = "devops创建实例",
            sagaCode = "devops-create-instance",
            maxRetryCount = 3,
            seq = 1)
    public String devopsCreateInstance(String data) {
        InstanceSagaPayload instanceSagaPayload = gson.fromJson(data, InstanceSagaPayload.class);
        applicationInstanceService.createInstanceBySaga(instanceSagaPayload);
        return data;
    }
}
