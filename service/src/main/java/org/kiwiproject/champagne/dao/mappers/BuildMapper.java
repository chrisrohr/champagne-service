package org.kiwiproject.champagne.dao.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.kiwiproject.champagne.model.Build;
import org.kiwiproject.jdbc.KiwiJdbc;
import org.kiwiproject.json.JsonHelper;

public class BuildMapper implements RowMapper<Build>{
    
    private final JsonHelper jsonHelper;

    public BuildMapper(JsonHelper jsonHelper) {
        this.jsonHelper = jsonHelper;
    }

    @Override
    public Build map(ResultSet rs, StatementContext ctx) throws SQLException {
        return Build.builder()
            .id(rs.getLong("id"))
            .createdAt(KiwiJdbc.instantFromTimestamp(rs, "created_at"))
            .repoNamespace(rs.getString("repo_namespace"))
            .repoName(rs.getString("repo_name"))
            .commitRef(rs.getString("commit_ref"))
            .commitUser(rs.getString("commit_user"))
            .sourceBranch(rs.getString("source_branch"))
            .componentIdentifier(rs.getString("component_identifier"))
            .componentVersion(rs.getString("component_version"))
            .distributionLocation(rs.getString("distribution_location"))
            .extraDeploymentInfo(jsonToMap(rs, "extra_deployment_info"))
            .build();
    }

    private Map<String, Object> jsonToMap(ResultSet rs, String columnName) throws SQLException {
        var optionalJson = Optional.ofNullable(rs.getString(columnName));

        return optionalJson.map(jsonHelper::toMap).orElse(Map.of());
    }
}
