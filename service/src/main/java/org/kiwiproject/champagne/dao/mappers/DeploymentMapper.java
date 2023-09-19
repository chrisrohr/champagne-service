package org.kiwiproject.champagne.dao.mappers;

import static org.kiwiproject.jdbc.KiwiJdbc.enumValueOrNull;
import static org.kiwiproject.jdbc.KiwiJdbc.instantFromTimestamp;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.kiwiproject.champagne.model.Deployment;

public class DeploymentMapper implements RowMapper<Deployment> {
    @Override
    public Deployment map(ResultSet rs, StatementContext ctx) throws SQLException {
        return Deployment.builder()
                .id(rs.getLong("id"))
                .createdAt(instantFromTimestamp(rs.getTimestamp("created_at")))
                .updatedAt(instantFromTimestamp(rs.getTimestamp("updated_at")))
                .buildId(rs.getLong("build_id"))
                .status(enumValueOrNull(rs, "status", Deployment.Status.class))
                .environmentId(rs.getLong("environment_id"))
                .executionId(rs.getLong("execution_id"))
                .failureOutput(rs.getString("failure_output"))
                .deployableSystemId(rs.getLong("deployable_system_id"))
                .build();
    }
}
