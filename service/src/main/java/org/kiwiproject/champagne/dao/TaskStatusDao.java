package org.kiwiproject.champagne.dao;

import java.util.List;

import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.kiwiproject.champagne.model.manualdeployment.DeploymentTaskStatus;
import org.kiwiproject.champagne.model.manualdeployment.TaskStatus;
import org.kiwiproject.champagne.dao.mappers.TaskStatusMapper;

@RegisterRowMapper(TaskStatusMapper.class)
public interface TaskStatusDao {
    
    @SqlUpdate("insert into manual_deployment_task_statuses"
            + " (manual_deployment_task_id, deployment_environment_id, status)"
            + " values (:taskId, :environmentId, :status)")
    @GetGeneratedKeys
    long insertTaskStatus(@BindBean TaskStatus taskStatus);

    @SqlQuery("select * from manual_deployment_task_statuses where manual_deployment_task_id = :taskId")
    List<TaskStatus> findByTaskId(@Bind("taskId") long id);

    @SqlUpdate("update manual_deployment_task_statuses set status = :status, updated_at = now() where id = :id")
    int updateStatus(@Bind("id") long id, @Bind("status") DeploymentTaskStatus status);
}
