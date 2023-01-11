package org.kiwiproject.champagne.jdbi.mappers;

import static org.kiwiproject.jdbc.KiwiJdbc.enumValueOrNull;
import static org.kiwiproject.jdbc.KiwiJdbc.instantFromTimestamp;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.kiwiproject.champagne.core.AuditRecord;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AuditRecordMapper implements RowMapper<AuditRecord> {
    @Override
    public AuditRecord map(ResultSet rs, StatementContext ctx) throws SQLException {
        return AuditRecord.builder()
                .id(rs.getLong("id"))
                .timestamp(instantFromTimestamp(rs, "audit_timestamp"))
                .userSystemIdentifier(rs.getString("user_system_identifier"))
                .action(enumValueOrNull(rs, "action", AuditRecord.Action.class))
                .recordType(rs.getString("record_type"))
                .recordId(rs.getLong("record_id"))
                .build();
    }
}
