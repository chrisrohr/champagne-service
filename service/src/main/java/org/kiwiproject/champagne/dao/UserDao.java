package org.kiwiproject.champagne.dao;

import java.util.List;
import java.util.Optional;

import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.kiwiproject.champagne.model.User;
import org.kiwiproject.champagne.dao.mappers.UserMapper;

@RegisterRowMapper(UserMapper.class)
public interface UserDao {
    
    @SqlUpdate("insert into users (first_name, last_name, display_name, system_identifier, admin) values (:firstName, :lastName, :displayName, :systemIdentifier, :admin)")
    @GetGeneratedKeys
    long insertUser(@BindBean User user);

    @SqlUpdate("update users set first_name = :firstName, last_name = :lastName, display_name = :displayName, system_identifier = :systemIdentifier, admin = :admin, updated_at = now() where id = :id")
    void updateUser(@BindBean User user);

    @SqlQuery("select * from users where system_identifier = :systemIdentifier")
    Optional<User> findBySystemIdentifier(@Bind("systemIdentifier") String systemIdentifier);

    @SqlQuery("select * from users offset :offset limit :limit")
    List<User> findPagedUsers(@Bind("offset") int offset, @Bind("limit") int limit);

    @SqlQuery("select count(*) from users")
    long countUsers();

    @SqlUpdate("delete from users where id = :id")
    void deleteUser(@Bind("id") long id);
}
