package org.kiwiproject.champagne.jdbi;

import java.util.List;

import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.kiwiproject.champagne.core.manualdeployment.DeploymentTaskStatus;
import org.kiwiproject.champagne.core.manualdeployment.ReleaseStatus;

@RegisterRowMapper(ReleaseStatusMapper.class)
public interface ReleaseStatusDao {

    @SqlUpdate("insert into manual_deployment_task_release_statuses"
            + " (manual_deployment_task_release_id, environment, status)"
            + " values (:releaseId, :environment, :status)")
    @GetGeneratedKeys
    long insertReleaseStatus(@BindBean ReleaseStatus releaseStatus);
    
    @SqlQuery("select * from manual_deployment_task_release_statuses where manual_deployment_task_release_id = :releaseId")
    List<ReleaseStatus> findByReleaseId(@Bind("releaseId") long id);

    @SqlUpdate("update manual_deployment_task_release_statuses set status = :status, updated_at = now() "
            + "where manual_deployment_task_release_id = :releaseId and environment = :environment")
    int updateStatus(@Bind("releaseId") long releaseId, @Bind("status") DeploymentTaskStatus status, @Bind("environment") String environment);
}
