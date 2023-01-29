package org.kiwiproject.champagne.dao;

import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.kiwiproject.champagne.model.AuditRecord;
import org.kiwiproject.champagne.dao.mappers.AuditRecordMapper;

import java.util.List;

@RegisterRowMapper(AuditRecordMapper.class)
public interface AuditRecordDao {

    @SqlUpdate("insert into audit_records (user_system_identifier, action, record_type, record_id) " +
            "values (:userSystemIdentifier, :action, :recordType, :recordId)")
    @GetGeneratedKeys
    long insertAuditRecord(@BindBean AuditRecord auditRecord);

    @SqlQuery("select * from audit_records order by audit_timestamp desc offset :offset limit :limit")
    List<AuditRecord> findPagedAuditRecords(@Bind("offset") int offset, @Bind("limit") int limit);

    @SqlQuery("select count(*) from audit_records")
    long countAuditRecords();

    // TODO: We may want some sort of delete method for auto cleaner purposes
}
