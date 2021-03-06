package io.choerodon.devops.app.service.impl;

import com.alibaba.fastjson.JSONObject;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.AgentNodeInfoVO;
import io.choerodon.devops.api.vo.ClusterNodeInfoVO;
import io.choerodon.devops.app.service.ClusterNodeInfoService;
import io.choerodon.devops.app.service.DevopsClusterService;
import io.choerodon.devops.infra.dto.DevopsClusterDTO;
import io.choerodon.devops.infra.util.K8sUtil;
import io.choerodon.devops.infra.util.TypeUtil;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author zmf
 */
@Service
public class ClusterNodeInfoServiceImpl implements ClusterNodeInfoService {
    private static final String REDIS_CLUSTER_KEY_TEMPLATE = "node_info_project_id_%s_cluster_id_%s";
    private static final String CPU_MEASURE_FORMAT = "%.2f";
    private static final String MEMORY_MEASURE_FORMAT = "%.2f%s";
    private static final String[] MEMORY_MEASURE = {"Ki", "Ki", "Mi", "Gi"};
    private static final String PERCENTAGE_FORMAT = "%.2f%%";
    /**
     * 如果出现时间的解析失误，可能是并发问题，不建议作为局部变量，可以考虑使用Joda-Time库，
     * 目前考虑Agent发送消息的间隔不会产生并发问题，当前日期(20190118)
     */
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final Logger LOGGER = LoggerFactory.getLogger(ClusterNodeInfoServiceImpl.class);

    @Autowired
    private DevopsClusterService devopsClusterService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public String getRedisClusterKey(Long clusterId) {
        DevopsClusterDTO devopsClusterDTO = devopsClusterService.baseQuery(clusterId);
        if (devopsClusterDTO != null) {
            return getRedisClusterKey(clusterId, devopsClusterDTO.getProjectId());
        } else {
            throw new CommonException("error.cluster.get");
        }
    }

    @Override
    public String getRedisClusterKey(Long clusterId, Long projectId) {
        return String.format(REDIS_CLUSTER_KEY_TEMPLATE, projectId, clusterId);
    }

    @Override
    public void setValueForKey(String redisClusterKey, List<AgentNodeInfoVO> agentNodeInfoVOS) {
        stringRedisTemplate.delete(redisClusterKey);
        stringRedisTemplate.opsForList().rightPushAll(redisClusterKey, agentNodeInfoVOS.stream().map(this::node2JsonString).collect(Collectors.toList()));
    }

    private void setCpuPercentage(ClusterNodeInfoVO node) {
        double total = Double.parseDouble(node.getCpuTotal());
        double limit = Double.parseDouble(node.getCpuLimit());
        double request = Double.parseDouble(node.getCpuRequest());
        node.setCpuLimitPercentage(String.format(PERCENTAGE_FORMAT, limit / total * 100));
        node.setCpuRequestPercentage(String.format(PERCENTAGE_FORMAT, request / total * 100));
    }

    /**
     * deal with the raw node information from the agent
     * Don't change the execution order unless you know about what you do.
     *
     * @param raw the node information
     * @return the json string of the node information
     */
    private String node2JsonString(AgentNodeInfoVO raw) {
        ClusterNodeInfoVO node = new ClusterNodeInfoVO();
        BeanUtils.copyProperties(raw, node);
        node.setCpuLimit(String.format(CPU_MEASURE_FORMAT, K8sUtil.getNormalValueFromCpuString(node.getCpuLimit())));
        node.setCpuRequest(String.format(CPU_MEASURE_FORMAT, K8sUtil.getNormalValueFromCpuString(node.getCpuRequest())));
        node.setCpuTotal(String.format(CPU_MEASURE_FORMAT, K8sUtil.getNormalValueFromCpuString(StringUtils.isEmpty(raw.getCpuAllocatable()) ? raw.getCpuCapacity() : raw.getCpuAllocatable())));
        node.setPodTotal(Long.parseLong(StringUtils.isEmpty(raw.getPodAllocatable()) ? raw.getPodCapacity() : raw.getPodAllocatable()));
        node.setMemoryTotal(StringUtils.isEmpty(raw.getMemoryAllocatable()) ? raw.getMemoryCapacity() : raw.getMemoryAllocatable());

        setMemoryInfo(node);

        node.setPodPercentage(String.format(PERCENTAGE_FORMAT, node.getPodCount() * 1.0 / node.getPodTotal() * 100));

        setCpuPercentage(node);

        try {
            node.setCreateTime(simpleDateFormat.format(simpleDateFormat.parse(raw.getCreateTime())));
        } catch (ParseException e) {
            LOGGER.info("date: {} failed to be formatted", raw.getCreateTime());
        } catch (Exception ex) {
            LOGGER.warn("Exception occurred when parsing creation time: {}", raw.getCreateTime());
        }
        return JSONObject.toJSONString(node);
    }

    /**
     * set the values for memory
     *
     * @param node the node information
     */
    private void setMemoryInfo(ClusterNodeInfoVO node) {
        double total = ((Long) K8sUtil.getByteFromMemoryString(node.getMemoryTotal())).doubleValue();
        long request = K8sUtil.getByteFromMemoryString(node.getMemoryRequest());
        long limit = K8sUtil.getByteFromMemoryString(node.getMemoryLimit());
        node.setMemoryLimitPercentage(String.format(PERCENTAGE_FORMAT, limit / total * 100));
        node.setMemoryRequestPercentage(String.format(PERCENTAGE_FORMAT, request / total * 100));

        node.setMemoryTotal(dealWithMemoryMeasure(total));
        node.setMemoryRequest(dealWithMemoryMeasure(request));
        node.setMemoryLimit(dealWithMemoryMeasure(limit));
    }

    /**
     * from byte to M or G
     *
     * @param memory the memory string
     * @return the memory string
     */
    private String dealWithMemoryMeasure(final double memory) {
        double value = memory;
        int count = 0;
        while (value >= 1024 && count < MEMORY_MEASURE.length - 1) {
            value /= 1024;
            count++;
        }

        if (count == 0) {
            value /= 1024;
        }
        return String.format(MEMORY_MEASURE_FORMAT, value, MEMORY_MEASURE[count]);
    }

    @Override
    public Page<ClusterNodeInfoVO> pageClusterNodeInfo(Long clusterId, Long projectId, PageRequest pageable) {
        long start = (long) (pageable.getPage() - 1) * (long) pageable.getSize();
        long stop = start + (long) pageable.getSize() - 1;
        String redisKey = getRedisClusterKey(clusterId, projectId);

        long total = stringRedisTemplate.opsForList().size(redisKey);
        List<ClusterNodeInfoVO> nodes = stringRedisTemplate
                .opsForList()
                .range(redisKey, start, stop)
                .stream()
                .map(node -> JSONObject.parseObject(node, ClusterNodeInfoVO.class))
                .collect(Collectors.toList());
        Page<ClusterNodeInfoVO> result = new Page<>();
        if (total < pageable.getSize() * pageable.getPage()) {
            result.setSize(TypeUtil.objToInt(total) - (pageable.getSize() * (pageable.getPage() - 1)));
        } else {
            result.setSize(pageable.getSize());
        }
        result.setSize(pageable.getSize());
        result.setNumber(pageable.getPage());
        result.setTotalElements(total);
        result.setContent(nodes);
        return result;
    }

    @Override
    public ClusterNodeInfoVO queryNodeInfo(Long projectId, Long clusterId, String nodeName) {
        if (StringUtils.isEmpty(nodeName)) {
            return null;
        }

        String redisKey = getRedisClusterKey(clusterId, projectId);
        long total = stringRedisTemplate.opsForList().size(redisKey);

        // get all nodes according to the cluster id and filter the node with the certain name
        return stringRedisTemplate
                .opsForList()
                .range(redisKey, 0, total - 1)
                .stream()
                .map(node -> JSONObject.parseObject(node, ClusterNodeInfoVO.class))
                .filter(node -> nodeName.equals(node.getNodeName()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<String> queryNodeName(Long projectId, Long clusterId) {
        DevopsClusterDTO devopsClusterDTO = devopsClusterService.baseQuery(clusterId);

        String rediskey = getRedisClusterKey(clusterId, devopsClusterDTO.getProjectId());

        long total = stringRedisTemplate.opsForList().size(rediskey);

        return Objects.requireNonNull(stringRedisTemplate
                .opsForList()
                .range(rediskey, 0, total - 1))
                .stream()
                .map(node -> JSONObject.parseObject(node, ClusterNodeInfoVO.class))
                .map(ClusterNodeInfoVO::getNodeName)
                .collect(Collectors.toList());

    }

    @Override
    public long countNodes(Long projectId, Long clusterId) {
        String key = getRedisClusterKey(clusterId, projectId);
        Long count = stringRedisTemplate.opsForList().size(key);
        return count == null ? 0 : count;
    }
}
