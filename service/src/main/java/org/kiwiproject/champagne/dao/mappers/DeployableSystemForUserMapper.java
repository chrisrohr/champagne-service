package org.kiwiproject.champagne.dao.mappers;

import org.jdbi.v3.core.statement.StatementContext;
import org.kiwiproject.champagne.model.DeployableSystem;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DeployableSystemForUserMapper extends DeployableSystemMapper {
    @Override
    public DeployableSystem map(ResultSet rs, StatementContext ctx) throws SQLException {
        var deployableSystem = super.map(rs, ctx);

        return deployableSystem.withAdmin(rs.getBoolean("system_admin"));
    }
}
