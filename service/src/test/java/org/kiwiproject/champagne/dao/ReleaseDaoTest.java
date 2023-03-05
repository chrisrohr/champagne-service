package org.kiwiproject.champagne.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kiwiproject.champagne.util.TestObjects.insertDeployableSystem;
import static org.kiwiproject.champagne.util.TestObjects.insertReleaseRecord;
import static org.kiwiproject.collect.KiwiLists.first;
import static org.kiwiproject.test.util.DateTimeTestHelper.assertTimeDifferenceWithinTolerance;

import org.jdbi.v3.core.Handle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.kiwiproject.champagne.dao.mappers.ReleaseMapper;
import org.kiwiproject.champagne.model.manualdeployment.Release;
import org.kiwiproject.test.junit.jupiter.Jdbi3DaoExtension;
import org.kiwiproject.test.junit.jupiter.PostgresLiquibaseTestExtension;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@DisplayName("ReleaseDao")
class ReleaseDaoTest {
    
    @RegisterExtension
    static final PostgresLiquibaseTestExtension POSTGRES = new PostgresLiquibaseTestExtension("migrations.xml");

    @RegisterExtension
    final Jdbi3DaoExtension<ReleaseDao> daoExtension = Jdbi3DaoExtension.<ReleaseDao>builder()
            .daoType(ReleaseDao.class)
            .dataSource(POSTGRES.getTestDataSource())
            .build();

    private ReleaseDao dao;
    private Handle handle;

    @BeforeEach
    void setUp() {
        dao = daoExtension.getDao();
        handle = daoExtension.getHandle();
    }

    @Nested
    class InsertRelease {

        @Test
        void shouldInsertReleaseSuccessfully() {
            var beforeInsert = ZonedDateTime.now();

            var releaseToInsert = Release.builder()
                .releaseNumber("2023.42.0")
                .build();

            var id = dao.insertRelease(releaseToInsert);

            var releases = handle.select("select * from manual_deployment_task_releases where id = ?", id)
                .map(new ReleaseMapper())
                .list();

            assertThat(releases).hasSize(1);

            var release = first(releases);
            assertThat(release.getId()).isEqualTo(id);

            assertTimeDifferenceWithinTolerance("createdAt", beforeInsert, release.getCreatedAt().atZone(ZoneOffset.UTC), 1000L);
            assertTimeDifferenceWithinTolerance("updatedAt", beforeInsert, release.getUpdatedAt().atZone(ZoneOffset.UTC), 1000L);

            assertThat(release.getReleaseNumber()).isEqualTo("2023.42.0");
        }
    }

    @Nested
    class FindPagedReleases {

        @Test
        void shouldReturnListOfReleases() {
            var systemId = insertDeployableSystem(handle, "kiwi");
            insertReleaseRecord(handle, "42", systemId);

            var releases = dao.findPagedReleases(0, 10, systemId);
            assertThat(releases)
                .extracting("releaseNumber")
                .contains("42");
        }

        @Test
        void shouldReturnEmptyListWhenNoReleasesFound() {
            var systemId = insertDeployableSystem(handle, "kiwi");
            insertReleaseRecord(handle, "42", systemId);

            var releases = dao.findPagedReleases(10, 10, systemId);
            assertThat(releases).isEmpty();
        }
    }

    @Nested
    class CountReleases {

        @Test
        void shouldReturnCountOfReleases() {
            var systemId = insertDeployableSystem(handle, "kiwi");
            insertReleaseRecord(handle, "42", systemId);

            var releases = dao.countReleases(systemId);
            assertThat(releases).isOne();
        }

        @Test
        void shouldReturnEmptyListWhenNoReleasesFound() {
            var releases = dao.countReleases(1L);
            assertThat(releases).isZero();
        }
    }

    @Nested
    class DeleteById {

        @Test
        void shouldDeleteReleaseSuccessfully() {
            var systemId = insertDeployableSystem(handle, "kiwi");
            var id = insertReleaseRecord(handle, "42", systemId);

            dao.deleteById(id);

            var releases = handle.select("select * from manual_deployment_task_releases where id = ?", id)
                .map(new ReleaseMapper())
                .list();

            assertThat(releases).isEmpty();
        }

    }

    @Nested
    class FindAllReleaseIds {

        @Test
        void shouldReturnListOfReleaseIds() {
            var systemId = insertDeployableSystem(handle, "kiwi");
            var releaseId = insertReleaseRecord(handle, "42", systemId);

            var releaseIds = dao.findAllReleaseIds();
            assertThat(releaseIds).contains(releaseId);
        }

        @Test
        void shouldReturnEmptyListWhenNoReleasesFound() {
            var releaseIds = dao.findAllReleaseIds();
            assertThat(releaseIds).isEmpty();
        }
    }

}
