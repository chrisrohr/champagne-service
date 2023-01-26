package org.kiwiproject.champagne.dao;

import java.util.List;

import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.kiwiproject.champagne.model.DeploymentEnvironment;
import org.kiwiproject.champagne.dao.mappers.DeploymentEnvironmentMapper;

@RegisterRowMapper(DeploymentEnvironmentMapper.class)
public interface DeploymentEnvironmentDao {
    @SqlUpdate("insert into deployment_environments"
            + " (environment_name, created_by, updated_by)"
            + " values (:name, :createdById, :updatedById)")
    @GetGeneratedKeys
    long insertEnvironment(@BindBean DeploymentEnvironment env);

    @SqlUpdate("update deployment_environments set environment_name = :name, updated_at = current_timestamp, updated_by = :updatedById where id = :id")
    void updateEnvironment(@BindBean DeploymentEnvironment env);

    @SqlQuery("select * from deployment_environments")
    List<DeploymentEnvironment> findAllEnvironments();

    @SqlUpdate("delete from deployment_environments where id = :id")
    void hardDeleteById(@Bind("id") long id);

    @SqlUpdate("update deployment_environments set deleted = true, updated_at = current_timestamp, updated_by = :updatedById where id = :id")
    void softDeleteById(@Bind("id") long id, @Bind("updatedById") long updatedById);

    @SqlUpdate("update deployment_environments set deleted = false, updated_at = current_timestamp, updated_by = :updatedById where id = :id")
    void unSoftDeleteById(@Bind("id") long id, @Bind("updatedById") long updatedById);

}
