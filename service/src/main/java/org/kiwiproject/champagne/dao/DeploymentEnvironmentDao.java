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
    @SqlUpdate("insert into deployment_environments (environment_name) values (:name)")
    @GetGeneratedKeys
    long insertEnvironment(@BindBean DeploymentEnvironment env);

    @SqlUpdate("update deployment_environments set environment_name = :name, updated_at = current_timestamp where id = :id")
    int updateEnvironment(@BindBean DeploymentEnvironment env);

    @SqlQuery("select * from deployment_environments")
    List<DeploymentEnvironment> findAllEnvironments();

    @SqlUpdate("delete from deployment_environments where id = :id")
    int hardDeleteById(@Bind("id") long id);

    @SqlUpdate("update deployment_environments set deleted = true, updated_at = current_timestamp where id = :id")
    int softDeleteById(@Bind("id") long id);

    @SqlUpdate("update deployment_environments set deleted = false, updated_at = current_timestamp where id = :id")
    int unSoftDeleteById(@Bind("id") long id);

}
