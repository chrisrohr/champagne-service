package org.kiwiproject.champagne.dao;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.List;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.kiwiproject.champagne.model.Build;

public interface BuildDao {
    
    default List<Build> findPagedBuilds(int offset, int limit, String componentIdentifierFilter, String componentVersionFilter) {
        if (isBlank(componentIdentifierFilter) && isBlank(componentVersionFilter)) {
            // No filtering
            return findPagedBuildsNoFilter(offset, limit);
        } else if (isBlank(componentIdentifierFilter)) {
            // Version only filter
            return findPagedBuildsWithVersionFilter(offset, limit, componentVersionFilter);
        } else if (isBlank(componentVersionFilter)) {
            // Name only filter
            return findPagedBuildsWithIdentiferFilter(offset, limit, componentIdentifierFilter);
        }

        // Both name and version filtering
        return findPagedBuildsWithVersionAndIdentifierFilters(offset, limit, componentIdentifierFilter, componentVersionFilter);
    }

    @SqlQuery("select * from builds order by created_at desc offset :offset limit :limit")
    List<Build> findPagedBuildsNoFilter(@Bind("offset") int offset, @Bind("limit") int limit);

    @SqlQuery("select * from builds where component_version like :version order by created_at desc offset :offset limit :limit")
    List<Build> findPagedBuildsWithVersionFilter(@Bind("offset") int offset, @Bind("limit") int limit, @Bind("version") String componentVersionFilter);

    @SqlQuery("select * from builds where component_identifier like :identifier order by created_at desc offset :offset limit :limit")
    List<Build> findPagedBuildsWithIdentiferFilter(@Bind("offset") int offset, @Bind("limit") int limit, @Bind("identifier") String componentIdentifierFilter);

    @SqlQuery("select * from builds where component_version like :version and component_identifier like :identifier order by created_at desc offset :offset limit :limit")
    List<Build> findPagedBuildsWithVersionAndIdentifierFilters(@Bind("offset") int offset, @Bind("limit") int limit, @Bind("identifier") String componentIdentifierFilter, @Bind("version") String componentVersionFilter);

    default long countBuilds(String componentIdentifierFilter, String componentVersionFilter) {
        if (isBlank(componentIdentifierFilter) && isBlank(componentVersionFilter)) {
            // No filtering
            return countBuildsNoFilter();
        } else if (isBlank(componentIdentifierFilter)) {
            // Version only filter
            return countBuildsWithVersionFilter(componentVersionFilter);
        } else if (isBlank(componentVersionFilter)) {
            // Name only filter
            return countBuildsWithIdentifierFilter(componentIdentifierFilter);
        }

        // Both name and version filtering
        return countBuildsWithVersionAndIdentifierFilters(componentVersionFilter, componentIdentifierFilter);
    }

    @SqlQuery("select count(*) from builds")
    long countBuildsNoFilter();

    @SqlQuery("select count(*) from builds where component_version like :version")
    long countBuildsWithVersionFilter(@Bind("version") String componentVersionFilter);

    @SqlQuery("select count(*) from builds where component_identifier like :identifier")
    long countBuildsWithIdentifierFilter(@Bind("identifier") String componentIdentifierFilter);

    @SqlQuery("select count(*) from builds where component_version like :version and component_identifier like :identifier")
    long countBuildsWithVersionAndIdentifierFilters(@Bind("version") String componentVersionFilter, @Bind("identifier") String componentIdentifierFilter);

    @SqlUpdate("insert into builds " 
        + "(repo_namespace, repo_name, commit_ref, commit_user, source_branch, component_identifier, component_version, distribution_location, extra_deployment_info) " 
        + "values " 
        + "(:repoNamespace, :repoName, :commitRef, :commitUser, :sourceBranch, :componentIdentifier, :componentVersion, :distributionLocation, :extraData)")
    @GetGeneratedKeys
    long insertBuild(@BindBean Build build, @Bind("extraData") String extraDataJson);
}
