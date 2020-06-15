package io.choerodon.devops.infra.feign.fallback;

import java.util.List;
import java.util.Set;

import org.springframework.http.ResponseEntity;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.infra.dto.harbor.HarborRepoDTO;
import io.choerodon.devops.infra.dto.repo.NexusMavenRepoDTO;
import io.choerodon.devops.api.vo.harbor.HarborCustomRepo;
import io.choerodon.devops.infra.feign.RdupmClient;

/**
 * User: Mr.Wang
 * Date: 2020/6/11
 */
public class RdupmClientFallback implements RdupmClient {
    @Override
    public ResponseEntity<List<HarborCustomRepo>> listAllCustomRepoByProject(Long projectId) {
        throw new CommonException("error.query.repo.list");
    }

    @Override
    public ResponseEntity<HarborCustomRepo> listRelatedCustomRepoByService(Long projectId, Long appServiceId) {
        throw new CommonException("error.query.repo.list.by.appServiceId");
    }

    @Override
    public ResponseEntity saveRelationByService(Long projectId, Long appServiceId, Long customRepoId) {
        throw new CommonException("error.save.repo.rel");
    }

    @Override
    public ResponseEntity deleteRelationByService(Long projectId, Long appServiceId, Long customRepoId) {
        throw new CommonException("error.delete.repo");
    }

    @Override
    public ResponseEntity<HarborRepoDTO> queryHarborRepoConfig(Long projectId, Long appServiceId) {
        throw new CommonException("error.query.harbor.config");
    }

    @Override
    public ResponseEntity<List<NexusMavenRepoDTO>> getRepoByProject(Long organizationId, Long projectId, String repoType, String type) {
        throw new CommonException("error.query.nexus.repo.list", projectId, repoType, type);
    }

    @Override
    public ResponseEntity<List<NexusMavenRepoDTO>> getRepoUserByProject(Long organizationId, Long projectId, Set<Long> repositoryIds) {
        throw new CommonException("error.query.nexus.repo.user.list", projectId, repositoryIds);
    }

    @Override
    public ResponseEntity<HarborRepoDTO> queryHarborRepoConfigById(Long projectId, Long repoId, String repoType) {
        throw new CommonException("error.query.config.by.configId");
    }
}
