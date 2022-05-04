package org.kiwiproject.champagne.jdbi.mappers;

import static org.kiwiproject.jdbc.KiwiJdbc.instantFromTimestamp;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.kiwiproject.champagne.core.User;

public class UserMapper implements RowMapper<User> {
    
    @Override
    public User map(ResultSet r, StatementContext ctx) throws SQLException {

        return User.builder()
                .id(r.getLong("id"))
                .createdAt(instantFromTimestamp(r, "created_at"))
                .updatedAt(instantFromTimestamp(r, "updated_at"))
                .firstName(r.getString("first_name"))
                .lastName(r.getString("last_name"))
                .displayName(r.getString("display_name"))
                .systemIdentifier(r.getString("system_identifier"))
                .build();
    }
}
