package org.kiwiproject.champagne.dao.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.kiwiproject.champagne.model.DeployableSystem;

public class SystemUserMapper implements RowMapper<DeployableSystem.SystemUser> {

    @Override
    public DeployableSystem.SystemUser map(ResultSet rs, StatementContext ctx) throws SQLException {
        return DeployableSystem.SystemUser.builder()
                .userId(rs.getLong("id"))
                .displayName(rs.getString("display_name"))
                .systemIdentifier(rs.getString("system_identifier"))
                .admin(rs.getBoolean("system_admin"))
                .build();
    }
}
