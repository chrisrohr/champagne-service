package org.kiwiproject.champagne.dao;

import java.util.List;

import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.kiwiproject.champagne.model.manualdeployment.Release;
import org.kiwiproject.champagne.dao.mappers.ReleaseMapper;

@RegisterRowMapper(ReleaseMapper.class)
public interface ReleaseDao {
    
    @SqlUpdate("insert into manual_deployment_task_releases (release_number, deployable_system_id) values (:releaseNumber, :deployableSystemId)")
    @GetGeneratedKeys
    long insertRelease(@BindBean Release release);

    @SqlQuery("select count(*) from manual_deployment_task_releases where deployable_system_id = :systemId")
    long countReleases(@Bind("systemId") long systemId);

    @SqlQuery("select * from manual_deployment_task_releases where deployable_system_id = :systemId order by release_number desc offset :offset limit :limit")
    List<Release> findPagedReleases(@Bind("offset") int offset, @Bind("limit") int limit, @Bind("systemId") long systemId);

    @SqlUpdate("delete from manual_deployment_task_releases where id = :id")
    void deleteById(@Bind("id") long id);

    @SqlQuery("select id from manual_deployment_task_releases")
    List<Long> findAllReleaseIds();
}
