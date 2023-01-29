package org.kiwiproject.champagne.dao.mappers;

import static org.kiwiproject.jdbc.KiwiJdbc.instantFromTimestamp;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.kiwiproject.champagne.model.manualdeployment.Release;

public class ReleaseMapper implements RowMapper<Release> {
    
    @Override
    public Release map(ResultSet rs, StatementContext ctx) throws SQLException {
        return Release.builder()
                    .id(rs.getLong("id"))
                    .createdAt(instantFromTimestamp(rs, "created_at"))
                    .updatedAt(instantFromTimestamp(rs, "updated_at"))
                    .releaseNumber(rs.getString("release_number"))
                    .build();
    }
}
