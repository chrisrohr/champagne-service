package org.kiwiproject.champagne.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.kiwiproject.champagne.util.TestObjects.insertDeployableSystem;
import static org.kiwiproject.champagne.util.TestObjects.insertDeploymentEnvironmentRecord;
import static org.kiwiproject.champagne.util.TestObjects.insertHostRecord;
import static org.kiwiproject.collect.KiwiLists.first;
import static org.kiwiproject.test.util.DateTimeTestHelper.assertTimeDifferenceWithinTolerance;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import org.jdbi.v3.core.Handle;
import org.jdbi.v3.postgres.PostgresPlugin;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.kiwiproject.champagne.dao.mappers.HostMapper;
import org.kiwiproject.champagne.model.Host;
import org.kiwiproject.test.junit.jupiter.Jdbi3DaoExtension;
import org.kiwiproject.test.junit.jupiter.PostgresLiquibaseTestExtension;

@DisplayName("HostDao")
class HostDaoTest {
    
    @RegisterExtension
    static final PostgresLiquibaseTestExtension POSTGRES = new PostgresLiquibaseTestExtension("migrations.xml");

    @RegisterExtension
    final Jdbi3DaoExtension<HostDao> daoExtension = Jdbi3DaoExtension.<HostDao>builder()
            .daoType(HostDao.class)
            .dataSource(POSTGRES.getTestDataSource())
            .plugins(List.of(new SqlObjectPlugin(), new PostgresPlugin()))
            .build();

    private HostDao dao;
    private Handle handle;

    @BeforeEach
    void setUp() {
        dao = daoExtension.getDao();
        handle = daoExtension.getHandle();
    }

    @Nested
    class InsertHost {

        @Test
        void shouldInsertHostSuccessfully() {
            var systemId = insertDeployableSystem(handle, "kiwi");
            var envId = insertDeploymentEnvironmentRecord(handle, "dev", systemId);
            var beforeInsert = ZonedDateTime.now();

            var hostToInsert = Host.builder()
                .environmentId(envId)
                .hostname("localhost")
                .source(Host.Source.CHAMPAGNE)
                .build();

            var id = dao.insertHost(hostToInsert);

            var hosts = handle.select("select * from hosts where id = ?", id)
                .map(new HostMapper())
                .list();

            assertThat(hosts).hasSize(1);

            var host = first(hosts);
            assertThat(host.getId()).isEqualTo(id);

            assertTimeDifferenceWithinTolerance("createdAt", beforeInsert, host.getCreatedAt().atZone(ZoneOffset.UTC), 1000L);
            assertTimeDifferenceWithinTolerance("updatedAt", beforeInsert, host.getUpdatedAt().atZone(ZoneOffset.UTC), 1000L);

            assertThat(host)
                .usingRecursiveComparison()
                .ignoringFields("id", "createdAt", "updatedAt")
                .isEqualTo(hostToInsert);
        }
    }

    @Nested
    class FindHostsByEnvId {

        @Test
        void shouldReturnListOfHosts() {
            var systemId = insertDeployableSystem(handle, "kiwi");
            var envId = insertDeploymentEnvironmentRecord(handle, "dev", systemId);
            var hostId = insertHostRecord(handle, "localhost", envId, systemId);

            var hosts = dao.findHostsByEnvId(envId, systemId);
            assertThat(hosts)
                .extracting("id", "hostname", "environmentId")
                .contains(tuple(hostId, "localhost", envId));
        }

        @Test
        void shouldReturnEmptyListWhenNoHostsFound() {
            var systemId = insertDeployableSystem(handle, "kiwi");
            var envId = insertDeploymentEnvironmentRecord(handle, "dev", systemId);
            var envId2 = insertDeploymentEnvironmentRecord(handle, "test", systemId);
            insertHostRecord(handle, "localhost", envId, systemId);

            var hosts = dao.findHostsByEnvId(envId2, systemId);
            assertThat(hosts).isEmpty();
        }
    }

    @Nested
    class DeleteHost {

        @Test
        void shouldDeleteHostSuccessfully() {
            var systemId = insertDeployableSystem(handle, "kiwi");
            var envId = insertDeploymentEnvironmentRecord(handle, "dev", systemId);
            var hostId = insertHostRecord(handle, "localhost", envId, systemId);

            dao.deleteHost(hostId);

            var hosts = handle.select("select * from hosts where id = ?", hostId).map(new HostMapper()).list();
            assertThat(hosts).isEmpty();
        }

    }

    @Nested
    class FindById {

        @Test
        void shouldReturnHostWhenFound() {
            var systemId = insertDeployableSystem(handle, "kiwi");
            var envId = insertDeploymentEnvironmentRecord(handle, "dev", systemId);
            var hostId = insertHostRecord(handle, "localhost", envId, systemId);

            var host = dao.findById(hostId).orElseThrow();

            assertThat(host.getHostname()).isEqualTo("localhost");
            assertThat(host.getId()).isEqualTo(hostId);
            assertThat(host.getEnvironmentId()).isEqualTo(envId);
        }

        @Test
        void shouldReturnEmptyWhenNoHostFound() {
            var systemId = insertDeployableSystem(handle, "kiwi");
            var envId = insertDeploymentEnvironmentRecord(handle, "dev", systemId);
            insertHostRecord(handle, "localhost", envId, systemId);

            var hosts = dao.findById(0L);
            assertThat(hosts).isEmpty();
        }
    }

}
