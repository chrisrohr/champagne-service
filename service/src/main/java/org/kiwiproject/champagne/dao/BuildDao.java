package org.kiwiproject.champagne.dao;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.kiwiproject.base.KiwiStrings.f;
import static org.kiwiproject.champagne.dao.DaoHelper.LIKE_QUERY_FORMAT;

import java.util.List;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.kiwiproject.champagne.model.Build;

public interface BuildDao {
    
    default List<Build> findPagedBuilds(int offset, int limit, long systemId, String componentFilter) {
        if (isBlank(componentFilter)) {
            // No filtering
            return findPagedBuildsNoFilter(offset, limit, systemId);
        }

        // Both name and version filtering
        return findPagedBuildsWithVersionOrIdentifierFilters(offset, limit, systemId, f(LIKE_QUERY_FORMAT, componentFilter));
    }

    @SqlQuery("select * from builds where deployable_system_id = :systemId order by created_at desc offset :offset limit :limit")
    List<Build> findPagedBuildsNoFilter(@Bind("offset") int offset, @Bind("limit") int limit, @Bind("systemId") long systemId);

    @SqlQuery("select * from builds where builds.deployable_system_id = :systemId and (component_version like :componentFilter or component_identifier like :componentFilter) order by created_at desc offset :offset limit :limit")
    List<Build> findPagedBuildsWithVersionOrIdentifierFilters(@Bind("offset") int offset, @Bind("limit") int limit, @Bind("systemId") long systemId, @Bind("componentFilter") String componentFilter);

    default long countBuilds(long systemId, String componentFilter) {
        if (isBlank(componentFilter)) {
            // No filtering
            return countBuildsNoFilter(systemId);
        }

        // Both name and version filtering
        return countBuildsWithVersionOrIdentifierFilters(systemId, componentFilter);
    }

    @SqlQuery("select count(*) from builds where deployable_system_id = :systemId")
    long countBuildsNoFilter(@Bind("systemId") long systemId);

    @SqlQuery("select count(*) from builds where builds.deployable_system_id = :systemId and (component_version like :componentFilter or component_identifier like :componentFilter)")
    long countBuildsWithVersionOrIdentifierFilters(@Bind("systemId") long systemId, @Bind("componentFilter") String componentFilter);

    @SqlUpdate("insert into builds " 
        + "(repo_namespace, repo_name, commit_ref, commit_user, source_branch, component_identifier, component_version, distribution_location, extra_deployment_info, change_log, git_provider, deployable_system_id) "
        + "values " 
        + "(:repoNamespace, :repoName, :commitRef, :commitUser, :sourceBranch, :componentIdentifier, :componentVersion, :distributionLocation, :extraData, :changeLog, :gitProvider, :deployableSystemId)")
    @GetGeneratedKeys
    long insertBuild(@BindBean Build build, @Bind("extraData") String extraDataJson);
}
