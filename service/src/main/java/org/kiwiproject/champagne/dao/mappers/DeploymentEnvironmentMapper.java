package org.kiwiproject.champagne.dao.mappers;

import static org.kiwiproject.jdbc.KiwiJdbc.instantFromTimestamp;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.kiwiproject.champagne.model.DeploymentEnvironment;

public class DeploymentEnvironmentMapper implements RowMapper<DeploymentEnvironment> {

    @Override
    public DeploymentEnvironment map(ResultSet rs, StatementContext ctx) throws SQLException {
        return DeploymentEnvironment.builder()
                .id(rs.getLong("id"))
                .createdAt(instantFromTimestamp(rs, "created_at"))
                .updatedAt(instantFromTimestamp(rs, "updated_at"))
                .name(rs.getString("environment_name"))
                .deleted(rs.getBoolean("deleted"))
                .build();
    }
}
