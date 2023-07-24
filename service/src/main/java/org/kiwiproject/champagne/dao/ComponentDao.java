package org.kiwiproject.champagne.dao;

import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.kiwiproject.champagne.dao.mappers.ComponentMapper;
import org.kiwiproject.champagne.model.Component;

import java.util.List;

@RegisterRowMapper(ComponentMapper.class)
public interface ComponentDao {

    @SqlQuery("select * from components where tag in (<tags>)")
    List<Component> findComponentsByHostTags(@BindList("tags") List<String> tags);

    @SqlQuery("select * from components where deployable_system_id = :deployableSystemId")
    List<Component> findComponentsForSystem(@Bind("deployableSystemId") long deployableSystemId);

    @SqlUpdate("insert into components (component_name, tag, deployable_system_id) values (:componentName, :tag, :deployableSystemId)")
    @GetGeneratedKeys
    long insertComponent(@BindBean Component component);

    @SqlUpdate("update components set component_name = :componentName, tag = :tag where id = :id")
    int updateComponent(@Bind("componentName") String componentName, @Bind("tag") String tag, @Bind("id") long id);

    @SqlUpdate("delete from components where id = :id")
    int deleteComponent(@Bind("id") long id);

}
