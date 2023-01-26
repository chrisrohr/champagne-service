package org.kiwiproject.champagne.dao.mappers;

import static org.kiwiproject.jdbc.KiwiJdbc.instantFromTimestamp;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.kiwiproject.champagne.model.manualdeployment.ReleaseStage;
import org.kiwiproject.champagne.model.manualdeployment.Task;

public class TaskMapper implements RowMapper<Task> {

    @Override
    public Task map(ResultSet rs, StatementContext ctx) throws SQLException {
        return Task.builder()
            .id(rs.getLong("id"))
            .createdAt(instantFromTimestamp(rs, "created_at"))
            .updatedAt(instantFromTimestamp(rs, "updated_at"))
            .releaseId(rs.getLong("manual_deployment_task_release_id"))
            .stage(ReleaseStage.valueOf(rs.getString("stage")))
            .summary(rs.getString("summary"))
            .description(rs.getString("description"))
            .component(rs.getString("component"))
            .build();
    }
    
}
