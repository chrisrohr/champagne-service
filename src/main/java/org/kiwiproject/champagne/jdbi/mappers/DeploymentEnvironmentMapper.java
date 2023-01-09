package org.kiwiproject.champagne.jdbi.mappers;

import static org.kiwiproject.jdbc.KiwiJdbc.instantFromTimestamp;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.kiwiproject.champagne.core.DeploymentEnvironment;

public class DeploymentEnvironmentMapper implements RowMapper<DeploymentEnvironment> {

    @Override
    public DeploymentEnvironment map(ResultSet rs, StatementContext ctx) throws SQLException {
        return DeploymentEnvironment.builder()
                .id(rs.getLong("id"))
                .createdAt(instantFromTimestamp(rs, "created_at"))
                .createdById(rs.getLong("created_by"))
                .updatedAt(instantFromTimestamp(rs, "updated_at"))
                .updatedById(rs.getLong("updated_by"))
                .name(rs.getString("environment_name"))
                .deleted(rs.getBoolean("deleted"))
                .build();
    }
}
