package org.kiwiproject.champagne.dao.mappers;

import static org.kiwiproject.jdbc.KiwiJdbc.instantFromTimestamp;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.kiwiproject.champagne.model.Tag;

public class TagMapper implements RowMapper<Tag> {

    @Override
    public Tag map(ResultSet rs, StatementContext ctx) throws SQLException {
        return Tag.builder()
                .id(rs.getLong("id"))
                .createdAt(instantFromTimestamp(rs, "created_at"))
                .updatedAt(instantFromTimestamp(rs, "updated_at"))
                .name(rs.getString("name"))
                .deployableSystemId(rs.getLong("deployable_system_id"))
                .build();
    }

}
