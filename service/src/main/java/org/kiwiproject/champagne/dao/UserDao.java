package org.kiwiproject.champagne.dao;

import java.util.List;
import java.util.Optional;

import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.kiwiproject.champagne.dao.mappers.UserInSystemMapper;
import org.kiwiproject.champagne.dao.mappers.UserMapper;
import org.kiwiproject.champagne.model.User;
import org.kiwiproject.champagne.model.UserInSystem;

@RegisterRowMapper(UserMapper.class)
public interface UserDao {
    
    @SqlUpdate("insert into users (first_name, last_name, display_name, system_identifier, admin) values (:firstName, :lastName, :displayName, :systemIdentifier, :admin)")
    @GetGeneratedKeys
    long insertUser(@BindBean User user);

    @SqlUpdate("update users set first_name = :firstName, last_name = :lastName, display_name = :displayName, system_identifier = :systemIdentifier, admin = :admin, updated_at = now() where id = :id")
    int updateUser(@BindBean User user);

    @SqlQuery("select * from users where system_identifier = :systemIdentifier")
    Optional<User> findBySystemIdentifier(@Bind("systemIdentifier") String systemIdentifier);

    @SqlQuery("select * from users offset :offset limit :limit")
    List<User> findPagedUsers(@Bind("offset") int offset, @Bind("limit") int limit);

    @SqlQuery("select count(*) from users")
    long countUsers();

    @SqlUpdate("delete from users where id = :id")
    int deleteUser(@Bind("id") long id);

    @SqlQuery("select u.*, usd.system_admin as system_admin from users u left join users_deployable_systems usd on u.id = usd.user_id where usd.deployable_system_id = :systemId offset :offset limit :limit")
    @RegisterRowMapper(UserInSystemMapper.class)
    List<UserInSystem> findPagedUsersInSystem(@Bind("systemId") long systemId, @Bind("offset") int offset, @Bind("limit") int limit);

    @SqlQuery("select count(*) from users u left join users_deployable_systems usd on u.id = usd.user_id where usd.deployable_system_id = :systemId")
    long countUsersInSystem(@Bind("systemId") long systemId);

    @SqlUpdate("delete from users_deployable_systems where user_id = :userId and deployable_system_id = :systemId")
    int removeUserFromSystem(@Bind("userId") long userId, @Bind("systemId") long systemId);

    @SqlUpdate("update users_deployable_systems set system_admin = true where user_id = :userId and deployable_system_id = :systemId")
    int makeUserAdminInSystem(@Bind("userId") long userId, @Bind("systemId") long systemId);

    @SqlUpdate("update users_deployable_systems set system_admin = false where user_id = :userId and deployable_system_id = :systemId")
    int makeUserNonAdminInSystem(@Bind("userId") long userId, @Bind("systemId") long systemId);

    @SqlUpdate("insert into users_deployable_systems (user_id, deployable_system_id) values (:userId, :systemId)")
    int addUserToSystem(@Bind("userId") long userId, @Bind("systemId") long systemId);
}
