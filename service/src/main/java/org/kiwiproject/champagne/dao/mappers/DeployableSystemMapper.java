package org.kiwiproject.champagne.dao.mappers;

import static org.kiwiproject.jdbc.KiwiJdbc.instantFromTimestamp;
import static org.kiwiproject.jdbc.KiwiJdbc.longValueOrNull;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.kiwiproject.champagne.model.DeployableSystem;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DeployableSystemMapper implements RowMapper<DeployableSystem> {
    @Override
    public DeployableSystem map(ResultSet rs, StatementContext ctx) throws SQLException {
        return DeployableSystem.builder()
                .id(rs.getLong("id"))
                .createdAt(instantFromTimestamp(rs, "created_at"))
                .updatedAt(instantFromTimestamp(rs, "updated_at"))
                .name(rs.getString("name"))
                .devEnvironmentId(longValueOrNull(rs, "dev_environment_id"))
                .environmentPromotionOrder(rs.getString("environment_promotion_order"))
                .build();
    }
}
