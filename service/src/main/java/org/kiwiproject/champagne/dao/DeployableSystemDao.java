package org.kiwiproject.champagne.dao;

import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.kiwiproject.champagne.dao.mappers.DeployableSystemForUserMapper;
import org.kiwiproject.champagne.dao.mappers.DeployableSystemMapper;
import org.kiwiproject.champagne.dao.mappers.SystemUserMapper;
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

    @SqlQuery("select * from users_deployable_systems where deployable_system_id = :systemId")
    @RegisterRowMapper(SystemUserMapper.class)
    List<DeployableSystem.SystemUser> findUsersForSystem(@Bind("systemId") long systemId);

    @SqlUpdate("update deployable_systems set dev_environment_id = :envId where id = :id")
    int updateDevEnvironment(@Bind("id") long id, @Bind("envId") long envId);

    @SqlUpdate("update deployable_systems set environment_promotion_order = :envCsv where id = :id")
    int updateEnvironmentPromotionOrder(@Bind("id") long id, @Bind("envCsv") String envCsv);

    @SqlUpdate("delete from deployable_systems where id = :id")
    int deleteById(@Bind("id") long id);

    @SqlQuery("select system_admin from users_deployable_systems where user_id = :userId and deployable_system_id = :systemId")
    boolean isUserAdminOfSystem(@Bind("userId") long userId, @Bind("systemId") long systemId);

    @SqlQuery("select true from users_deployable_systems uds join users u on u.id = uds.user_id join deployable_systems ds on ds.id = uds.deployable_system_id where u.system_identifier = :userName and ds.id = :systemId")
    boolean isUserBySystemIdentifierInSystem(@Bind("userName") String systemIdentifier, @Bind("systemId") long systemId);

    @SqlQuery("select true from users_deployable_systems where deployable_system_id = :systemId and user_id = :userId")
    boolean isUserInSystem(@Bind("userId") long userId, @Bind("systemId") long systemId);

    default void insertOrUpdateSystemUser(long systemId, long userId, boolean isAdmin) {
        if (isUserInSystem(userId, systemId)) {
            updateAdminStatusForUserInSystem(systemId, userId, isAdmin);
        } else {
            addUserToSystem(systemId, userId, isAdmin);
        }
    }

    @SqlUpdate("update users_deployable_systems set admin = :admin where deployable_system_id = :systemId and user_id = :userId")
    void updateAdminStatusForUserInSystem(@Bind("systemId") long systemId, @Bind("userId") long userId, @Bind("admin") boolean isAdmin);

    @SqlUpdate("insert into users_deployable_systems (deployable_system_id, user_id, system_admin) values (:systemId, :userId, :admin)")
    void addUserToSystem(@Bind("systemId") long systemId, @Bind("userId") long userId, @Bind("admin") boolean isAdmin);
}
