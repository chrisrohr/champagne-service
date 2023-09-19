package org.kiwiproject.champagne.dao.mappers;

import static org.kiwiproject.jdbc.KiwiJdbc.enumValueOrNull;
import static org.kiwiproject.jdbc.KiwiJdbc.instantFromTimestamp;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.kiwiproject.champagne.model.DeploymentExecution;

public class DeploymentExecutionMapper implements RowMapper<DeploymentExecution> {
    @Override
    public DeploymentExecution map(ResultSet rs, StatementContext ctx) throws SQLException {
        return DeploymentExecution.builder()
                .id(rs.getLong("id"))
                .createdAt(instantFromTimestamp(rs.getTimestamp("created_at")))
                .updatedAt(instantFromTimestamp(rs.getTimestamp("updated_at")))
                .status(enumValueOrNull(rs, "status", DeploymentExecution.Status.class))
                .failureOutput(rs.getString("failure_output"))
                .environmentId(rs.getLong("environment_id"))
                .consolidatedChangeLog(rs.getString("consolidated_change_log"))
                .percentComplete(rs.getDouble("percent_complete"))
                .deployableSystemId(rs.getLong("deployable_system_id"))
                .build();
    }
}
