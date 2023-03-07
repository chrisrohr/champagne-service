package org.kiwiproject.champagne.dao.mappers;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.kiwiproject.champagne.model.DeployableSystem;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SystemUserMapper implements RowMapper<DeployableSystem.SystemUser> {

    @Override
    public DeployableSystem.SystemUser map(ResultSet rs, StatementContext ctx) throws SQLException {
        return DeployableSystem.SystemUser.builder()
                .userId(rs.getLong("user_id"))
                .admin(rs.getBoolean("system_admin"))
                .build();
    }
}
