package org.kiwiproject.champagne.dao.mappers;

import static org.kiwiproject.jdbc.KiwiJdbc.instantFromTimestamp;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.kiwiproject.champagne.model.manualdeployment.DeploymentTaskStatus;
import org.kiwiproject.champagne.model.manualdeployment.ReleaseStatus;

public class ReleaseStatusMapper implements RowMapper<ReleaseStatus> {
    
    @Override
    public ReleaseStatus map(ResultSet rs, StatementContext ctx) throws SQLException {
        return ReleaseStatus.builder()
                .id(rs.getLong("id"))
                .createdAt(instantFromTimestamp(rs, "created_at"))
                .updatedAt(instantFromTimestamp(rs, "updated_at"))
                .releaseId(rs.getLong("manual_deployment_task_release_id"))
                .environmentId(rs.getLong("deployment_environment_id"))
                .status(DeploymentTaskStatus.valueOf(rs.getString("status")))
                .build();
    }
}
