package org.kiwiproject.champagne.jdbi.mappers;

import static org.kiwiproject.jdbc.KiwiJdbc.instantFromTimestamp;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.kiwiproject.champagne.core.manualdeployment.DeploymentTaskStatus;
import org.kiwiproject.champagne.core.manualdeployment.TaskStatus;

public class TaskStatusMapper implements RowMapper<TaskStatus> {
    
    @Override
    public TaskStatus map(ResultSet rs, StatementContext ctx) throws SQLException {
        return TaskStatus.builder()
            .id(rs.getLong("id"))
            .createdAt(instantFromTimestamp(rs, "created_at"))
            .updatedAt(instantFromTimestamp(rs, "updated_at"))
            .taskId(rs.getLong("manual_deployment_task_id"))
            .environmentId(rs.getLong("deployment_environment_id"))
            .status(DeploymentTaskStatus.valueOf(rs.getString("status")))
            .build();
    }
}
