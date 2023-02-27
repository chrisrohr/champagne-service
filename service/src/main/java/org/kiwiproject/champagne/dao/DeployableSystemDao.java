package org.kiwiproject.champagne.dao;

import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.kiwiproject.champagne.dao.mappers.DeployableSystemForUserMapper;
import org.kiwiproject.champagne.dao.mappers.DeployableSystemMapper;
import org.kiwiproject.champagne.model.DeployableSystem;

import java.util.List;

public interface DeployableSystemDao {

    @SqlUpdate("insert into deployable_systems (name) values (:name)")
    @GetGeneratedKeys
    long insertDeployableSystem(@BindBean DeployableSystem system);

    @SqlQuery("select s.*, us.system_admin from deployable_systems s join users_deployable_systems us on s.id = us.deployable_system_id where us.user_id = :userId")
    @RegisterRowMapper(DeployableSystemForUserMapper.class)
    List<DeployableSystem> findDeployableSystemsForUser(@Bind("userId") long userId);

    @SqlQuery("select * from deployable_systems order by created_at desc offset :offset limit :limit")
    @RegisterRowMapper(DeployableSystemMapper.class)
    List<DeployableSystem> findPagedDeployableSystems(@Bind("offset") int offset, @Bind("limit") int limit);

    @SqlQuery("select count(*) from deployable_systems")
    long countDeployableSystems();

    @SqlUpdate("update deployable_systems set dev_environment_id = :envId where id = :id")
    int updateDevEnvironment(@Bind("id") long id, @Bind("envId") long envId);

    @SqlUpdate("update deployable_systems set environment_promotion_order = :envCsv where id = :id")
    int updateEnvironmentPromotionOrder(@Bind("id") long id, @Bind("envCsv") String envCsv);

    @SqlUpdate("delete from deployable_systems where id = :id")
    int deleteById(@Bind("id") long id);

    @SqlQuery("select system_admin from users_deployable_systems where user_id = :userId and deployable_system_id = :systemId")
    boolean isUserAdminOfSystem(@Bind("userId") long userId, @Bind("systemId") long systemId);
}
