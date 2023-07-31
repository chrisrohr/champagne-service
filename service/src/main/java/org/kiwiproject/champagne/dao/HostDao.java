package org.kiwiproject.champagne.dao;

import java.util.List;
import java.util.Optional;

import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlBatch;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.kiwiproject.champagne.dao.mappers.HostMapper;
import org.kiwiproject.champagne.model.Host;

@RegisterRowMapper(HostMapper.class)
public interface HostDao {

    @SqlQuery("select * from hosts where environment_id = :envId and deployable_system_id = :systemId order by hostname")
    List<Host> findHostsByEnvId(@Bind("envId") Long envId, @Bind("systemId") long systemId);

    @SqlUpdate("insert into hosts (environment_id, hostname, source, deployable_system_id) values (:environmentId, :hostname, :source, :deployableSystemId)")
    @GetGeneratedKeys
    long insertHost(@BindBean Host host);

    default void updateTagList(long hostId, List<Long> tagIds) {
        removeTagsForHost(hostId);
        addTagsForHost(hostId, tagIds);
    }

    @SqlUpdate("delete from host_tags where host_id = :hostId")
    void removeTagsForHost(@Bind("hostId") long hostId);

    @SqlQuery("select tag_id from host_tags where host_id = :hostId")
    List<Long> findTagIdsForHost(@Bind("hostId") long hostId);

    @SqlQuery("select h.* from hosts h inner join host_tags ht on ht.host_id = h.id where ht.tag_id in (<tagIds>) order by h.hostname")
    List<Host> findHostsForTags(@BindList("tagIds") List<Long> tagIds);

    @SqlBatch("insert into host_tags (host_id, tag_id) values (:hostId, :tagId)")
    void addTagsForHost(@Bind("hostId") long hostId, @Bind("tagId") List<Long> tagIds);

    @SqlUpdate("update hosts set hostname = :hostname where id = :id")
    int updateHost(@Bind("hostname") String hostname, @Bind("id") long id);

    @SqlUpdate("delete from hosts where id = :id")
    int deleteHost(@Bind("id") long id);

    @SqlQuery("select * from hosts where id = :id")
    Optional<Host> findById(@Bind("id") long id);
}
