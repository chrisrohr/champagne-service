package org.kiwiproject.champagne.dao;

import java.time.Instant;
import java.util.List;

import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.kiwiproject.champagne.dao.mappers.AuditRecordMapper;
import org.kiwiproject.champagne.model.AuditRecord;

@RegisterRowMapper(AuditRecordMapper.class)
public interface AuditRecordDao {

    @SqlUpdate("insert into audit_records (user_system_identifier, action, record_type, record_id, deployable_system_id) " +
            "values (:userSystemIdentifier, :action, :recordType, :recordId, :deployableSystemId)")
    @GetGeneratedKeys
    long insertAuditRecord(@BindBean AuditRecord auditRecord);

    @SqlQuery("select * from audit_records where deployable_system_id = :systemId order by audit_timestamp desc offset :offset limit :limit")
    List<AuditRecord> findPagedAuditRecordsForSystem(@Bind("offset") int offset, @Bind("limit") int limit, @Bind("systemId") long systemId);

    @SqlQuery("select count(*) from audit_records where deployable_system_id = :systemId")
    long countAuditRecordsForSystem(@Bind("systemId") long systemId);

    @SqlQuery("select * from audit_records order by audit_timestamp desc offset :offset limit :limit")
    List<AuditRecord> findPagedAuditRecords(@Bind("offset") int offset, @Bind("limit") int limit);

    @SqlQuery("select count(*) from audit_records")
    long countAuditRecords();

    @SqlUpdate("delete from audit_records where audit_timestamp < :maxRetainDate")
    int deleteAuditRecordsOlderThan(@Bind("maxRetainDate") Instant maxRetainDate);
}
