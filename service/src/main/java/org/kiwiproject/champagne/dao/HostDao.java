package org.kiwiproject.champagne.dao;

import java.util.List;

import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.kiwiproject.champagne.dao.mappers.HostMapper;
import org.kiwiproject.champagne.model.Host;

@RegisterRowMapper(HostMapper.class)
public interface HostDao {

    @SqlQuery("select * from hosts where environment_id = :envId")
    List<Host> findHostsByEnvId(@Bind("envId") Long envId);

    @SqlUpdate("insert into hosts (environment_id, hostname, tags, source) values (:environmentId, :hostname, :tagCsv, :source)")
    @GetGeneratedKeys
    long insertHost(@BindBean Host host, @Bind("tagCsv") String tagCsv);

    @SqlUpdate("delete from hosts where id = :id")
    int deleteHost(@Bind("id") long id);
}
