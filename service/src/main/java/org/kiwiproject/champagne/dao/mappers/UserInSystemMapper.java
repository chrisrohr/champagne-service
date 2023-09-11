package org.kiwiproject.champagne.dao.mappers;

import static org.kiwiproject.jdbc.KiwiJdbc.instantFromTimestamp;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.kiwiproject.champagne.model.User;
import org.kiwiproject.champagne.model.UserInSystem;

public class UserInSystemMapper implements RowMapper<UserInSystem> {
    
    @Override
    public UserInSystem map(ResultSet r, StatementContext ctx) throws SQLException {

        var user = User.builder()
                .id(r.getLong("id"))
                .createdAt(instantFromTimestamp(r, "created_at"))
                .updatedAt(instantFromTimestamp(r, "updated_at"))
                .firstName(r.getString("first_name"))
                .lastName(r.getString("last_name"))
                .displayName(r.getString("display_name"))
                .systemIdentifier(r.getString("system_identifier"))
                .admin(r.getBoolean("admin"))
                .build();

        return UserInSystem.builder()
                .user(user)
                .systemAdmin(r.getBoolean("system_admin"))
                .build();
    }
}
