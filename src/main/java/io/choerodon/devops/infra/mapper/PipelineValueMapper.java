package io.choerodon.devops.infra.mapper;

import io.choerodon.devops.infra.dataobject.PipelineValueDO;
import io.choerodon.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  9:33 2019/4/10
 * Description:
 */
public interface PipelineValueMapper extends BaseMapper<PipelineValueDO> {
    List<PipelineValueDO> listByOptions(@Param("projectId") Long projectId,
                                        @Param("appId") Long appId,
                                        @Param("envId") Long envId,
                                        @Param("searchParam") Map<String, Object> searchParam,
                                        @Param("param") String param);
}
