package io.choerodon.devops.infra.template;

import org.springframework.stereotype.Component;

import io.choerodon.core.notify.*;

/**
 * @author zmf
 * @since 12/4/19
 */
@NotifyBusinessType(code = "disableAppService", name = "应用服务停用", level = Level.PROJECT,
        description = "应用服务停用通知", isAllowConfig = false, isManualRetry = true, categoryCode = "app-service-notice",
        pmEnabledFlag = true,
        notifyType = ServiceNotifyType.DEVOPS_NOTIFY,
        targetUserType = {TargetUserType.TARGET_USER_APPLICATION_SERVICE_PERMISSION_OWNER})
@Component
public class DisableAppServicePmTemplate implements PmTemplate {
    @Override
    public String code() {
        return "DisableAppServicePm";
    }

    @Override
    public String name() {
        return "停用应用服务站内信模板";
    }

    @Override
    public String businessTypeCode() {
        // TODO by zmf
        return null;
    }

    @Override
    public String title() {
        return "应用服务已停用";
    }

    /**
     * projectName appServiceName projectId projectName projectCategory organizationId
     */
    @Override
    public String content() {
        return "<p>项目“${projectName}”下的应用服务“${appServiceName}”已被停用</p>\n" +
                "<p><a href=#/devops/app-service?type=project&id=${projectId}&name=${projectName}&category=${projectCategory}&organizationId=${organizationId}>查看详情</a></p>";
    }
}