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

    @SqlQuery("select * from components where tag in (<tags>)")
    List<Component> findComponentsByHostTags(@BindList("tags") List<String> tags);

    @SqlUpdate("insert into components (component_name, tag) values (:componentName, :tag)")
    @GetGeneratedKeys
    long insertComponent(@BindBean Component component);

    @SqlUpdate("delete from components where id = :id")
    int deleteComponent(@Bind("id") long id);

}
