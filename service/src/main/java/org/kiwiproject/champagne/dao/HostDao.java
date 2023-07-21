package org.kiwiproject.champagne.dao;

import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.kiwiproject.champagne.dao.mappers.HostMapper;
import org.kiwiproject.champagne.model.Host;

import java.util.List;
import java.util.Optional;

@RegisterRowMapper(HostMapper.class)
public interface HostDao {

    @SqlQuery("select * from hosts where environment_id = :envId and deployable_system_id = :systemId")
    List<Host> findHostsByEnvId(@Bind("envId") Long envId, @Bind("systemId") long systemId);

    @SqlUpdate("insert into hosts (environment_id, hostname, tags, source, deployable_system_id) values (:environmentId, :hostname, :tagCsv, :source, :deployableSystemId)")
    @GetGeneratedKeys
    long insertHost(@BindBean Host host, @Bind("tagCsv") String tagCsv);

    @SqlUpdate("update hosts set hostname = :hostname, tags = :tags where id = :id")
    int updateHost(@Bind("hostname") String hostname, @Bind("tags") String tagCsv, @Bind("id") long id);

    @SqlUpdate("delete from hosts where id = :id")
    int deleteHost(@Bind("id") long id);

    @SqlQuery("select * from hosts where id = :id")
    Optional<Host> findById(@Bind("id") long id);
}
