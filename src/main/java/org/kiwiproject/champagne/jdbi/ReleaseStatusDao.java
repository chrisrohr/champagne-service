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
import org.kiwiproject.champagne.jdbi.mappers.ReleaseStatusMapper;

@RegisterRowMapper(ReleaseStatusMapper.class)
public interface ReleaseStatusDao {

    @SqlUpdate("insert into manual_deployment_task_release_statuses"
            + " (manual_deployment_task_release_id, deployment_environment_id, status)"
            + " values (:releaseId, :environmentId, :status)")
    @GetGeneratedKeys
    long insertReleaseStatus(@BindBean ReleaseStatus releaseStatus);
    
    @SqlQuery("select * from manual_deployment_task_release_statuses where manual_deployment_task_release_id = :releaseId")
    List<ReleaseStatus> findByReleaseId(@Bind("releaseId") long id);

    @SqlUpdate("update manual_deployment_task_release_statuses set status = :status, updated_at = now() where id = :id")
    int updateStatus(@Bind("id") long id, @Bind("status") DeploymentTaskStatus status);
}
