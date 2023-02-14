package org.kiwiproject.champagne.dao.mappers;

import static org.kiwiproject.jdbc.KiwiJdbc.enumValueOrNull;
import static org.kiwiproject.jdbc.KiwiJdbc.instantFromTimestamp;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.kiwiproject.champagne.model.Host;
import org.kiwiproject.champagne.model.Host.Source;

public class HostMapper implements RowMapper<Host> {

    @Override
    public Host map(ResultSet rs, StatementContext ctx) throws SQLException {

        List<String> tagList = Optional.ofNullable(rs.getString("tags"))
            .filter(StringUtils::isNotBlank)
            .map(tags -> Arrays.asList(tags.split(",")))
            .orElseGet(List::of);

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
