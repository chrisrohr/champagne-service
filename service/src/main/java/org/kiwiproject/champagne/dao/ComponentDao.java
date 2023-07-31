package org.kiwiproject.champagne.dao;

import java.util.List;

import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.kiwiproject.champagne.dao.mappers.ComponentMapper;
import org.kiwiproject.champagne.model.Component;

@RegisterRowMapper(ComponentMapper.class)
public interface ComponentDao {

    @SqlQuery("select * from components where tag_id in (<tagIds>) order by component_name")
    List<Component> findComponentsByHostTags(@BindList("tagIds") List<Long> tagIds);

    @SqlQuery("select * from components where deployable_system_id = :deployableSystemId order by component_name")
    List<Component> findComponentsForSystem(@Bind("deployableSystemId") long deployableSystemId);

    @SqlQuery("select * from components where deployable_system_id = :deployableSystemId and component_name like :name order by component_name")
    List<Component> findComponentsForSystemMatchingName(@Bind("deployableSystemId") long deployableSystemId, @Bind("name") String name);

    @SqlUpdate("insert into components (component_name, tag_id, deployable_system_id) values (:componentName, :tagId, :deployableSystemId)")
    @GetGeneratedKeys
    long insertComponent(@BindBean Component component);

    @SqlUpdate("update components set component_name = :componentName, tag_id = :tagId where id = :id")
    int updateComponent(@Bind("componentName") String componentName, @Bind("tagId") Long tagId, @Bind("id") long id);

    @SqlUpdate("delete from components where id = :id")
    int deleteComponent(@Bind("id") long id);

}
