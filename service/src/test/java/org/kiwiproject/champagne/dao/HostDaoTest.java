package org.kiwiproject.champagne.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.kiwiproject.champagne.util.TestObjects.insertDeployableSystem;
import static org.kiwiproject.champagne.util.TestObjects.insertDeploymentEnvironmentRecord;
import static org.kiwiproject.champagne.util.TestObjects.insertHostRecord;
import static org.kiwiproject.champagne.util.TestObjects.insertTagRecord;
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

            var host = handle.select("select * from hosts where id = ?", id)
                .map(new HostMapper())
                .first();

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

    @Nested
    class UpdateTagList {
        @Test
        void shouldAddGivenTagsWhenNoneExist() {
            var systemId = insertDeployableSystem(handle, "kiwi");
            var envId = insertDeploymentEnvironmentRecord(handle, "dev", systemId);
            var hostId = insertHostRecord(handle, "localhost", envId, systemId);
            var tagId = insertTagRecord(handle, "core", systemId);

            dao.updateTagList(hostId, List.of(tagId));

            var tag_ids = handle.select("select tag_id from host_tags where host_id = ?", hostId)
                    .mapTo(Long.class)
                    .list();

            assertThat(tag_ids).contains(tagId);
        }

        @Test
        void shouldRemoveAllTagsIfNoneGiven() {
            var systemId = insertDeployableSystem(handle, "kiwi");
            var envId = insertDeploymentEnvironmentRecord(handle, "dev", systemId);
            var hostId = insertHostRecord(handle, "localhost", envId, systemId);
            var tagId = insertTagRecord(handle, "core", systemId);

            handle.createUpdate("insert into host_tags "
                            + "(host_id, tag_id) "
                            + "values "
                            + "(:hostId, :tagId)")
                    .bind("hostId", hostId)
                    .bind("tagId", tagId)
                    .execute();

            dao.updateTagList(hostId, List.of());

            var tag_ids = handle.select("select tag_id from host_tags where host_id = ?", hostId)
                    .mapTo(Long.class)
                    .list();

            assertThat(tag_ids).isEmpty();
        }

        @Test
        void shouldAddGivenTags() {
            var systemId = insertDeployableSystem(handle, "kiwi");
            var envId = insertDeploymentEnvironmentRecord(handle, "dev", systemId);
            var hostId = insertHostRecord(handle, "localhost", envId, systemId);
            var tagId = insertTagRecord(handle, "core", systemId);
            var tagId2 = insertTagRecord(handle, "audit", systemId);

            handle.createUpdate("insert into host_tags "
                            + "(host_id, tag_id) "
                            + "values "
                            + "(:hostId, :tagId)")
                    .bind("hostId", hostId)
                    .bind("tagId", tagId)
                    .execute();

            dao.updateTagList(hostId, List.of(tagId, tagId2));

            var tag_ids = handle.select("select tag_id from host_tags where host_id = ?", hostId)
                    .mapTo(Long.class)
                    .list();

            assertThat(tag_ids).contains(tagId, tagId2);
        }

        @Test
        void shouldRemoveSomeTags() {
            var systemId = insertDeployableSystem(handle, "kiwi");
            var envId = insertDeploymentEnvironmentRecord(handle, "dev", systemId);
            var hostId = insertHostRecord(handle, "localhost", envId, systemId);
            var tagId = insertTagRecord(handle, "core", systemId);
            var tagId2 = insertTagRecord(handle, "audit", systemId);

            handle.createUpdate("insert into host_tags "
                            + "(host_id, tag_id) "
                            + "values "
                            + "(:hostId, :tagId)")
                    .bind("hostId", hostId)
                    .bind("tagId", tagId)
                    .execute();

            handle.createUpdate("insert into host_tags "
                            + "(host_id, tag_id) "
                            + "values "
                            + "(:hostId, :tagId)")
                    .bind("hostId", hostId)
                    .bind("tagId", tagId2)
                    .execute();

            dao.updateTagList(hostId, List.of(tagId));

            var tag_ids = handle.select("select tag_id from host_tags where host_id = ?", hostId)
                    .mapTo(Long.class)
                    .list();

            assertThat(tag_ids).contains(tagId);
        }
    }

    @Nested
    class FindTagIdsForHost {
        @Test
        void shouldFindMatchingTagIdsLinkedToAGivenHost() {
            var systemId = insertDeployableSystem(handle, "kiwi");
            var envId = insertDeploymentEnvironmentRecord(handle, "dev", systemId);
            var hostId = insertHostRecord(handle, "localhost", envId, systemId);
            var tagId = insertTagRecord(handle, "core", systemId);

            handle.createUpdate("insert into host_tags "
                            + "(host_id, tag_id) "
                            + "values "
                            + "(:hostId, :tagId)")
                    .bind("hostId", hostId)
                    .bind("tagId", tagId)
                    .execute();

            var tagIds = dao.findTagIdsForHost(hostId);

            assertThat(tagIds).contains(tagId);
        }
    }

    @Nested
    class FindHostsForTagsInEnv {
        @Test
        void shouldFindHostsWithGivenTags() {
            var systemId = insertDeployableSystem(handle, "kiwi");
            var envId = insertDeploymentEnvironmentRecord(handle, "dev", systemId);
            var hostId = insertHostRecord(handle, "localhost", envId, systemId);
            var tagId = insertTagRecord(handle, "core", systemId);

            handle.createUpdate("insert into host_tags "
                            + "(host_id, tag_id) "
                            + "values "
                            + "(:hostId, :tagId)")
                    .bind("hostId", hostId)
                    .bind("tagId", tagId)
                    .execute();

            var hosts = dao.findHostsForTagsInEnv(systemId, envId, List.of(tagId));

            assertThat(hosts).hasSize(1)
                    .extracting("id", "hostname")
                    .contains(tuple(hostId, "localhost"));
        }
    }

    @Nested
    class UpdateHost {
        @Test
        void shouldUpdateGivenHost() {
            var systemId = insertDeployableSystem(handle, "kiwi");
            var envId = insertDeploymentEnvironmentRecord(handle, "dev", systemId);
            var hostId = insertHostRecord(handle, "localhost", envId, systemId);

            var updateCount = dao.updateHost("server-1", hostId);

            assertThat(updateCount).isOne();

            var host = handle.select("select * from hosts where id = ?", hostId)
                    .map(new HostMapper())
                    .first();

            assertThat(host.getHostname()).isEqualTo("server-1");
        }
    }
}
