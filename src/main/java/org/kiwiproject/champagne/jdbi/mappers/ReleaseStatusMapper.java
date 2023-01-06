package org.kiwiproject.champagne.jdbi.mappers;

import static org.kiwiproject.jdbc.KiwiJdbc.instantFromTimestamp;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.kiwiproject.champagne.core.manualdeployment.DeploymentTaskStatus;
import org.kiwiproject.champagne.core.manualdeployment.ReleaseStatus;

public class ReleaseStatusMapper implements RowMapper<ReleaseStatus> {
    
    @Override
    public ReleaseStatus map(ResultSet rs, StatementContext ctx) throws SQLException {
        return ReleaseStatus.builder()
                .id(rs.getLong("id"))
                .createdAt(instantFromTimestamp(rs, "created_at"))
                .updatedAt(instantFromTimestamp(rs, "updated_at"))
                .releaseId(rs.getLong("manual_deployment_task_release_id"))
                .environment(rs.getString("environment"))
                .status(DeploymentTaskStatus.valueOf(rs.getString("status")))
                .build();
    }
}
