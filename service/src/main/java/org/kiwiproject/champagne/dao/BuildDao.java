package org.kiwiproject.champagne.dao;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.kiwiproject.base.KiwiStrings.f;
import static org.kiwiproject.champagne.dao.DaoHelper.LIKE_QUERY_FORMAT;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.kiwiproject.champagne.model.Build;

import java.util.List;

public interface BuildDao {
    
    default List<Build> findPagedBuilds(int offset, int limit, long systemId, String componentIdentifierFilter, String componentVersionFilter) {
        if (isBlank(componentIdentifierFilter) && isBlank(componentVersionFilter)) {
            // No filtering
            return findPagedBuildsNoFilter(offset, limit, systemId);
        } else if (isBlank(componentIdentifierFilter)) {
            // Version only filter
            return findPagedBuildsWithVersionFilter(offset, limit, systemId, f(LIKE_QUERY_FORMAT, componentVersionFilter));
        } else if (isBlank(componentVersionFilter)) {
            // Name only filter
            return findPagedBuildsWithIdentiferFilter(offset, limit, systemId, f(LIKE_QUERY_FORMAT, componentIdentifierFilter));
        }

        // Both name and version filtering
        return findPagedBuildsWithVersionAndIdentifierFilters(offset, limit, systemId, componentIdentifierFilter, componentVersionFilter);
    }

    @SqlQuery("select * from builds where deployable_system_id = :systemId order by created_at desc offset :offset limit :limit")
    List<Build> findPagedBuildsNoFilter(@Bind("offset") int offset, @Bind("limit") int limit, @Bind("systemId") long systemId);

    @SqlQuery("select * from builds where builds.deployable_system_id = :systemId and component_version like :version order by created_at desc offset :offset limit :limit")
    List<Build> findPagedBuildsWithVersionFilter(@Bind("offset") int offset, @Bind("limit") int limit, @Bind("systemId") long systemId, @Bind("version") String componentVersionFilter);

    @SqlQuery("select * from builds where builds.deployable_system_id = :systemId and component_identifier like :identifier order by created_at desc offset :offset limit :limit")
    List<Build> findPagedBuildsWithIdentiferFilter(@Bind("offset") int offset, @Bind("limit") int limit, @Bind("systemId") long systemId, @Bind("identifier") String componentIdentifierFilter);

    @SqlQuery("select * from builds where builds.deployable_system_id = :systemId and component_version like :version and component_identifier like :identifier order by created_at desc offset :offset limit :limit")
    List<Build> findPagedBuildsWithVersionAndIdentifierFilters(@Bind("offset") int offset, @Bind("limit") int limit, @Bind("systemId") long systemId, @Bind("identifier") String componentIdentifierFilter, @Bind("version") String componentVersionFilter);

    default long countBuilds(long systemId, String componentIdentifierFilter, String componentVersionFilter) {
        if (isBlank(componentIdentifierFilter) && isBlank(componentVersionFilter)) {
            // No filtering
            return countBuildsNoFilter(systemId);
        } else if (isBlank(componentIdentifierFilter)) {
            // Version only filter
            return countBuildsWithVersionFilter(systemId, componentVersionFilter);
        } else if (isBlank(componentVersionFilter)) {
            // Name only filter
            return countBuildsWithIdentifierFilter(systemId, componentIdentifierFilter);
        }

        // Both name and version filtering
        return countBuildsWithVersionAndIdentifierFilters(systemId, componentVersionFilter, componentIdentifierFilter);
    }

    @SqlQuery("select count(*) from builds where deployable_system_id = :systemId")
    long countBuildsNoFilter(@Bind("systemId") long systemId);

    @SqlQuery("select count(*) from builds where builds.deployable_system_id = :systemId and component_version like :version")
    long countBuildsWithVersionFilter(@Bind("systemId") long systemId, @Bind("version") String componentVersionFilter);

    @SqlQuery("select count(*) from builds where builds.deployable_system_id = :systemId and component_identifier like :identifier")
    long countBuildsWithIdentifierFilter(@Bind("systemId") long systemId, @Bind("identifier") String componentIdentifierFilter);

    @SqlQuery("select count(*) from builds where builds.deployable_system_id = :systemId and component_version like :version and component_identifier like :identifier")
    long countBuildsWithVersionAndIdentifierFilters(@Bind("systemId") long systemId, @Bind("version") String componentVersionFilter, @Bind("identifier") String componentIdentifierFilter);

    @SqlUpdate("insert into builds " 
        + "(repo_namespace, repo_name, commit_ref, commit_user, source_branch, component_identifier, component_version, distribution_location, extra_deployment_info, change_log, git_provider, deployable_system_id) "
        + "values " 
        + "(:repoNamespace, :repoName, :commitRef, :commitUser, :sourceBranch, :componentIdentifier, :componentVersion, :distributionLocation, :extraData, :changeLog, :gitProvider, :deployableSystemId)")
    @GetGeneratedKeys
    long insertBuild(@BindBean Build build, @Bind("extraData") String extraDataJson);
}
