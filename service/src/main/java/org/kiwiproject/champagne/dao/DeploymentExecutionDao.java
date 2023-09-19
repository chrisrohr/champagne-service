package org.kiwiproject.champagne.dao;

import java.util.List;

import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.kiwiproject.champagne.dao.mappers.DeploymentExecutionMapper;
import org.kiwiproject.champagne.model.DeploymentExecution;

@RegisterRowMapper(DeploymentExecutionMapper.class)
public interface DeploymentExecutionDao {

    @SqlUpdate("insert into deployment_executions (status, environment_id, deployable_system_id) values (:status, :environmentId, :deployableSystemId)")
    @GetGeneratedKeys
    long insertDeploymentExecution(@BindBean DeploymentExecution execution);

    @SqlQuery("select * from deployment_executions where deployable_system_id = :systemId offset :offset limit :limit")
    List<DeploymentExecution> findPageOfDeploymentExecutions(@Bind("systemId") long systemId, @Bind("offset") int offset, @Bind("limit") int limit);

    @SqlQuery("select count(*) from deployment_executions where deployable_system_id = :systemId")
    long countDeploymentExecutions(@Bind("systemId") long systemId);
}
