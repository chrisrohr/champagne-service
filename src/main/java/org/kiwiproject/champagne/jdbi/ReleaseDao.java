package org.kiwiproject.champagne.jdbi;

import java.util.List;

import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.kiwiproject.champagne.core.manualdeployment.Release;
import org.kiwiproject.champagne.jdbi.mappers.ReleaseMapper;

@RegisterRowMapper(ReleaseMapper.class)
public interface ReleaseDao {
    
    @SqlUpdate("insert into manual_deployment_task_releases (release_number) values (:releaseNumber)")
    @GetGeneratedKeys
    long insertRelease(@BindBean Release release);

    @SqlQuery("select count(*) from manual_deployment_task_releases")
    long countAll();

    @SqlQuery("select * from manual_deployment_task_releases order by release_number desc offset :offset limit :limit")
    List<Release> findAll(@Bind("offset") int offset, @Bind("limit") int limit);

    @SqlQuery("select r.* from manual_deployment_task_releases r join manual_deployment_task_release_statuses rs " +
            "on r.id = rs.manual_deployment_task_release_id where rs.status = :status and rs.environment = :environment")
    List<Release> findByStatusAndEnvironment(@Bind("status") DeploymentTaskStatus status, 
                                             @Bind("environment") DeploymentEnvironment environment);

    @SqlUpdate("delete from manual_deployment_task_releases where id = :id")
    void deleteById(@Bind("id") long id);
}
