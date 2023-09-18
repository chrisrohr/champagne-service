package org.kiwiproject.champagne.dao;

import java.util.List;

import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.kiwiproject.champagne.dao.mappers.DeploymentMapper;
import org.kiwiproject.champagne.model.Deployment;

@RegisterRowMapper(DeploymentMapper.class)
public interface DeploymentDao {

    @SqlUpdate("insert into deployments (build_id, status, environment_id, execution_id, deployable_system_id) values (:buildId, :status, :environmentId, :executionId, :deployableSystemId)")
    @GetGeneratedKeys
    long insertDeployment(@BindBean Deployment deployment);

    @SqlQuery("select * from deployments where execution_id = :executionId offset :offset limit :limit")
    List<Deployment> findPageOfDeploymentsInExecution(@Bind("executionId") long executionId, @Bind("offset") int offset, @Bind("limit") int limit);

    @SqlQuery("select count(*) from deployments where execution_id = :executionId")
    long countDeploymentsInExecution(@Bind("executionId") long executionId);
}
