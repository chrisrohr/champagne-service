package org.kiwiproject.champagne.dao;

import java.util.List;
import java.util.Optional;

import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.kiwiproject.champagne.model.manualdeployment.Task;
import org.kiwiproject.champagne.dao.mappers.TaskMapper;

@RegisterRowMapper(TaskMapper.class)
public interface TaskDao {

    @SqlUpdate("insert into manual_deployment_tasks"
            + " (manual_deployment_task_release_id, stage, summary, description, component)"
            + " values (:releaseId, :stage, :summary, :description, :component)")
    @GetGeneratedKeys
    long insertTask(@BindBean Task task);

    @SqlQuery("select * from manual_deployment_tasks where manual_deployment_task_release_id = :releaseId")
    List<Task> findByReleaseId(@Bind("releaseId") long releaseId);

    @SqlUpdate("update manual_deployment_tasks set stage = :stage, summary = :summary, description = :description,"
            + " component = :component, updated_at = now() where id = :id")
    void updateTask(@BindBean Task task);

    @SqlUpdate("delete from manual_deployment_tasks where id = :id")
    void deleteById(@Bind("id") long id);

    @SqlQuery("select * from manual_deployment_tasks where id = :id")
    Optional<Task> findById(@Bind("id") long id);

    @SqlQuery("select t.* from manual_deployment_tasks t left join manual_deployment_task_statuses s on s.manual_deployment_task_id = t.id where s.id = :statusId")
    Optional<Task> findByTaskStatusId(@Bind("statusId") long statusId);

    @SqlQuery("select id from manual_deployment_tasks")
    List<Long> findAllTaskIds();
}
