package org.kiwiproject.champagne.jdbi;

import java.util.List;

import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.kiwiproject.champagne.core.DeploymentEnvironment;
import org.kiwiproject.champagne.jdbi.mappers.DeploymentEnvironmentMapper;

@RegisterRowMapper(DeploymentEnvironmentMapper.class)
public interface DeploymentEnvironmentDao {

    @SqlUpdate("insert into deployment_environments"
            + " (environment_name, created_by, updated_by)"
            + " values (:name, :createdById, :updatedById)")
    @GetGeneratedKeys
    long insertEnvironment(@BindBean DeploymentEnvironment env);

    @SqlUpdate("update deployment_environments set environment_name = :name, updated_at = current_timestamp, updated_by = :updatedById where id = :id")
    void updateEnvironment(@BindBean DeploymentEnvironment env);

    @SqlUpdate("delete from deployment_environments where id = :id")
    void deleteById(@Bind("id") long id);

    @SqlQuery("select * from deployment_environments")
    List<DeploymentEnvironment> findAllEnvironments();

}
