package org.kiwiproject.champagne.dao;

import java.util.List;

import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.kiwiproject.champagne.dao.mappers.TagMapper;
import org.kiwiproject.champagne.model.Tag;

@RegisterRowMapper(TagMapper.class)
public interface TagDao {

    @SqlQuery("select * from tags where deployable_system_id = :deployableSystemId order by name")
    List<Tag> findTagsForSystem(@Bind("deployableSystemId") long deployableSystemId);

    @SqlUpdate("insert into tags (name, deployable_system_id) values (:name, :deployableSystemId)")
    @GetGeneratedKeys
    long insertTag(@BindBean Tag tag);

    @SqlUpdate("update tags set name = :name, updated_at = now() where id = :id")
    int updateTag(@Bind("id") long id, @Bind("name") String name);

    @SqlUpdate("delete from tags where id = :id")
    int deleteTag(@Bind("id") long id);

    @SqlQuery("select t.* from tags t left join host_tags ht on ht.tag_id = t.id where ht.host_id = :hostId order by t.name")
    List<Tag> findTagsForHost(@Bind("hostId") long hostId);

    @SqlQuery("select * from tags where id = :id")
    Tag findTagById(@Bind("id") long id);

}
