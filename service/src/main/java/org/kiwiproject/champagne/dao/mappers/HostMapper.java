package org.kiwiproject.champagne.dao.mappers;

import static org.kiwiproject.jdbc.KiwiJdbc.enumValueOrNull;
import static org.kiwiproject.jdbc.KiwiJdbc.instantFromTimestamp;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.kiwiproject.base.KiwiStrings;
import org.kiwiproject.champagne.model.Host;
import org.kiwiproject.champagne.model.Host.Source;

public class HostMapper implements RowMapper<Host> {

    @Override
    public Host map(ResultSet rs, StatementContext ctx) throws SQLException {
        var tagList = KiwiStrings.nullSafeSplitOnCommas(rs.getString("tags"));

        return Host.builder()
            .id(rs.getLong("id"))
            .createdAt(instantFromTimestamp(rs, "created_at"))
            .updatedAt(instantFromTimestamp(rs, "updated_at"))
            .environmentId(rs.getLong("id"))
            .hostname(rs.getString("hostname"))
            .tags(tagList)
            .source(enumValueOrNull(rs, "source", Source.class))
            .build();
    }
    
}
