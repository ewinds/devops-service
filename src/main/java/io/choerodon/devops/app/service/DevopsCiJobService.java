package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.infra.dto.DevopsCiJobDTO;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2020/4/3 9:24
 */
public interface DevopsCiJobService {
    /**
     * 创建ci流水线job
     * @param devopsCiJobDTO
     * @return
     */
    DevopsCiJobDTO create(DevopsCiJobDTO devopsCiJobDTO);

    /**
     * 删除stage下的job
     * @param stageId
     */
    void deleteByStageId(Long stageId);

    /**
     * 查询pipeline下的jobs
     * @param ciPipelineId
     * @return
     */
    List<DevopsCiJobDTO> listByPipelineId(Long ciPipelineId);
}
