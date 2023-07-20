package org.kiwiproject.champagne.dao.mappers;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.kiwiproject.champagne.model.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.kiwiproject.jdbc.KiwiJdbc.instantFromTimestamp;

public class ComponentMapper implements RowMapper<Component> {

    @Override
    public Component map(ResultSet rs, StatementContext ctx) throws SQLException {
        return Component.builder()
                .id(rs.getLong("id"))
                .createdAt(instantFromTimestamp(rs, "created_at"))
                .updatedAt(instantFromTimestamp(rs, "updated_at"))
                .componentName(rs.getString("component_name"))
                .tag(rs.getString("tag"))
                .deployableSystemId(rs.getLong("deployable_system_id"))
                .build();
    }

}
