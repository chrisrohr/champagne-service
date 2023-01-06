package org.kiwiproject.champagne.jdbi;

import java.util.List;

import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.kiwiproject.champagne.core.manualdeployment.DeploymentTaskStatus;
import org.kiwiproject.champagne.core.manualdeployment.TaskStatus;

@RegisterRowMapper(TaskStatusMapper.class)
public interface TaskStatusDao {
    
    @SqlUpdate("insert into manual_deployment_task_statuses"
            + " (manual_deployment_task_id, environment, status)"
            + " values (:taskId, :environment, :status)")
    @GetGeneratedKeys
    long insertTaskStatus(@BindBean TaskStatus taskStatus);

    @SqlQuery("select * from manual_deployment_task_statuses where manual_deployment_task_id = :taskId")
    List<TaskStatus> findByTaskId(@Bind("taskId") long id);

    @SqlUpdate("update manual_deployment_task_statuses set status = :status, updated_at = now() "
            + "where manual_deployment_task_id = :taskId and environment = :environment")
    int updateStatus(@Bind("taskId") long taskId, @Bind("status") DeploymentTaskStatus status, @Bind("environment") String environment);
}
